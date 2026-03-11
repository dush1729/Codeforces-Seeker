package com.dush1729.cfseeker.analytics

import com.dush1729.cfseeker.utils.isPowerOfTwo
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import javax.inject.Inject

class FirebaseAnalyticsService @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : AnalyticsService {
    // Log launch event on every power of 2 (1, 2, 4, 8, 16, ...)
    override fun logMilestoneLaunch(launchCount: Int) {
        val isMilestone = launchCount.isPowerOfTwo()
        if (isMilestone) {
            firebaseAnalytics.logEvent("app_launch") {
                param("launch_count", launchCount.toLong())
            }
        }
    }

    override fun logUserAdded(handle: String) {
        firebaseAnalytics.logEvent("user_added") {
            param("handle", handle)
        }
    }

    override fun logUserAddFailed(handle: String, error: String) {
        firebaseAnalytics.logEvent("user_add_failed") {
            param("handle", handle)
            param("error", error)
        }
    }

    override fun logUserDeleted(handle: String) {
        firebaseAnalytics.logEvent("user_deleted") {
            param("handle", handle)
        }
    }

    override fun logBulkSyncStarted() {
        firebaseAnalytics.logEvent("bulk_sync_started") {}
    }

    override fun logBulkSyncCompleted(
        durationMs: Long,
        userCount: Int,
        successCount: Int,
        failureCount: Int
    ) {
        firebaseAnalytics.logEvent("bulk_sync_completed") {
            param("duration_ms", durationMs)
            param("user_count", userCount.toLong())
            param("success_count", successCount.toLong())
            param("failure_count", failureCount.toLong())
            param("success_rate", if (userCount > 0) (successCount.toDouble() / userCount * 100) else 100.0)
        }
    }

    override fun logSortChanged(sortOption: String) {
        firebaseAnalytics.logEvent("sort_changed") {
            param("sort_option", sortOption)
        }
    }

    override fun logScreenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    override fun logFeedbackOpened() {
        firebaseAnalytics.logEvent("feedback_opened") {}
    }

    override fun logGitHubOpened() {
        firebaseAnalytics.logEvent("github_opened") {}
    }

    override fun logPlayStoreOpened(source: String) {
        firebaseAnalytics.logEvent("play_store_opened") {
            param("source", source)
        }
    }

    override fun logAppShared(source: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param("source", source)
        }
    }

    override fun logUserSyncedFromDetails(handle: String) {
        firebaseAnalytics.logEvent("user_synced_from_details") {
            param("handle", handle)
        }
    }
}
