package com.snagajob.mvpireference.views.startup

import com.coreyhorn.mvpiframework.architecture.Interactor
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.views.startup.models.ForceUpdateAction
import com.snagajob.mvpireference.views.startup.models.ForceUpdateResult
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers

class ForceUpdateInteractor(actions: Observable<ForceUpdateAction>, val forceUpdateChecker: ForceUpdateChecker): Interactor<ForceUpdateResult>() {

    init {
        actions.compose(ActionToResult())
                .subscribe { results.onNext(it) }


        forceUpdateChecker.forceUpdateResults()
                .compose(ForceUpdateRequirementTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { results.onNext(it) }

        forceUpdateChecker.isForceUpdateRequired()
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
                            forceUpdateChecker.isForceUpdateRequired()
                            ForceUpdateResult.EvaluationInProgress()
                        }
                )
            }
        }
    }

    private class ForceUpdateRequirementTransformer : ObservableTransformer<ForceUpdateChecker.ForceUpdateRequirement, ForceUpdateResult> {
        override fun apply(upstream: Observable<ForceUpdateChecker.ForceUpdateRequirement>): ObservableSource<ForceUpdateResult> {
            return upstream.publish { source ->
                merge<ForceUpdateResult>(
                        source.ofType(ForceUpdateChecker.ForceUpdateRequirement.HardForce::class.java).map { ForceUpdateResult.HardForceRequired() },
                        source.ofType(ForceUpdateChecker.ForceUpdateRequirement.SoftForce::class.java).map { ForceUpdateResult.SoftForceRequired() },
                        source.ofType(ForceUpdateChecker.ForceUpdateRequirement.None::class.java).map { ForceUpdateResult.NoForceRequired() }
                )
            }
        }
    }
}