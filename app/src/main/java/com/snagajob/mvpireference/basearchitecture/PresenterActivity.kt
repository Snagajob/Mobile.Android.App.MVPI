package com.snagajob.mvpireference.basearchitecture

import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v7.app.AppCompatActivity
import com.snagajob.mvpireference.basemodels.Action
import com.snagajob.mvpireference.basemodels.Event
import com.snagajob.mvpireference.basemodels.Result
import com.snagajob.mvpireference.basemodels.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

abstract class PresenterActivity<P : Presenter<E, R, A, S>, E : Event, R : Result, A : Action, S : State> : AppCompatActivity(), PresenterView<P, E, R, A, S> {

    override val events: PublishSubject<E> = PublishSubject.create()

    override var presenter: P? = null
    override var disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializePresenter(supportLoaderManager)
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

    override fun initializeLoader(loaderCallbacks: LoaderManager.LoaderCallbacks<P>) {
        supportLoaderManager.initLoader(loaderId(), null, loaderCallbacks)
    }
}