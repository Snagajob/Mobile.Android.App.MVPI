package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.State

data class ForceUpdateState(val navigationState: NavigationState,
                            val dialogState: DialogState): State() {

    companion object {
        fun idle() = ForceUpdateState(NavigationState.EvaluationInProgress(), DialogState.Hidden())
    }
}

sealed class DialogState {
    class Hidden: DialogState()
    class ShowHardForceUpdate: DialogState()
    class ShowSoftForceUpdate: DialogState()
}

sealed class NavigationState {
    class EvaluationInProgress: NavigationState()
    class NavigateToStore: NavigationState()
    class ContinueInApp: NavigationState()
    class CloseApp: NavigationState()
    class ReturningFromStore : NavigationState()
}