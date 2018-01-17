package com.snagajob.mvpireference.views.startup

import com.coreyhorn.mvpiframework.architecture.Interactor
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.views.startup.models.StartupAction
import com.snagajob.mvpireference.views.startup.models.StartupResult
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers

class StartupInteractor(actions: Observable<StartupAction>, val forceUpdateChecker: ForceUpdateChecker): Interactor<StartupResult>() {

    init {
        actions.compose(ActionToResult())
                .subscribe { results.onNext(it) }


        forceUpdateChecker.forceUpdateResults()
                .compose(RemoteConfigServiceResultTransformer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { results.onNext(it) }

        forceUpdateChecker.isForceUpdateRequired()
    }

    private inner class ActionToResult: ObservableTransformer<StartupAction, StartupResult> {
        override fun apply(upstream: Observable<StartupAction>): ObservableSource<StartupResult> {
            return upstream.publish { source ->
                merge<StartupResult>(
                        source.ofType(StartupAction.AcceptUpgrade::class.java).map {
                            StartupResult.UpgradeAccepted()
                        },
                        source.ofType(StartupAction.CloseApplication::class.java).map {
                            StartupResult.ApplicationClosed()
                        },
                        source.ofType(StartupAction.DismissUpgrade::class.java).map {
                            StartupResult.DialogDismissed()
                        },
                        source.ofType(StartupAction.ReevaluateConditionsMet::class.java).map {
                            forceUpdateChecker.isForceUpdateRequired()
                            StartupResult.EvaluationInProgress()
                        }
                )
            }
        }
    }

    private class RemoteConfigServiceResultTransformer: ObservableTransformer<ForceUpdateChecker.ForceUpdateRequirement, StartupResult> {
        override fun apply(upstream: Observable<ForceUpdateChecker.ForceUpdateRequirement>): ObservableSource<StartupResult> {
            return upstream.publish { source ->
                merge<StartupResult>(
                        source.ofType(ForceUpdateChecker.ForceUpdateRequirement.HardForce::class.java).map { StartupResult.HardForceRequired() },
                        source.ofType(ForceUpdateChecker.ForceUpdateRequirement.SoftForce::class.java).map { StartupResult.SoftForceRequired() },
                        source.ofType(ForceUpdateChecker.ForceUpdateRequirement.None::class.java).map { StartupResult.NoForceRequired() }
                )
            }
        }
    }
}