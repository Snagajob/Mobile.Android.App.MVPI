package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Action

sealed class ForceUpdateAction : Action() {
    class AcceptUpgrade: ForceUpdateAction()
    class DismissUpgrade: ForceUpdateAction()
    class CloseApplication: ForceUpdateAction()
    class ReevaluateConditionsMet: ForceUpdateAction()
}