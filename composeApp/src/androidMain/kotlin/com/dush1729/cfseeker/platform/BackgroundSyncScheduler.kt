package com.dush1729.cfseeker.platform

import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.dush1729.cfseeker.worker.SyncUsersWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class AndroidBackgroundSyncScheduler(
    private val workManager: WorkManager
) : BackgroundSyncScheduler {

    override fun scheduleSyncUsers(handles: List<String>) {
        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncUsersWorker>()
            .setInputData(
                workDataOf(SyncUsersWorker.KEY_USER_HANDLES to handles.toTypedArray())
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            SyncUsersWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }

    override fun observeSyncStatus(): Flow<SyncStatus> {
        return workManager.getWorkInfosForUniqueWorkFlow(SyncUsersWorker.WORK_NAME)
            .map { workInfos ->
                val runningWork = workInfos.firstOrNull {
                    it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED
                }
                val completedWork = workInfos.firstOrNull {
                    it.state == WorkInfo.State.SUCCEEDED || it.state == WorkInfo.State.FAILED
                }

                when {
                    runningWork != null -> {
                        val current = runningWork.progress.getInt(SyncUsersWorker.KEY_PROGRESS_CURRENT, 0)
                        val total = runningWork.progress.getInt(SyncUsersWorker.KEY_PROGRESS_TOTAL, 0)
                        if (total > 0) SyncStatus.Running(current, total) else SyncStatus.Running(0, 0)
                    }
                    completedWork?.state == WorkInfo.State.SUCCEEDED -> SyncStatus.Succeeded
                    completedWork?.state == WorkInfo.State.FAILED -> SyncStatus.Failed
                    else -> SyncStatus.Idle
                }
            }
    }
}
