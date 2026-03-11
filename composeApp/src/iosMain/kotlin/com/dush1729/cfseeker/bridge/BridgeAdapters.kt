package com.dush1729.cfseeker.bridge

import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.utils.isPowerOfTwo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BridgedAnalyticsService(
    private val bridge: AnalyticsBridge
) : AnalyticsService {

    override fun logMilestoneLaunch(launchCount: Int) {
        if (launchCount.isPowerOfTwo()) {
            bridge.logEvent("app_launch", mapOf("launch_count" to launchCount.toLong()))
        }
    }

    override fun logUserAdded(handle: String) {
        bridge.logEvent("user_added", mapOf("handle" to handle))
    }

    override fun logUserAddFailed(handle: String, error: String) {
        bridge.logEvent("user_add_failed", mapOf("handle" to handle, "error" to error))
    }

    override fun logUserDeleted(handle: String) {
        bridge.logEvent("user_deleted", mapOf("handle" to handle))
    }

    override fun logUserSyncedFromDetails(handle: String) {
        bridge.logEvent("user_synced_from_details", mapOf("handle" to handle))
    }

    override fun logBulkSyncStarted() {
        bridge.logEvent("bulk_sync_started", emptyMap())
    }

    override fun logBulkSyncCompleted(
        durationMs: Long,
        userCount: Int,
        successCount: Int,
        failureCount: Int
    ) {
        bridge.logEvent("bulk_sync_completed", mapOf(
            "duration_ms" to durationMs,
            "user_count" to userCount.toLong(),
            "success_count" to successCount.toLong(),
            "failure_count" to failureCount.toLong(),
            "success_rate" to if (userCount > 0) (successCount.toDouble() / userCount * 100) else 100.0
        ))
    }

    override fun logSortChanged(sortOption: String) {
        bridge.logEvent("sort_changed", mapOf("sort_option" to sortOption))
    }

    override fun logScreenView(screenName: String) {
        bridge.logEvent("screen_view", mapOf("screen_name" to screenName))
    }

    override fun logFeedbackOpened() {
        bridge.logEvent("feedback_opened", emptyMap())
    }

    override fun logGitHubOpened() {
        bridge.logEvent("github_opened", emptyMap())
    }

    override fun logPlayStoreOpened(source: String) {
        bridge.logEvent("play_store_opened", mapOf("source" to source))
    }

    override fun logAppShared(source: String) {
        bridge.logEvent("share", mapOf("source" to source))
    }
}

class BridgedCrashlyticsService(
    private val bridge: CrashlyticsBridge
) : CrashlyticsService {

    override fun logException(exception: Throwable) {
        val message = exception.message ?: exception::class.simpleName ?: "Unknown exception"
        val stackTrace = exception.stackTraceToString()
        bridge.recordException(message, stackTrace)
    }

    override fun log(message: String) {
        bridge.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        bridge.setCustomKeyString(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        bridge.setCustomKeyInt(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        bridge.setCustomKeyBool(key, value)
    }
}

class BridgedRemoteConfigService(
    private val bridge: RemoteConfigBridge,
    private val crashlyticsService: CrashlyticsService
) : RemoteConfigService {

    override suspend fun fetchAndActivate(): Boolean {
        return try {
            suspendCancellableCoroutine { continuation ->
                bridge.fetchAndActivate(object : FetchCallback {
                    override fun onResult(success: Boolean) {
                        if (continuation.isActive) {
                            continuation.resume(success)
                        }
                    }
                })
            }
        } catch (e: Exception) {
            crashlyticsService.logException(e)
            crashlyticsService.setCustomKey("operation", "remote_config_fetch")
            crashlyticsService.log("RemoteConfig: Error fetching remote config - ${e.message}")
            false
        }
    }

    override fun getString(key: String): String = bridge.getString(key)
    override fun getBoolean(key: String): Boolean = bridge.getBoolean(key)
    override fun getLong(key: String): Long = bridge.getLong(key)
    override fun getDouble(key: String): Double = bridge.getDouble(key)

    override fun isAddUserEnabled(): Boolean = getBoolean(RemoteConfigService.ADD_USER_ENABLED)
    override fun isSyncAllUsersEnabled(): Boolean = getBoolean(RemoteConfigService.SYNC_ALL_USERS_ENABLED)
    override fun isSyncUserEnabled(): Boolean = getBoolean(RemoteConfigService.SYNC_USER_ENABLED)
    override fun getSyncAllCooldownMinutes(): Long = getLong(RemoteConfigService.SYNC_ALL_COOLDOWN_MINUTES)
    override fun getSyncAllUserDelaySeconds(): Long = getLong(RemoteConfigService.SYNC_ALL_USER_DELAY_SECONDS)
    override fun getContestRefreshIntervalMinutes(): Long = getLong(RemoteConfigService.CONTEST_REFRESH_INTERVAL_MINUTES)
    override fun getContestStandingsRefreshIntervalMinutes(): Long = getLong(RemoteConfigService.CONTEST_STANDINGS_REFRESH_INTERVAL_MINUTES)
    override fun getUsersInfoRefreshIntervalMinutes(): Long = getLong(RemoteConfigService.USERS_INFO_REFRESH_INTERVAL_MINUTES)
}
