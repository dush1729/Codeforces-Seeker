package com.dush1729.cfseeker.platform

interface PlatformActions {
    fun openPlayStore(packageName: String = "com.dush1729.cfseeker")
    fun shareText(text: String, title: String = "Share via")
}

const val appVersionName: String = "5.0"

expect val isIos: Boolean
