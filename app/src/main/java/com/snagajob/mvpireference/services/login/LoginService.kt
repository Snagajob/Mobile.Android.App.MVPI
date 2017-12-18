package com.snagajob.mvpireference.services.login

import android.util.Base64
import com.snagajob.mvpireference.services.*
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

class LoginService {

    private val loginResults: PublishSubject<LoginServiceResult> = PublishSubject.create()

    //TODO: [MIPG-106] Consider injecting moshi where needed.
    val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val newRequest = chain.request().newBuilder()

                        .addHeader(OS_NAME, "Android")
                        .addHeader(USER_AGENT, "Fake News")
                        .addHeader(APP_NAME, "PMGo")
                        .addHeader(APP_VERSION, "0.01.01")
                        .addHeader(OS_VERSION, "4.3.2")
                        .addHeader(SESSION_ID, "Fake News")
                        .addHeader(APP_INSTANCE_ID, "Fake News")

                        .build()

                chain.proceed(newRequest)
            }
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    val retrofit = Retrofit.Builder()
            .baseUrl("http://mpi-go.api.snagqa.corp")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    val loginService = retrofit.create(Login::class.java)

    fun attemptToLogin(username: String, password: String) {
        loginService.getToken("Basic " + Base64.encodeToString((username + ":" + password).toByteArray(), Base64.NO_WRAP))
                .subscribeOn(Schedulers.io())
                .map {
                    if (it.isSuccessful) {
                        val customerName = it.body()!!.tokens.map { it.customerName }
                        LoginServiceResult.Success(customerName)
                    } else {
                        //TODO: Should be based on response codes or something similar.
                        //QA MPI is outputting 500's in the event of bad passwords currently.
                        LoginServiceResult.BadCredentials()
                    }
                }
                .onErrorReturn {
                    LoginServiceResult.NetworkError()
                }
                .subscribe { loginResults.onNext(it) }
    }

    fun loginResults(): Observable<LoginServiceResult> = loginResults

    fun random(from: Int, to: Int) = (Math.random() * (to - from) + from).toInt()



    interface Login {
        @GET("/v1/Authentication/token")
        fun getToken(@Header("Authorization") encodedPassword: String): Observable<Response<LoginResponse>>
    }

    sealed class LoginServiceResult {
        class Success(val customers: List<String>): LoginServiceResult()
        class BadCredentials: LoginServiceResult()
        class NetworkError: LoginServiceResult()
    }
}