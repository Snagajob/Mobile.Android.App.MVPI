package com.snagajob.mvpireference.views.startup

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.snagajob.mvpireference.BuildConfig
import com.snagajob.mvpireference.R

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        val loadingFragment = LoadingFragment()
        val transaction = supportFragmentManager.beginTransaction()

        transaction.add(R.id.fragmentContainer, loadingFragment)
        transaction.commit()
    }

    fun swapFragment() {
        val newFragment = ForceUpdateFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}