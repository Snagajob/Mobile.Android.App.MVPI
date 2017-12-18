package com.snagajob.mvpireference.views.login.models

import com.snagajob.mvpireference.basemodels.Result

sealed class LoginResult : Result() {
    class LoginSuccess(val customers: List<String>) : LoginResult()
    class BadUsername : LoginResult()
    class BadPassword : LoginResult()
    class NetworkFailure : LoginResult()
    class UnknownError : LoginResult()
    class RequestInProgress : LoginResult()
    class SnackbarDismissed : LoginResult()
}