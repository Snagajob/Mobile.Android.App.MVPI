package com.snagajob.mvpireference.views.login.models

import com.snagajob.mvpireference.basemodels.Result

sealed class LoginResult : Result() {
    class LoginSuccess : LoginResult()
    class BadCredentials : LoginResult()
    class NetworkFailure : LoginResult()
    class UnknownError : LoginResult()
    class RequestInProgress : LoginResult()
    class SnackbarDismissed : LoginResult()
}