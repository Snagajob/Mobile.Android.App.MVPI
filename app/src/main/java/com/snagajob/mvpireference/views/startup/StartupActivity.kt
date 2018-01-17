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
import android.support.v4.content.ContextCompat.startActivity
import com.snagajob.mvpireference.views.login.LoginActivity

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
    }
}