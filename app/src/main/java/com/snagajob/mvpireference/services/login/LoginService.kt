package com.snagajob.mvpireference.services.login

import android.annotation.SuppressLint
import android.util.Base64
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header

class LoginService(retrofit: Retrofit) {

    /**
     * Exposes our internal Subject as an Observable. This is what consumers of this service
     * will subscribe to. Services can have many subjects that they expose in this way.
     */
    fun loginResults(): Observable<LoginServiceResult> = loginResults

    /**
     * Exposes the ability of the service to attempt to login. Results are then pushed onto
     * the Subject created for this type of result.
     *
     * We are currently suppressing CheckResult because we are not doing anything with the Disposable
     * returned by the .subscribe call. I believe we don't need to because we are dealing with a Single.
     * More research is needed on whether we need to store that disposable anywhere temporarily.
     */
    @SuppressLint("CheckResult")
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
                .subscribe({loginResults.onNext(it)}, {loginResults.onNext(LoginServiceResult.NetworkError())})
    }

    // The actual service created by retrofit from our interface
    private val service = retrofit.create(Login::class.java)

    /**
     * The Subject used to push results onto. If you want your service to cache results
     * and immediately deliver the cached item when a consumer subscribes, simply
     * change this to a BehaviorSubject
     */
    private val loginResults: PublishSubject<LoginServiceResult> = PublishSubject.create()

    /**
     * We have decided to go with Retrofit for creating our network services.
     * As you can see, these are Singles. This more explicitly shows that there
     * are no long running streams when a request is made.
     */
    interface Login {
        @GET("/v1/Authentication/token")
        fun getToken(@Header("Authorization") authorizationToken: String): Single<Response<LoginResponse>>
    }

    /**
     * Each service will define all possible result / error types.
     * These can be the same or separate for each route
     * Known error types should be separated when possible and
     * providing a generic error type will make consuming streams of these objects
     * much cleaner.
     */
    sealed class LoginServiceResult {
        class Success(val customers: List<String>): LoginServiceResult()
        class BadCredentials: LoginServiceResult()
        class NetworkError: LoginServiceResult()
    }
}