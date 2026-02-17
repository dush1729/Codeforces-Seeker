package com.dush1729.cfseeker.platform

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.dush1729.cfseeker.BuildConfig
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    override fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            Firebase.crashlytics.setCustomKey("url", url)
            Firebase.crashlytics.log("PlatformActions: Failed to open URL: $url - ${e.message}")
        }
    }

    override fun openPlayStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrl("https://play.google.com/store/apps/details?id=$packageName")
        }
    }

    override fun shareText(text: String, title: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooserIntent = Intent.createChooser(shareIntent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }
}

actual val appVersionName: String = BuildConfig.VERSION_NAME
