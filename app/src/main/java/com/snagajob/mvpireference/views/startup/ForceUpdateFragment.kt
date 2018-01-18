package com.snagajob.mvpireference.views.startup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coreyhorn.mvpiframework.architecture.Presenter
import com.coreyhorn.mvpiframework.architecture.PresenterFactory
import com.coreyhorn.mvpiframework.architecture.PresenterFragment
import com.snagajob.mvpireference.LOADER_ID_STARTUP_FRAGMENT
import com.snagajob.mvpireference.R
import com.snagajob.mvpireference.views.login.LoginActivity
import com.snagajob.mvpireference.views.startup.models.*

class ForceUpdateFragment : PresenterFragment<ForceUpdateEvent, ForceUpdateAction, ForceUpdateResult, ForceUpdateState>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_startup, container, false)

    override fun loaderId(): Int = LOADER_ID_STARTUP_FRAGMENT

    override fun setupViewBindings() {
    }

    override fun presenterFactory(): PresenterFactory<Presenter<ForceUpdateEvent, ForceUpdateAction, ForceUpdateResult, ForceUpdateState>> {
        return object: PresenterFactory<ForceUpdatePresenter>() {
            override fun create() = ForceUpdatePresenter()
        }
    }

    override fun renderViewState(state: ForceUpdateState) {
        Log.d("state", state.toString())
        if (state.prerequisitesMet) {
            continueToExpectedActivity()
        }
        else
        {
            if (state.navigationState is NavigationState.ReturningFromStore)
            {
                events.onNext(ForceUpdateEvent.ReevaluateConditionsMet())
            }

            handleNavigationState(state.navigationState)
            handleDialogState(state.dialogState)
        }
    }

    fun handleNavigationState(navigationState: NavigationState)
    {
        when (navigationState)
        {
            is NavigationState.ContinueInApp -> { continueToExpectedActivity()}
            is NavigationState.NavigateToStore -> { redirectToStore()}
            is NavigationState.CloseApp -> { activity?.finish()}
        }
    }

    fun handleDialogState(dialogState: DialogState)
    {
        var titleStringId = 0
        var messageStringId = 0
        var negativeButtonStringId = 0
        var positiveButtonStringId = R.string.update

        when (dialogState)
        {
            is DialogState.ShowHardForceUpdate -> {
                titleStringId = R.string.new_version_required
                messageStringId = R.string.to_continue_usage_need_to_update
                negativeButtonStringId = R.string.close_app

            }
            is DialogState.ShowSoftForceUpdate -> {
                titleStringId = R.string.new_version_recommended
                messageStringId = R.string.please_update_to_latest
                negativeButtonStringId = R.string.dont_update
            }
        }

        if (dialogState !is DialogState.Hidden) {
            val dialog = AlertDialog.Builder(context!!)
                    .setTitle(titleStringId)
                    .setMessage(messageStringId)
                    .setPositiveButton(positiveButtonStringId,
                            { _, _ -> events.onNext(ForceUpdateEvent.AcceptUpgrade())
                            }).setNegativeButton(negativeButtonStringId,
                    { _, _ ->
                        when (dialogState) {
                            is DialogState.ShowHardForceUpdate -> events.onNext(ForceUpdateEvent.CloseApplication())
                            is DialogState.ShowSoftForceUpdate -> events.onNext(ForceUpdateEvent.DismissUpgrade())
                        }
                    }).create()
            dialog.show()

            dialog.setCanceledOnTouchOutside(false)
        }
    }

    private fun continueToExpectedActivity()
    {
        startActivity(Intent(activity, LoginActivity::class.java))
    }

    private fun redirectToStore()
    {
        val intent = Intent(Intent.ACTION_VIEW)
        // TODO: Change this to desired app id
        intent.data = Uri.parse("market://details?id=com.coco.dmv")
        startActivity(intent)
    }


}
