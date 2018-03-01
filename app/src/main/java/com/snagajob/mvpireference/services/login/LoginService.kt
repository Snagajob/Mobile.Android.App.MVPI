package com.snagajob.mvpireference.services.login

import android.util.Base64
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header

class LoginService(retrofit: Retrofit) {

    fun loginResults(): Observable<LoginServiceResult> = loginResults

    fun attemptToLogin(username: String, password: String) {
        val authToken = "Basic " + Base64.encodeToString((username + ":" + password).toByteArray(), Base64.NO_WRAP)
        service.getToken(authToken)
                .subscribeOn(Schedulers.io())
                .map {
                    if (it.isSuccessful) {
                        val customerName = it.body()!!.tokens.map { it.customerName }
                        LoginServiceResult.Success(customerName)
                    } else {
                        when (it.code()) {
                            401 -> LoginServiceResult.BadCredentials()
                            else -> LoginServiceResult.NetworkError()
                        }
                    }
                }
                .onErrorReturn {
                    LoginServiceResult.NetworkError()
                }
                .subscribe(loginResults)
    }

    private val service = retrofit.create(Login::class.java)
    private val loginResults: PublishSubject<LoginServiceResult> = PublishSubject.create()

    interface Login {
        @GET("/v1/Authentication/token")
        fun getToken(@Header("Authorization") authorizationToken: String): Observable<Response<LoginResponse>>
    }

    sealed class LoginServiceResult {
        class Success(val customers: List<String>): LoginServiceResult()
        class BadCredentials: LoginServiceResult()
        class NetworkError: LoginServiceResult()
    }
}