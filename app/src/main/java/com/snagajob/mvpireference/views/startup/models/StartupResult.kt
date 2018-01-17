package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Result

sealed class StartupResult : Result() {
    class HardForceRequired : StartupResult()
    class SoftForceRequired : StartupResult()
    class NoForceRequired : StartupResult()
    class DialogDismissed : StartupResult()
    class UpgradeAccepted : StartupResult()
    class ApplicationClosed : StartupResult()
    class EvaluationInProgress : StartupResult()
}