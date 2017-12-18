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

    fun attemptToLogin(username: String, password: String) {
        val authToken = "Basic " + Base64.encodeToString((username + ":" + password).toByteArray(), Base64.NO_WRAP)
        loginService.getToken(authToken)
                .subscribeOn(Schedulers.io())
                .map {
                    if (it.isSuccessful) {
                        val customerName = it.body()!!.tokens.map { it.customerName }
                        LoginServiceResult.Success(customerName)
                    } else {
                        //TODO: We can identify bad usernames, etc.
                        //MPI is currently 500ing improperly. Will add additional cases later.
                        if (it.code() == 401)
                            LoginServiceResult.BadPassword()
                        else
                            LoginServiceResult.NetworkError()
                    }
                }
                .onErrorReturn {
                    LoginServiceResult.NetworkError()
                }
                .subscribe { loginResults.onNext(it) }
    }

    fun loginResults(): Observable<LoginServiceResult> = loginResults



    //TODO: [MIPG-106] All of these objects should be injected as needed throughout the application.
    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    private val okHttpClient = OkHttpClient.Builder()
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

    private val retrofit = Retrofit.Builder()
            .baseUrl("http://mpi-go.api.snagqa.corp")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    private val loginService = retrofit.create(Login::class.java)

    interface Login {
        @GET("/v1/Authentication/token")
        fun getToken(@Header("Authorization") authorizationToken: String): Observable<Response<LoginResponse>>
    }

    sealed class LoginServiceResult {
        class Success(val customers: List<String>): LoginServiceResult()
        class BadUsername: LoginServiceResult()
        class BadPassword: LoginServiceResult()
        class NetworkError: LoginServiceResult()
    }
}