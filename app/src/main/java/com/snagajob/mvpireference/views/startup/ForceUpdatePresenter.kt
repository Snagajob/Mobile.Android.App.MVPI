package com.snagajob.mvpireference.views.startup

import com.coreyhorn.mvpiframework.architecture.Presenter
import com.coreyhorn.mvpiframework.disposeWith
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.services.startup.ForceUpdateService
import com.snagajob.mvpireference.views.startup.models.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class ForceUpdatePresenter : Presenter<ForceUpdateEvent, ForceUpdateAction, ForceUpdateResult, ForceUpdateState>() {

    override fun attachEventStream(events: Observable<ForceUpdateEvent>) {
        super.attachEventStream(events)

        events.compose(EventToActionTransformer())
                .subscribe { actions.onNext(it) }
                .disposeWith(eventDisposables)
    }

    init{
        attachResultStream(ForceUpdateInteractor(actions, ForceUpdateService()).results())
    }

    override fun attachResultStream(results: Observable<ForceUpdateResult>) {
        results.scan(ForceUpdateState.idle(), this::accumulator)
                .subscribe {
                    //Emit the current state
                    states.onNext(it)

                    //Emit Single Live Event follow up
                    if (it.navigationState is NavigationState.NavigateToStore) {
                        states.onNext(it.copy(navigationState = NavigationState.ReturningFromStore()))
                    }
                }
    }

    private class EventToActionTransformer: ObservableTransformer<ForceUpdateEvent, ForceUpdateAction> {
        override fun apply(upstream: Observable<ForceUpdateEvent>): ObservableSource<ForceUpdateAction> {
            return upstream.publish { source ->
                merge<ForceUpdateAction>(
                        source.ofType(ForceUpdateEvent.DismissUpgrade::class.java).map { ForceUpdateAction.DismissUpgrade()},
                        source.ofType(ForceUpdateEvent.AcceptUpgrade::class.java).map { ForceUpdateAction.AcceptUpgrade()},
                        source.ofType(ForceUpdateEvent.CloseApplication::class.java).map { ForceUpdateAction.CloseApplication()},
                        source.ofType(ForceUpdateEvent.ReevaluateConditionsMet::class.java).map { ForceUpdateAction.ReevaluateConditionsMet()}
                )
            }
        }
    }

    private fun accumulator(previousState: ForceUpdateState, result: ForceUpdateResult): ForceUpdateState = when (result) {
        is ForceUpdateResult.HardForceRequired -> previousState.copy(dialogState = DialogState.ShowHardForceUpdate())
        is ForceUpdateResult.SoftForceRequired -> previousState.copy(dialogState = DialogState.ShowSoftForceUpdate())
        is ForceUpdateResult.NoForceRequired -> ForceUpdateState(NavigationState.ContinueInApp(), DialogState.Hidden())
        is ForceUpdateResult.UpgradeAccepted -> ForceUpdateState(NavigationState.NavigateToStore(), DialogState.Hidden())
        is ForceUpdateResult.ApplicationClosed -> ForceUpdateState(NavigationState.CloseApp(), DialogState.Hidden())
        is ForceUpdateResult.DialogDismissed -> ForceUpdateState(NavigationState.ContinueInApp(), DialogState.Hidden())
        is ForceUpdateResult.EvaluationInProgress -> ForceUpdateState.idle()
    }
}