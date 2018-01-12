package com.snagajob.mvpireference.views.login.models

import com.coreyhorn.mvpiframework.basemodels.State

data class LoginState(val controlsEnabled: Boolean = true,
                      val snackbarState: SnackbarState): State() {

    companion object {
        fun idle() = LoginState(true, SnackbarState.Hidden())
    }
}

sealed class SnackbarState {
    class BadCredentials: SnackbarState()
    class Hidden: SnackbarState()
    class LoginSuccess(val customers: List<String>): SnackbarState()
    class NetworkFailure: SnackbarState()
    class UnknownFailure: SnackbarState()
}