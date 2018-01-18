package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Result

sealed class ForceUpdateResult : Result() {
    class HardForceRequired : ForceUpdateResult()
    class SoftForceRequired : ForceUpdateResult()
    class NoForceRequired : ForceUpdateResult()
    class DialogDismissed : ForceUpdateResult()
    class UpgradeAccepted : ForceUpdateResult()
    class ApplicationClosed : ForceUpdateResult()
    class EvaluationInProgress : ForceUpdateResult()
}