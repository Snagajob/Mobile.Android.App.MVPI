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

    var numCores = Runtime.getRuntime().availableProcessors()
    var executor = ThreadPoolExecutor(numCores * 2, numCores * 2,
            60L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>())

    fun fetchFirebaseConfig() {
        var mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
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
                .addOnCompleteListener( executor, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mFirebaseRemoteConfig.activateFetched()
                        remoteConfigResults.onNext(RemoteConfigResult.Success())
                    } else {
                        remoteConfigResults.onNext(RemoteConfigResult.Failure())
                    }
                })
    }
}

