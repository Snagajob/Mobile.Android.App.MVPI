package com.snagajob.mvpireference.views.login

import com.snagajob.mvpireference.basearchitecture.Interactor
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.services.login.LoginService
import com.snagajob.mvpireference.services.login.LoginService.LoginServiceResult
import com.snagajob.mvpireference.views.login.models.LoginAction
import com.snagajob.mvpireference.views.login.models.LoginResult
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers

class LoginInteractor(actions: Observable<LoginAction>, private val loginService: LoginService): Interactor<LoginResult>() {

    init {
        actions.compose(ActionToResult())
                .subscribe { results.onNext(it) }

        loginService.loginResults()
                .compose(LoginServiceResultTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { results.onNext(it) }
    }

    private inner class ActionToResult: ObservableTransformer<LoginAction, LoginResult> {
        override fun apply(upstream: Observable<LoginAction>): ObservableSource<LoginResult> {
            return upstream.publish { source ->
                merge<LoginResult>(
                        source.ofType(LoginAction.LoginAttempt::class.java).map {
                            loginService.attemptToLogin(it.username, it.password)
                            LoginResult.RequestInProgress()
                        },
                        source.ofType(LoginAction.SnackbarDismiss::class.java).map { LoginResult.SnackbarDismissed() }
                )
            }
        }
    }

    private class LoginServiceResultTransformer: ObservableTransformer<LoginService.LoginServiceResult, LoginResult> {
        override fun apply(upstream: Observable<LoginService.LoginServiceResult>): ObservableSource<LoginResult> {
            return upstream.publish { source ->
                merge<LoginResult>(
                        source.ofType(LoginServiceResult.BadCredentials::class.java).map { LoginResult.BadCredentials() },
                        source.ofType(LoginServiceResult.NetworkError::class.java).map { LoginResult.NetworkFailure() },
                        source.ofType(LoginServiceResult.Success::class.java).map { LoginResult.LoginSuccess() }
                )
            }
        }
    }
}