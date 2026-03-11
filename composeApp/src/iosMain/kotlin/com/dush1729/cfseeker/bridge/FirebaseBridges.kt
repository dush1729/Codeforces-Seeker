package com.dush1729.cfseeker.bridge

/**
 * ObjC-friendly bridge interfaces for Firebase services.
 * These avoid Kotlin-specific constructs (overloads, suspend, Throwable)
 * that don't translate cleanly to ObjC/Swift.
 */

interface AnalyticsBridge {
    fun logEvent(name: String, params: Map<String, Any>)
}

interface FetchCallback {
    fun onResult(success: Boolean)
}

interface CrashlyticsBridge {
    fun recordException(message: String, stackTrace: String)
    fun log(message: String)
    fun setCustomKeyString(key: String, value: String)
    fun setCustomKeyInt(key: String, value: Int)
    fun setCustomKeyBool(key: String, value: Boolean)
}

interface RemoteConfigBridge {
    fun fetchAndActivate(callback: FetchCallback)
    fun getString(key: String): String
    fun getBoolean(key: String): Boolean
    fun getLong(key: String): Long
    fun getDouble(key: String): Double
}
