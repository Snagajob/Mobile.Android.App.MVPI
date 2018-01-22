package com.snagajob.mvpireference.views.startup

import com.coreyhorn.mvpiframework.architecture.Presenter
import com.snagajob.mvpireference.services.startup.RemoteConfigService
import com.snagajob.mvpireference.views.startup.models.*
import io.reactivex.Observable

class LoadingPresenter : Presenter<LoadingModel.LoadingEvent, LoadingModel.LoadingAction, LoadingModel.LoadingResult, LoadingModel.LoadingState>() {

    init { attachResultStream(LoadingInteractor(RemoteConfigService()).results()) }

    override fun attachResultStream(results: Observable<LoadingModel.LoadingResult>) {
        results.scan(LoadingModel.LoadingState.loading(), this::accumulator)
                .subscribe(states::onNext)
    }

    private fun accumulator(previousState: LoadingModel.LoadingState, result: LoadingModel.LoadingResult): LoadingModel.LoadingState = when (result) {
        is LoadingModel.LoadingResult.LoadingInProgress -> LoadingModel.LoadingState.loading()
        is LoadingModel.LoadingResult.RemoteConfigFetchSuccess -> LoadingModel.LoadingState(false, LoadingModel.RemoteConfigFetchState.Success())
        is LoadingModel.LoadingResult.RemoteConfigFetchFailure -> LoadingModel.LoadingState(false, LoadingModel.RemoteConfigFetchState.Failure())
    }
}