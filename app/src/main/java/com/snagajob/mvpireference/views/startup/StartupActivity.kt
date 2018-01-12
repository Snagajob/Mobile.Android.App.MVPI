package com.snagajob.mvpireference.views.startup

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.snagajob.mvpireference.BuildConfig
import com.snagajob.mvpireference.R
import android.support.v7.app.AlertDialog
import android.content.Intent
import android.net.Uri
import com.snagajob.mvpireference.views.login.LoginActivity

class StartupActivity : AppCompatActivity() {

    private val FORCE_UPGRADE_MIN_TARGET_OS_VERSION = "forceUpgradeMinTargetOsVersion"
    private val FORCE_UPGRADE_CURRENT_APP_VERSION_CODE = "forceUpgradeCurrentAppVersionCode"
    private val FORCE_UPGRADE_ACTIVE = "forceUpgradeActive"
    private val FORCE_UPGRADE_IS_SOFT = "forceUpgradeIsSoft"
    private val FORCE_UPGRADE_EXCEPTED_APP_VERSION_CODES = "forceUpgradeExceptedAppVersionCodes"

    private lateinit var mFirebaseRemoteConfig : FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        fetchFirebaseConfig()
    }

    private fun fetchFirebaseConfig()
    {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        mFirebaseRemoteConfig.setConfigSettings(configSettings)
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)

        var cacheExpiration: Long = 3600 // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        mFirebaseRemoteConfig.activateFetched()

                        when (isForceUpdateRequired()) {
                            is ForceUpdateRequirement.HardForce -> { forceUpgrade(ForceUpdateRequirement.HardForce()) }
                            is ForceUpdateRequirement.SoftForce -> { forceUpgrade(ForceUpdateRequirement.SoftForce()) }
                            is ForceUpdateRequirement.None -> { continueToExpectedActivity() }
                        }
                    } else {
                       continueToExpectedActivity()
                    }
                }
    }


    sealed class ForceUpdateRequirement {
        class HardForce : ForceUpdateRequirement()
        class SoftForce : ForceUpdateRequirement()
        class None: ForceUpdateRequirement()
    }

    private fun isForceUpdateRequired() : ForceUpdateRequirement
    {
        // based upon the firebase parameters, this logic determines whether a force upgrade is required as follows:
        // 1. is forceUpgradeActive? it must be or it does not happen.
        // 2. is forceUpgradeCurrentAppVersionCode higher than the version code of this build? it must be or it does not happen.
        // 3. is the forceUpgradeMinTargetOsVersion >= this devices os version OR does the list of excepted
        // versions (forceUpgradeExceptionAppVersionCodes) contain this app version? if so, force upgrade.
        // the effect of #3 is that any old device on a frozen version will be forced up to the latest frozen version for their OS
        // while any device that is higher than our min OS will be force upgraded to our latest store release.
        // 4. finally if we've determined that a force upgrade is required, we evaluate forceUpgradeIsSoft, and
        // then return whether a HardForce, SoftForce, or None is required.

        val forceUpgradeActive = mFirebaseRemoteConfig.getBoolean(FORCE_UPGRADE_ACTIVE)
        val forceUpgradeCurrentAppVersionCode = mFirebaseRemoteConfig.getLong(FORCE_UPGRADE_CURRENT_APP_VERSION_CODE).toInt()
        val forceUpgradeMinTargetOsVersion = mFirebaseRemoteConfig.getLong(FORCE_UPGRADE_MIN_TARGET_OS_VERSION).toInt()
        val forceUpgradeExceptionAppVersionCodes = mFirebaseRemoteConfig.getString(FORCE_UPGRADE_EXCEPTED_APP_VERSION_CODES).toListOfInts()
        val forceUpgradeIsSoft = mFirebaseRemoteConfig.getBoolean(FORCE_UPGRADE_IS_SOFT)

        val forceRequired = (forceUpgradeActive
                && forceUpgradeCurrentAppVersionCode > BuildConfig.VERSION_CODE
                && (Build.VERSION.SDK_INT >= forceUpgradeMinTargetOsVersion)
                || !forceUpgradeExceptionAppVersionCodes.contains(BuildConfig.VERSION_CODE))

        return when (forceRequired) {
            true ->  when (forceUpgradeIsSoft) {
                true -> ForceUpdateRequirement.SoftForce()
                false -> ForceUpdateRequirement.HardForce()
            }
            false -> ForceUpdateRequirement.None()
        }
    }

    private fun continueToExpectedActivity()
    {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun forceUpgrade(forceUpdateRequirement: ForceUpdateRequirement)
    {
        var titleStringId = 0
        var messageStringId = 0
        var negativeButtonStringId = 0
        var positiveButtonStringId = R.string.update

        when (forceUpdateRequirement)
        {
            is ForceUpdateRequirement.HardForce -> {
                titleStringId = R.string.new_version_required
                messageStringId = R.string.to_continue_usage_need_to_update
                negativeButtonStringId = R.string.close_app

            }
            is ForceUpdateRequirement.SoftForce -> {
                titleStringId = R.string.new_version_recommended
                messageStringId = R.string.please_update_to_latest
                negativeButtonStringId = R.string.dont_update
            }
        }

        val dialog = AlertDialog.Builder(this)
                // TODO Update all copy and add to strings.xml
                .setTitle(titleStringId)
                .setMessage(messageStringId)
                .setPositiveButton(positiveButtonStringId,
                        { dialog, _ -> redirectToStore() }).setNegativeButton(negativeButtonStringId,
                        { dialog, _ -> when (forceUpdateRequirement) {
                            is ForceUpdateRequirement.HardForce -> finish()
                            is ForceUpdateRequirement.SoftForce -> continueToExpectedActivity()
                        } }).create()
        dialog.show()
    }

    private fun redirectToStore()
    {
        val intent = Intent(Intent.ACTION_VIEW)
        // TODO: Change this to our app id
        intent.data = Uri.parse("market://details?id=com.coco.dmv")
        startActivity(intent)
    }


    // region extension methods

    /**
     * Parses out a list of ints from a comma-delimited string
     */
    private fun String?.toListOfInts(): List<Int> {
        var result = ArrayList<Int>()
        if (this != null && !this.isEmpty())
        {
            try {
                result.addAll(this
                        .replace(" ", "")
                        .split(",")
                        .map{ it.toInt()}
                )
            }
            catch(ex : Exception)
            {
                // TODO: should log to firebase
                Log.d("Parsing Version Codes", ex.toString())
            }
        }
        return result
    }

    // endregion
}