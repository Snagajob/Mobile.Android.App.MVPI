package com.snagajob.mvpireference.views.login

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coreyhorn.mvpiframework.architecture.Presenter
import com.coreyhorn.mvpiframework.architecture.PresenterFactory
import com.coreyhorn.mvpiframework.architecture.PresenterFragment
import com.coreyhorn.mvpiframework.disposeWith
import com.jakewharton.rxbinding2.view.clicks
import com.snagajob.mvpireference.LOADER_ID_LOGIN_FRAGMENT
import com.snagajob.mvpireference.R
import com.snagajob.mvpireference.views.login.models.*
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment: PresenterFragment<LoginEvent, LoginAction, LoginResult, LoginState>() {

    private lateinit var snackbar: Snackbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun loaderId(): Int = LOADER_ID_LOGIN_FRAGMENT

    override fun presenterFactory(): PresenterFactory<Presenter<LoginEvent, LoginAction, LoginResult, LoginState>> {
        return object: PresenterFactory<LoginPresenter>() {
            override fun create() = LoginPresenter()
        }
    }

    override fun renderViewState(state: LoginState) {
        Log.d("mvpistate", state.toString())
        submit.isEnabled = state.controlsEnabled
        email.isEnabled = state.controlsEnabled
        password.isEnabled = state.controlsEnabled

        when (state.snackbarState) {
            is SnackbarState.LoginSuccess -> snackbar.setText("Customers: " + state.snackbarState.customers).show()
            is SnackbarState.BadCredentials -> snackbar.setText("Username or password is incorrect").show()
            is SnackbarState.NetworkFailure -> snackbar.setText("Network Failure").show()
            is SnackbarState.UnknownFailure -> snackbar.setText("Unknown Error").show()
            is SnackbarState.Hidden -> {}
        }
    }

    override fun setupViewBindings() {
        submit.clicks()
                .map { LoginEvent.LoginAttempt(email.text.toString(), password.text.toString()) }
                .subscribe { events.onNext(it) }
                .disposeWith(disposables)
    }

    companion object {
        fun newInstance() = LoginFragment()
    }
}