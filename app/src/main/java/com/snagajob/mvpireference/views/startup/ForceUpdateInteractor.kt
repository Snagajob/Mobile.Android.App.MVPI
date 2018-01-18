package com.snagajob.mvpireference.views.startup

import com.coreyhorn.mvpiframework.architecture.Interactor
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.services.startup.ForceUpdateService
import com.snagajob.mvpireference.views.startup.models.ForceUpdateAction
import com.snagajob.mvpireference.views.startup.models.ForceUpdateResult
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers

class ForceUpdateInteractor(actions: Observable<ForceUpdateAction>, val forceUpdateService: ForceUpdateService): Interactor<ForceUpdateResult>() {

    init {
        actions.compose(ActionToResult())
                .subscribe { results.onNext(it) }


        forceUpdateService.forceUpdateResults()
                .compose(ForceUpdateRequirementTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { results.onNext(it) }

        forceUpdateService.isForceUpdateRequired()
    }

    private inner class ActionToResult: ObservableTransformer<ForceUpdateAction, ForceUpdateResult> {
        override fun apply(upstream: Observable<ForceUpdateAction>): ObservableSource<ForceUpdateResult> {
            return upstream.publish { source ->
                merge<ForceUpdateResult>(
                        source.ofType(ForceUpdateAction.AcceptUpgrade::class.java).map {
                            ForceUpdateResult.UpgradeAccepted()
                        },
                        source.ofType(ForceUpdateAction.CloseApplication::class.java).map {
                            ForceUpdateResult.ApplicationClosed()
                        },
                        source.ofType(ForceUpdateAction.DismissUpgrade::class.java).map {
                            ForceUpdateResult.DialogDismissed()
                        },
                        source.ofType(ForceUpdateAction.ReevaluateConditionsMet::class.java).map {
                            forceUpdateService.isForceUpdateRequired()
                            ForceUpdateResult.EvaluationInProgress()
                        }
                )
            }
        }
    }

    private class ForceUpdateRequirementTransformer : ObservableTransformer<ForceUpdateService.ForceUpdateRequirement, ForceUpdateResult> {
        override fun apply(upstream: Observable<ForceUpdateService.ForceUpdateRequirement>): ObservableSource<ForceUpdateResult> {
            return upstream.publish { source ->
                merge<ForceUpdateResult>(
                        source.ofType(ForceUpdateService.ForceUpdateRequirement.HardForce::class.java).map { ForceUpdateResult.HardForceRequired() },
                        source.ofType(ForceUpdateService.ForceUpdateRequirement.SoftForce::class.java).map { ForceUpdateResult.SoftForceRequired() },
                        source.ofType(ForceUpdateService.ForceUpdateRequirement.None::class.java).map { ForceUpdateResult.NoForceRequired() }
                )
            }
        }
    }
}