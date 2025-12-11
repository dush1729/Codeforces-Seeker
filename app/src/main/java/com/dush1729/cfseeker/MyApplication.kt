package com.dush1729.cfseeker

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        analytics = Firebase.analytics
    }
}