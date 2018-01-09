package com.snagajob.mvpireference.views.login.models

import com.coreyhorn.mvpiframework.basemodels.Result

sealed class LoginResult : Result() {
    class LoginSuccess(val customers: List<String>) : LoginResult()
    class BadCredentials : LoginResult()
    class NetworkFailure : LoginResult()
    class UnknownError : LoginResult()
    class RequestInProgress : LoginResult()
    class SnackbarDismissed : LoginResult()
}