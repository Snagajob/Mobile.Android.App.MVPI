package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Action

sealed class StartupAction: Action() {
    class AcceptUpgrade: StartupAction()
    class DismissUpgrade: StartupAction()
    class CloseApplication: StartupAction()
    class ReevaluateConditionsMet: StartupAction()
}