package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Event

sealed class StartupEvent : Event() {
    class AcceptUpgrade: StartupEvent()
    class DismissUpgrade: StartupEvent()
    class CloseApplication: StartupEvent()
    class ReevaluateConditionsMet: StartupEvent()
}
