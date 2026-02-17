package com.dush1729.cfseeker.crashlytics

object DummyCrashlyticsService : CrashlyticsService {
    override fun logException(exception: Throwable) {
        println("Crashlytics: Exception: ${exception.message}")
    }

    override fun log(message: String) {
        println("Crashlytics: $message")
    }

    override fun setCustomKey(key: String, value: String) {}
    override fun setCustomKey(key: String, value: Int) {}
    override fun setCustomKey(key: String, value: Boolean) {}
}
