package com.dush1729.cfseeker.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosPlatformActions : PlatformActions {
    override fun openUrl(url: String) {
        val nsUrl = NSURL(string = url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl)
    }

    override fun openPlayStore(packageName: String) {
        // No Play Store on iOS - open App Store or no-op
    }

    override fun shareText(text: String, title: String) {
        // Sharing requires a UIViewController reference, handled at UI level if needed
    }
}
