package com.snagajob.mvpireference.views.login.models

import com.coreyhorn.mvpiframework.basemodels.Action

sealed class LoginAction: Action() {
    data class LoginAttempt(val username: String, val password: String): LoginAction()
    class SnackbarDismiss: LoginAction()
}