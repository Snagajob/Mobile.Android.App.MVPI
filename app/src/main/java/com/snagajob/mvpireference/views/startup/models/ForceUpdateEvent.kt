package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Event

sealed class ForceUpdateEvent : Event() {
    class AcceptUpgrade: ForceUpdateEvent()
    class DismissUpgrade: ForceUpdateEvent()
    class CloseApplication: ForceUpdateEvent()
    class ReevaluateConditionsMet: ForceUpdateEvent()
}
