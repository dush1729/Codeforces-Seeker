package com.dush1729.cfseeker.platform

interface PlatformActions {
    fun openPlayStore(packageName: String = "com.dush1729.cfseeker")
    fun shareText(text: String, title: String = "Share via")
}

expect val appVersionName: String

expect val isIos: Boolean
