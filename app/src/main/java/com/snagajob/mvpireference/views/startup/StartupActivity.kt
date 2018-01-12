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

                        if (isForceUpdateRequired()) {
                            forceUpgrade()
                        }
                        else {
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                    } else {
                       startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
    }

    private fun isForceUpdateRequired() : Boolean
    {
        // in the unlikely scenario that you want to force upgrade a version of an app that is frozen for older OS versions,
        // you can do so by setting min OS version down in Firebase, and if the client version is less than an excepted version, they'll be forced.
        // Otherwise, this min os version should match the min os version of the current app version
        val forceUpgradeMinTargetOsVersion = mFirebaseRemoteConfig.getLong(FORCE_UPGRADE_MIN_TARGET_OS_VERSION).toInt()
        val forceUpgradeCurrentAppVersionCode = mFirebaseRemoteConfig.getLong(FORCE_UPGRADE_CURRENT_APP_VERSION_CODE).toInt()
        val forceUpgradeActive = mFirebaseRemoteConfig.getBoolean(FORCE_UPGRADE_ACTIVE)
        val forceUpgradeExceptionAppVersionCodes = mFirebaseRemoteConfig.getString(FORCE_UPGRADE_EXCEPTED_APP_VERSION_CODES).toListOfInts()

        return (forceUpgradeActive
                && forceUpgradeCurrentAppVersionCode > BuildConfig.VERSION_CODE
                && (Build.VERSION.SDK_INT >= forceUpgradeMinTargetOsVersion)
                || !forceUpgradeExceptionAppVersionCodes.contains(BuildConfig.VERSION_CODE))
    }


    private fun forceUpgrade()
    {
        val dialog = AlertDialog.Builder(this)
                // TODO Update all copy and add to strings.xml
                .setTitle("New version required")
                .setMessage("Please, update app to new version to continue reposting.")
                .setPositiveButton("Update",
                        { dialog, _ -> redirectToStore() }).setNegativeButton("Close App",
                        { dialog, _ -> finish() }).create()
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