package com.snagajob.mvpireference.views.startup

import com.coreyhorn.mvpiframework.architecture.Presenter
import com.coreyhorn.mvpiframework.disposeWith
import com.snagajob.mvpireference.merge
import com.snagajob.mvpireference.views.startup.models.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class StartupPresenter: Presenter<StartupEvent, StartupAction, StartupResult, StartupState>() {

    override fun attachEventStream(events: Observable<StartupEvent>) {
        super.attachEventStream(events)

        events.compose(EventToActionTransformer())
                .subscribe { actions.onNext(it) }
                .disposeWith(eventDisposables)
    }

    init{
        attachResultStream(StartupInteractor(actions, ForceUpdateChecker()).results())
    }

    override fun attachResultStream(results: Observable<StartupResult>) {
        results.scan(StartupState.idle(), this::accumulator)
                .subscribe {
                    //Emit the current state
                    states.onNext(it)

                    //Emit Single Live Event follow up
                    if (it.navigationState is NavigationState.NavigateToStore) {
                        states.onNext(it.copy(navigationState = NavigationState.ReturningFromStore()))
                    }
                }
    }

    private class EventToActionTransformer: ObservableTransformer<StartupEvent, StartupAction> {
        override fun apply(upstream: Observable<StartupEvent>): ObservableSource<StartupAction> {
            return upstream.publish { source ->
                merge<StartupAction>(
                        source.ofType(StartupEvent.DismissUpgrade::class.java).map { StartupAction.DismissUpgrade()},
                        source.ofType(StartupEvent.AcceptUpgrade::class.java).map { StartupAction.AcceptUpgrade()},
                        source.ofType(StartupEvent.CloseApplication::class.java).map { StartupAction.CloseApplication()},
                        source.ofType(StartupEvent.ReevaluateConditionsMet::class.java).map { StartupAction.ReevaluateConditionsMet()}
                )
            }
        }
    }

    private fun accumulator(previousState: StartupState, result: StartupResult): StartupState = when (result) {
        is StartupResult.HardForceRequired -> previousState.copy(dialogState = DialogState.ShowHardForceUpdate())
        is StartupResult.SoftForceRequired -> previousState.copy(dialogState = DialogState.ShowSoftForceUpdate())
        is StartupResult.NoForceRequired -> previousState.copy(prerequisitesMet = true, dialogState = DialogState.Hidden())
        is StartupResult.UpgradeAccepted -> StartupState(false, NavigationState.NavigateToStore(), DialogState.Hidden())
        is StartupResult.ApplicationClosed -> StartupState( false, NavigationState.CloseApp(), DialogState.Hidden())
        is StartupResult.DialogDismissed -> StartupState(true, NavigationState.ContinueInApp(), DialogState.Hidden())
        is StartupResult.EvaluationInProgress -> StartupState.idle()
    }
}