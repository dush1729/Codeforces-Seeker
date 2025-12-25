package com.dush1729.cfseeker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.dush1729.cfseeker.R
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay

@HiltWorker
class SyncUsersWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: UserRepository,
    private val analyticsService: AnalyticsService
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "sync_users_channel"
        const val CHANNEL_NAME = "User Sync"
        const val WORK_NAME = "sync_all_users"
        const val KEY_PROGRESS_CURRENT = "progress_current"
        const val KEY_PROGRESS_TOTAL = "progress_total"
    }

    override suspend fun doWork(): Result {
        android.util.Log.d("SyncUsersWorker", "doWork() started")
        val startTime = System.currentTimeMillis()
        var successCount = 0
        var failureCount = 0

        return try {
            // Get all user handles from database
            val userHandles = repository.getAllUserHandles()
            android.util.Log.d("SyncUsersWorker", "Found ${userHandles.size} users to sync")

            if (userHandles.isEmpty()) {
                android.util.Log.d("SyncUsersWorker", "No users to sync, returning success")
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

            // Sync each user with 5 second delay
            userHandles.forEachIndexed { index, handle ->
                try {
                    android.util.Log.d("SyncUsersWorker", "Syncing user $handle (${index + 1}/${userHandles.size})")

                    // Update progress data
                    val progressData = Data.Builder()
                        .putInt(KEY_PROGRESS_CURRENT, index + 1)
                        .putInt(KEY_PROGRESS_TOTAL, userHandles.size)
                        .build()
                    setProgress(progressData)

                    // Update notification with current progress
                    setForeground(createForegroundInfo(index, userHandles.size, handle))

                    // Fetch user data
                    repository.fetchUser(handle)
                    successCount++
                    android.util.Log.d("SyncUsersWorker", "Successfully synced $handle")

                    // Wait 5 seconds before next user (except for last user)
                    if (index < userHandles.size - 1) {
                        delay(5000)
                    }
                } catch (e: Exception) {
                    // Continue with next user even if one fails
                    failureCount++
                    android.util.Log.e("SyncUsersWorker", "Failed to sync $handle", e)
                    e.printStackTrace()
                }
            }

            // Show completion notification
            setForeground(createCompletionNotification(userHandles.size))
            android.util.Log.d("SyncUsersWorker", "Sync completed successfully")

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
            android.util.Log.e("SyncUsersWorker", "doWork() failed", e)
            e.printStackTrace()

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
        currentHandle: String? = null
    ): ForegroundInfo {
        val title = "Syncing Users"
        val text = if (currentHandle != null) {
            "Syncing $currentHandle (${current + 1}/$total)"
        } else {
            "Starting sync..."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
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

    private fun createCompletionNotification(total: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Sync Complete")
            .setContentText("Successfully synced $total users")
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
}
