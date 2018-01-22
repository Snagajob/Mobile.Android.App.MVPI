package com.snagajob.mvpireference.views.startup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coreyhorn.mvpiframework.architecture.Presenter
import com.coreyhorn.mvpiframework.architecture.PresenterFactory
import com.coreyhorn.mvpiframework.architecture.PresenterFragment
import com.snagajob.mvpireference.LOADER_ID_LOADING_FRAGMENT
import com.snagajob.mvpireference.R
import com.snagajob.mvpireference.views.startup.models.*

class LoadingFragment : PresenterFragment<LoadingModel.LoadingEvent, LoadingModel.LoadingAction, LoadingModel.LoadingResult, LoadingModel.LoadingState>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_loading, container, false)

    override fun loaderId(): Int = LOADER_ID_LOADING_FRAGMENT

    override fun setupViewBindings() {
    }

    override fun presenterFactory(): PresenterFactory<Presenter<LoadingModel.LoadingEvent, LoadingModel.LoadingAction, LoadingModel.LoadingResult, LoadingModel.LoadingState>> {
        return object: PresenterFactory<LoadingPresenter>() {
            override fun create() = LoadingPresenter()
        }
    }
    
    override fun renderViewState(state: LoadingModel.LoadingState) {
        Log.d("mvpistate", state.toString())
        if (state.remoteConfigFetchState !is LoadingModel.RemoteConfigFetchState.InProgress) {
            (activity as StartupActivity).swapFragment()
        }
    }
}