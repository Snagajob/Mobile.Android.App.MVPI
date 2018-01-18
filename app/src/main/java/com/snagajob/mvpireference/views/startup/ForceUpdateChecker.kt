package com.snagajob.mvpireference.views.startup

import android.os.Build
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.snagajob.mvpireference.BuildConfig
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


class ForceUpdateChecker {
    private val FORCE_UPGRADE_MIN_TARGET_OS_VERSION = "forceUpgradeMinTargetOsVersion"
    private val FORCE_UPGRADE_CURRENT_APP_VERSION_CODE = "forceUpgradeCurrentAppVersionCode"
    private val FORCE_UPGRADE_ACTIVE = "forceUpgradeActive"
    private val FORCE_UPGRADE_IS_SOFT = "forceUpgradeIsSoft"
    private val FORCE_UPGRADE_EXCEPTED_APP_VERSION_CODES = "forceUpgradeExceptedAppVersionCodes"

    private val forceUpdateResults : BehaviorSubject<ForceUpdateRequirement> = BehaviorSubject.create()

    fun forceUpdateResults() : Observable<ForceUpdateRequirement> = forceUpdateResults

    sealed class ForceUpdateRequirement {
        class HardForce : ForceUpdateRequirement()
        class SoftForce : ForceUpdateRequirement()
        class None : ForceUpdateRequirement()
    }

    fun isForceUpdateRequired() {

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

        // based upon the firebase parameters, this logic determines whether a force upgrade is required as follows:
        // 1. is forceUpgradeActive? it must be or it does not happen.
        // 2. is forceUpgradeCurrentAppVersionCode higher than the version code of this build? it must be or it does not happen.
        // 3. is the forceUpgradeMinTargetOsVersion >= this devices os version OR does the list of excepted
        // versions (forceUpgradeExceptionAppVersionCodes) contain this app version? if so, force upgrade.
        // the effect of #3 is that any old device on a frozen version will be forced up to the latest frozen version for their OS
        // while any device that is higher than our min OS will be force upgraded to our latest store release.
        // 4. finally if we've determined that a force upgrade is required, we evaluate forceUpgradeIsSoft, and
        // then return whether a HardForce, SoftForce, or None is required.

        val forceUpgradeActive = firebaseRemoteConfig.getBoolean(FORCE_UPGRADE_ACTIVE)
        val forceUpgradeCurrentAppVersionCode = firebaseRemoteConfig.getLong(FORCE_UPGRADE_CURRENT_APP_VERSION_CODE).toInt()
        val forceUpgradeMinTargetOsVersion = firebaseRemoteConfig.getLong(FORCE_UPGRADE_MIN_TARGET_OS_VERSION).toInt()
        val forceUpgradeExceptionAppVersionCodes = firebaseRemoteConfig.getString(FORCE_UPGRADE_EXCEPTED_APP_VERSION_CODES).toListOfInts()
        val forceUpgradeIsSoft = firebaseRemoteConfig.getBoolean(FORCE_UPGRADE_IS_SOFT)

        val forceRequired = (forceUpgradeActive
                && forceUpgradeCurrentAppVersionCode > BuildConfig.VERSION_CODE
                && (Build.VERSION.SDK_INT >= forceUpgradeMinTargetOsVersion
                || !forceUpgradeExceptionAppVersionCodes.contains(BuildConfig.VERSION_CODE)))

        var forceUpdateRequirement = when (forceRequired) {
            true -> when (forceUpgradeIsSoft) {
                true -> ForceUpdateRequirement.SoftForce()
                false -> ForceUpdateRequirement.HardForce()
            }
            false -> ForceUpdateRequirement.None()
        }

        forceUpdateResults.onNext(forceUpdateRequirement)
    }

    // region extension methods

    /**
     * Parses out a list of ints from a comma-delimited string
     */
    private fun String?.toListOfInts(): List<Int> {
        var result = ArrayList<Int>()
        if (this != null && !this.isEmpty()) {
            try {
                result.addAll(this
                        .replace(" ", "")
                        .split(",")
                        .map { it.toInt() }
                )
            } catch (ex: Exception) {
                // TODO: should log to firebase
                Log.d("Parsing Version Codes", ex.toString())
            }
        }
        return result
    }

    // endregion
}