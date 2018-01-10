package com.snagajob.mvpireference.views.login.models

import com.snagajob.mvpireference.basemodels.Event

sealed class LoginEvent: Event() {
    data class LoginAttempt(val username: String, val password: String): LoginEvent()
    class SnackbarDismiss: LoginEvent()
}