package com.dush1729.cfseeker.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashlyticsService(
    private val crashlytics: FirebaseCrashlytics
) : CrashlyticsService {

    override fun logException(exception: Throwable) {
        crashlytics.recordException(exception)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }
}
