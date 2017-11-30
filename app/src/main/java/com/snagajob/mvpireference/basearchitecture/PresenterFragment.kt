package com.snagajob.mvpireference.basearchitecture

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import com.snagajob.mvpireference.basemodels.Action
import com.snagajob.mvpireference.basemodels.Event
import com.snagajob.mvpireference.basemodels.Result
import com.snagajob.mvpireference.basemodels.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

abstract class PresenterFragment<E : Event, R : Result, A : Action, S : State> : Fragment(), PresenterView<E, R, A, S> {

    override val events: PublishSubject<E> = PublishSubject.create()

    override var presenter: Presenter<E, R, A, S>? = null
    override var disposables = CompositeDisposable()
    override var attachAttempted = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initializePresenter(loaderManager)
    }

    override fun onResume() {
        super.onResume()
        attachStream()
        setupViewBindings()
    }

    override fun onPause() {
        detachStream()
        super.onPause()
    }

    override fun initializeLoader(loaderCallbacks: LoaderManager.LoaderCallbacks<Presenter<E, R, A, S>>) {
        loaderManager.initLoader(loaderId(), null, loaderCallbacks)
    }
}