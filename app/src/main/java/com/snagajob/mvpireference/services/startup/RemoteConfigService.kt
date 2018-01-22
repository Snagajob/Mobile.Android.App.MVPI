package com.snagajob.mvpireference.services.startup

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.snagajob.mvpireference.BuildConfig
import com.snagajob.mvpireference.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class RemoteConfigService {

    private val remoteConfigResults: BehaviorSubject<RemoteConfigResult> = BehaviorSubject.create()

    fun remoteConfigResults(): Observable<RemoteConfigResult> = remoteConfigResults

    sealed class RemoteConfigResult {
        class Success : RemoteConfigResult()
        class Failure : RemoteConfigResult()
    }

    fun fetchFirebaseConfig() {
        var firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        firebaseRemoteConfig.setConfigSettings(configSettings)
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)

        var cacheExpiration: Long = 3600 // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (firebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }

        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener( { task ->
                    if (task.isSuccessful) {
                        firebaseRemoteConfig.activateFetched()
                        remoteConfigResults.onNext(RemoteConfigResult.Success())
                    } else {
                        remoteConfigResults.onNext(RemoteConfigResult.Failure())
                    }
                })
    }
}

