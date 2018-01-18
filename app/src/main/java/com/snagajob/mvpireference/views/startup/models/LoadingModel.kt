package com.snagajob.mvpireference.views.startup.models

import com.coreyhorn.mvpiframework.basemodels.Action
import com.coreyhorn.mvpiframework.basemodels.Event
import com.coreyhorn.mvpiframework.basemodels.Result
import com.coreyhorn.mvpiframework.basemodels.State

class LoadingModel {

    sealed class LoadingEvent : Event() {}

    sealed class LoadingAction : Action() {}

    sealed class LoadingResult : Result() {
        class LoadingInProgress : LoadingResult()
        class RemoteConfigFetchSuccess : LoadingResult()
        class RemoteConfigFetchFailure : LoadingResult()
    }

    data class LoadingState(val progressShowing: Boolean,
                            val remoteConfigFetchState: RemoteConfigFetchState): State() {

        companion object {
            fun idle() = LoadingState(true, RemoteConfigFetchState.InProgress())
        }
    }

    sealed class RemoteConfigFetchState {
        class InProgress: RemoteConfigFetchState()
        class Success: RemoteConfigFetchState()
        class Failure: RemoteConfigFetchState()
    }
}
