package com.dush1729.cfseeker.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.dush1729.cfseeker.analytics.AnalyticsService
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.repository.UserRepository

class SyncUsersWorkerFactory(
    private val repository: UserRepository,
    private val analyticsService: AnalyticsService,
    private val crashlyticsService: CrashlyticsService,
    private val remoteConfigService: RemoteConfigService
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            SyncUsersWorker::class.java.name -> SyncUsersWorker(
                context = appContext,
                workerParams = workerParameters,
                repository = repository,
                analyticsService = analyticsService,
                crashlyticsService = crashlyticsService,
                remoteConfigService = remoteConfigService
            )
            else -> null
        }
    }
}
