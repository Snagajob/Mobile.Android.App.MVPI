package com.snagajob.mvpireference.services.login

import com.snagajob.mvpireference.services.OS_NAME
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

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

                        .build()

                chain.proceed(newRequest)
            }

    fun attemptToLogin(username: String, password: String) {
        //TODO: Change to service call
        Observable.timer(3000, TimeUnit.MILLISECONDS)
                .subscribe {
                    val randomNumber = random(0, 2)
                    if (randomNumber == 0) {
                        loginResults.onNext(LoginServiceResult.NetworkError())
                    } else {
                        loginResults.onNext(LoginServiceResult.BadCredentials())
                    }
                }
    }

    fun loginResults(): Observable<LoginServiceResult> = loginResults

    fun random(from: Int, to: Int) = (Math.random() * (to - from) + from).toInt()



    interface Login {
        @GET("/v1/Authentication/token")
        fun getToken(): Observable<Response<String>>
    }

    sealed class LoginServiceResult {
        class Success: LoginServiceResult()
        class BadCredentials: LoginServiceResult()
        class NetworkError: LoginServiceResult()
    }
}