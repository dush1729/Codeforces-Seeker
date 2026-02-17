package com.dush1729.cfseeker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dush1729.cfseeker.R
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.repository.UserRepository
import kotlinx.coroutines.delay

class SyncUsersWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val repository: UserRepository,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
    private val remoteConfigService: RemoteConfigService,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "sync_users_channel"
        const val CHANNEL_NAME = "User Sync"
        const val WORK_NAME = "sync_all_users"
        const val KEY_PROGRESS_CURRENT = "progress_current"
        const val KEY_PROGRESS_TOTAL = "progress_total"
        const val KEY_USER_HANDLES = "user_handles"
    }

    override suspend fun doWork(): Result {
        crashlyticsService.log("SyncUsersWorker: doWork() started")
        val startTime = System.currentTimeMillis()
        var successCount = 0
        var failureCount = 0

        return try {
            // Get user handles from input data, or fall back to all user handles
            val inputHandles = inputData.getStringArray(KEY_USER_HANDLES)?.toList()
            val userHandles = if (!inputHandles.isNullOrEmpty()) {
                inputHandles
            } else {
                repository.getAllUserHandles()
            }
            crashlyticsService.log("SyncUsersWorker: Found ${userHandles.size} users to sync")

            if (userHandles.isEmpty()) {
                crashlyticsService.log("SyncUsersWorker: No users to sync, returning success")
                analyticsService.logBulkSyncCompleted(
                    durationMs = System.currentTimeMillis() - startTime,
                    userCount = 0,
                    successCount = 0,
                    failureCount = 0
                )
                return Result.success()
            }

            // Create notification channel
            createNotificationChannel()

            // Set foreground to show notification
            setForeground(createForegroundInfo(0, userHandles.size))

            // Get delay between user syncs from remote config
            val delaySeconds = remoteConfigService.getSyncAllUserDelaySeconds()
            val delayMillis = delaySeconds * 1000

            // Use fetchUsers which makes 1 api call for all users + n calls for rating changes
            repository.fetchUsers(userHandles, delayMillis) { index, total, handle, exception ->
                if (exception == null) {
                    // Success
                    successCount++
                    crashlyticsService.log("SyncUsersWorker: Successfully synced $handle (${index + 1}/$total)")
                } else {
                    // Failure
                    failureCount++
                    crashlyticsService.logException(exception)
                    crashlyticsService.setCustomKey("user_handle", handle)
                    crashlyticsService.setCustomKey("operation", "doWork")
                    crashlyticsService.setCustomKey("sync_progress", "${index + 1}/$total")
                    crashlyticsService.log("SyncUsersWorker: Failed to sync $handle - ${exception.message}")
                }

                // Update progress data
                val progressData = Data.Builder()
                    .putInt(KEY_PROGRESS_CURRENT, index + 1)
                    .putInt(KEY_PROGRESS_TOTAL, total)
                    .build()
                setProgress(progressData)

                // Update notification with current progress
                setForeground(createForegroundInfo(index, total, handle, delayMillis))
            }

            // Show completion notification
            setForeground(createCompletionNotification(successCount, failureCount))
            crashlyticsService.log("SyncUsersWorker: Sync completed successfully (success: $successCount, failed: $failureCount)")

            // Log analytics
            val duration = System.currentTimeMillis() - startTime
            analyticsService.logBulkSyncCompleted(
                durationMs = duration,
                userCount = userHandles.size,
                successCount = successCount,
                failureCount = failureCount
            )

            Result.success()
        } catch (e: Exception) {
            crashlyticsService.logException(e)
            crashlyticsService.setCustomKey("operation", "doWorkWhole")
            crashlyticsService.setCustomKey("success_count", successCount)
            crashlyticsService.setCustomKey("failure_count", failureCount)
            crashlyticsService.log("SyncUsersWorker: doWorkWhole failed - ${e.message}")

            // Log analytics for failure
            val duration = System.currentTimeMillis() - startTime
            analyticsService.logBulkSyncCompleted(
                durationMs = duration,
                userCount = successCount + failureCount,
                successCount = successCount,
                failureCount = failureCount
            )

            Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of user synchronization"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundInfo(
        current: Int,
        total: Int,
        currentHandle: String? = null,
        delayMillis: Long = 0
    ): ForegroundInfo {
        val title = "Syncing Users"
        val text = if (currentHandle != null) {
            val remaining = total - (current + 1)
            val etaMillis = remaining * delayMillis
            val etaText = formatEta(etaMillis)
            "Syncing $currentHandle (${current + 1}/$total)\nSync completes in ~$etaText"
        } else {
            "Starting sync..."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(total, current, false)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createCompletionNotification(successCount: Int, failureCount: Int): ForegroundInfo {
        val total = successCount + failureCount
        val text = if (failureCount == 0) {
            "Successfully synced $total users"
        } else {
            "Synced $total users ($successCount successful, $failureCount failed)"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Sync Complete")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun formatEta(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return when {
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
}
