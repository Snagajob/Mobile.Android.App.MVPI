package com.snagajob.mvpireference.basearchitecture

import android.content.Context
import android.support.v4.content.Loader
import com.snagajob.mvpireference.R
import com.snagajob.mvpireference.basemodels.Action
import com.snagajob.mvpireference.basemodels.Event
import com.snagajob.mvpireference.basemodels.Result
import com.snagajob.mvpireference.basemodels.State

class PresenterLoader<E : Event, A : Action, R : Result, S : State>
(context: Context, private val factory: PresenterFactory<Presenter<E, A, R, S>>) : Loader<Presenter<E, A, R, S>>(context) {

    var presenter: Presenter<E, A, R, S>? = null

    override fun onStartLoading() {
        super.onStartLoading()

        if (presenter != null) {
            deliverResult(presenter)
            return
        }

        forceLoad()
    }

    override fun forceLoad() {
        super.forceLoad()

        presenter = factory.create()
        deliverResult(presenter)
    }

    override fun onReset() {
        super.onReset()

        presenter = null
    }
}