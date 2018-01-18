package com.snagajob.mvpireference.views.startup

import com.coreyhorn.mvpiframework.architecture.Interactor
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.services.startup.RemoteConfigService
import com.snagajob.mvpireference.views.startup.models.LoadingModel
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers

class LoadingInteractor(remoteConfigService: RemoteConfigService): Interactor<LoadingModel.LoadingResult>() {

    init {
        remoteConfigService.remoteConfigResults()
                .compose((RemoteConfigTransformer()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { results.onNext(it) }

        remoteConfigService.fetchFirebaseConfig()
    }

    private class RemoteConfigTransformer : ObservableTransformer<RemoteConfigService.RemoteConfigResult, LoadingModel.LoadingResult> {
        override fun apply(upstream: Observable<RemoteConfigService.RemoteConfigResult>): ObservableSource<LoadingModel.LoadingResult> {
            return upstream.publish { source ->
                merge<LoadingModel.LoadingResult>(
                        source.ofType(RemoteConfigService.RemoteConfigResult.Failure::class.java).map { LoadingModel.LoadingResult.RemoteConfigFetchFailure() },
                        source.ofType(RemoteConfigService.RemoteConfigResult.Success::class.java).map { LoadingModel.LoadingResult.RemoteConfigFetchSuccess() }
                 )
            }
        }
    }
}