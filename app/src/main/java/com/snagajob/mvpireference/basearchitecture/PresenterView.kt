package com.snagajob.mvpireference.basearchitecture

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import com.snagajob.mvpireference.addTo
import com.snagajob.mvpireference.basemodels.Action
import com.snagajob.mvpireference.basemodels.Event
import com.snagajob.mvpireference.basemodels.Result
import com.snagajob.mvpireference.basemodels.State
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

interface PresenterView<E : Event, R : Result, A : Action, S : State> {

    val events: PublishSubject<E>

    var presenter: Presenter<E, R, A, S>?
    var disposables: CompositeDisposable
    var attachAttempted: Boolean

    val loaderCallbacks: LoaderManager.LoaderCallbacks<Presenter<E, R, A, S>>
        get() = object : LoaderManager.LoaderCallbacks<Presenter<E, R, A, S>> {
            override fun onLoaderReset(loader: Loader<Presenter<E, R, A, S>>?) {
                presenter = null
            }

            override fun onLoadFinished(loader: Loader<Presenter<E, R, A, S>>?, data: Presenter<E, R, A, S>) {
                presenter = data
                onPresenterAvailable(data)
            }

            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Presenter<E, R, A, S>> {
                return PresenterLoader(getContext(), presenterFactory())
            }
        }

    fun initializePresenter(loaderManager: LoaderManager) {
        @Suppress("UNCHECKED_CAST")
        val loader = loaderManager.getLoader<Presenter<E, R, A, S>>(loaderId()) as? PresenterLoader<E, R, A, S>

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
                    .subscribe { renderViewState(it) }
                    .addTo(disposables)
        }
    }

    fun detachStream() {
        attachAttempted = false
        disposables.clear()
        disposables = CompositeDisposable()
        presenter?.detachEventStream()
    }

    fun onPresenterAvailable(presenter: Presenter<E, R, A, S>) {
        if (attachAttempted) {
            attachStream()
        }
    }

    fun initializeLoader(loaderCallbacks: LoaderManager.LoaderCallbacks<Presenter<E, R, A, S>>)
    fun getContext(): Context
    fun loaderId(): Int
    fun presenterFactory(): PresenterFactory<Presenter<E, R, A, S>>
    fun renderViewState(state: S)
    fun setupViewBindings()
}