package com.snagajob.mvpireference.views.login

import com.snagajob.mvpireference.basearchitecture.Presenter
import com.snagajob.mvpireference.disposeWith
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.services.login.LoginService
import com.snagajob.mvpireference.views.login.models.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class LoginPresenter: Presenter<LoginEvent, LoginAction, LoginResult, LoginState>() {

    init {
        //TODO: Inject LoginService so all references are the same instance.
        attachResultStream(LoginInteractor(actions, LoginService()).results())
    }

    override fun attachEventStream(events: Observable<LoginEvent>) {
        super.attachEventStream(events)

        events.compose(EventToActionTransformer())
                .subscribe { actions.onNext(it) }
                .disposeWith(eventDisposables)
    }

    override fun attachResultStream(results: Observable<LoginResult>) {
        results.scan(LoginState.idle(), this::loginAccumulator)
                .subscribe {
                    //Emit the current state
                    states.onNext(it)

                    //Switch the snackbar state to hidden to prevent showing on rotation
                    if (it.snackbarState != SnackbarState.Hidden()) {
                        states.onNext(it.copy(snackbarState = SnackbarState.Hidden()))
                    }
                }
    }

    private class EventToActionTransformer: ObservableTransformer<LoginEvent, LoginAction> {
        override fun apply(upstream: Observable<LoginEvent>): ObservableSource<LoginAction> {
            return upstream.publish { source ->
                merge<LoginAction>(
                        source.ofType(LoginEvent.LoginAttempt::class.java).map { LoginAction.LoginAttempt(it.username, it.password) },
                        source.ofType(LoginEvent.SnackbarDismiss::class.java).map { LoginAction.SnackbarDismiss() }
                )
            }
        }
    }

    private fun loginAccumulator(previousState: LoginState, result: LoginResult): LoginState = when (result) {
        is LoginResult.BadCredentials -> LoginState(true, SnackbarState.BadCredentials())
        is LoginResult.LoginSuccess -> LoginState(true, SnackbarState.LoginSuccess())
        is LoginResult.NetworkFailure -> LoginState(true, SnackbarState.NetworkFailure())
        is LoginResult.UnknownError -> LoginState(true, SnackbarState.UnknownFailure())
        is LoginResult.RequestInProgress -> LoginState(false, SnackbarState.Hidden())
        is LoginResult.SnackbarDismissed -> previousState.copy(snackbarState = SnackbarState.Hidden())
    }
}