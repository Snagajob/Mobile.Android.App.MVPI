package com.snagajob.mvpireference.basearchitecture

import com.snagajob.mvpireference.basemodels.Action
import com.snagajob.mvpireference.basemodels.Event
import com.snagajob.mvpireference.basemodels.Result
import com.snagajob.mvpireference.basemodels.State
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class Presenter<E : Event, A : Action, R: Result, S : State> {

    protected val actions: PublishSubject<A> = PublishSubject.create()
    protected val states: BehaviorSubject<S> = BehaviorSubject.create()

    protected var eventDisposables = CompositeDisposable()

    fun actions(): Observable<A> = actions

    fun states(): Observable<S> = states

    open fun attachEventStream(events: Observable<E>) {
        eventDisposables.clear()
        eventDisposables = CompositeDisposable()
    }

    fun detachEventStream() {
        eventDisposables.clear()
        eventDisposables = CompositeDisposable()
    }

    abstract fun attachResultStream(results: Observable<R>)

}



