package com.dush1729.cfseeker.platform

class IosPlatformActions : PlatformActions {
    override fun openPlayStore(packageName: String) {
        // No Play Store on iOS - open App Store or no-op
    }

    override fun shareText(text: String, title: String) {
        // Sharing requires a UIViewController reference, handled at UI level if needed
    }
}
