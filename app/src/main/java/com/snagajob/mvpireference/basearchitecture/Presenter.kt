package com.snagajob.mvpireference.basearchitecture

import com.snagajob.mvpireference.basemodels.Action
import com.snagajob.mvpireference.basemodels.Event
import com.snagajob.mvpireference.basemodels.Result
import com.snagajob.mvpireference.basemodels.State
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class Presenter<E : Event, R : Result, A : Action, S : State> {

    protected val actions: PublishSubject<A> = PublishSubject.create()
    protected val states: BehaviorSubject<S> = BehaviorSubject.create()

    protected var eventDisposables = CompositeDisposable()

    fun actions(): Observable<A> {
        return actions
    }

    fun states(): Observable<out S> {
        return states
                .observeOn(AndroidSchedulers.mainThread())
    }

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



