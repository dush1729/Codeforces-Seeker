package com.dush1729.cfseeker.platform

import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.dush1729.cfseeker.data.remote.config.RemoteConfigService
import com.dush1729.cfseeker.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IosBackgroundSyncScheduler(
    private val repository: UserRepository,
    private val remoteConfigService: RemoteConfigService,
    private val crashlyticsService: CrashlyticsService
) : BackgroundSyncScheduler {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)

    override fun scheduleSyncUsers(handles: List<String>) {
        if (handles.isEmpty()) return

        scope.launch {
            try {
                _syncStatus.value = SyncStatus.Running(0, handles.size)
                crashlyticsService.log("IosBackgroundSyncScheduler: Starting sync for ${handles.size} users")

                val delaySeconds = remoteConfigService.getSyncAllUserDelaySeconds()
                val delayMillis = delaySeconds * 1000

                repository.fetchUsers(handles, delayMillis) { index, total, handle, exception ->
                    if (exception != null) {
                        crashlyticsService.logException(exception)
                        crashlyticsService.log("IosBackgroundSyncScheduler: Failed to sync $handle - ${exception.message}")
                    } else {
                        crashlyticsService.log("IosBackgroundSyncScheduler: Synced $handle (${index + 1}/$total)")
                    }
                    _syncStatus.value = SyncStatus.Running(index + 1, total)
                }

                _syncStatus.value = SyncStatus.Succeeded
                crashlyticsService.log("IosBackgroundSyncScheduler: Sync completed successfully")
            } catch (e: Exception) {
                crashlyticsService.logException(e)
                crashlyticsService.log("IosBackgroundSyncScheduler: Sync failed - ${e.message}")
                _syncStatus.value = SyncStatus.Failed
            }
        }
    }

    override fun observeSyncStatus(): Flow<SyncStatus> = _syncStatus.asStateFlow()
}
