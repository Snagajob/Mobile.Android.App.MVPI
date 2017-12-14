package com.snagajob.mvpireference.basearchitecture

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import com.snagajob.mvpireference.basemodels.Action
import com.snagajob.mvpireference.basemodels.Event
import com.snagajob.mvpireference.basemodels.Result
import com.snagajob.mvpireference.basemodels.State
import com.snagajob.mvpireference.disposeWith
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

interface PresenterView<E : Event, A : Action, R : Result, S : State> {

    val events: PublishSubject<E>

    var presenter: Presenter<E, A, R, S>?
    var disposables: CompositeDisposable
    var attachAttempted: Boolean

    val loaderCallbacks: LoaderManager.LoaderCallbacks<Presenter<E, A, R, S>>
        get() = object : LoaderManager.LoaderCallbacks<Presenter<E, A, R, S>> {
            override fun onLoaderReset(loader: Loader<Presenter<E, A, R, S>>?) {
                presenter = null
            }

            override fun onLoadFinished(loader: Loader<Presenter<E, A, R, S>>?, data: Presenter<E, A, R, S>) {
                presenter = data
                onPresenterAvailable(data)
            }

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Presenter<E, A, R, S>> = PresenterLoader(getContext()!!, presenterFactory())
        }

    fun initializePresenter(loaderManager: LoaderManager) {
        @Suppress("UNCHECKED_CAST")
        val loader = loaderManager.getLoader<Presenter<E, A, R, S>>(loaderId()) as? PresenterLoader<E, A, R, S>

        if (loader == null) {
            initializeLoader(loaderCallbacks)
        } else {
            loader.presenter?.let {
                presenter = it
                onPresenterAvailable(it)
            }
        }
    }

    fun attachStream() {
        attachAttempted = true
        presenter?.let {
            it.attachEventStream(events)
            it.states()
                    .subscribe { renderViewStateOnMainThread(it) }
                    .disposeWith(disposables)
        }
    }

    fun detachStream() {
        attachAttempted = false
        disposables.clear()
        disposables = CompositeDisposable()
        presenter?.detachEventStream()
    }

    fun onPresenterAvailable(presenter: Presenter<E, A, R, S>) {
        if (attachAttempted) {
            attachStream()
        }
    }

    private fun renderViewStateOnMainThread(state: S) {
        val mainHandler = Handler(Looper.getMainLooper())
        val myRunnable = { renderViewState(state) }
        mainHandler.post(myRunnable)
    }

    fun initializeLoader(loaderCallbacks: LoaderManager.LoaderCallbacks<Presenter<E, A, R, S>>)
    fun getContext(): Context?
    fun loaderId(): Int
    fun presenterFactory(): PresenterFactory<Presenter<E, A, R, S>>
    fun renderViewState(state: S)
    fun setupViewBindings()
}