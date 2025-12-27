package com.dush1729.cfseeker.crashlytics

interface CrashlyticsService {
    fun logException(exception: Throwable)
    fun log(message: String)
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Int)
    fun setCustomKey(key: String, value: Boolean)
}
