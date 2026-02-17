package com.dush1729.cfseeker.platform

interface PlatformActions {
    fun openUrl(url: String)
    fun openPlayStore(packageName: String = "com.dush1729.cfseeker")
    fun shareText(text: String, title: String = "Share via")
}

expect val appVersionName: String
