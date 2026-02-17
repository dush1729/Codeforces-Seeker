package com.dush1729.cfseeker.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class IosBackgroundSyncScheduler : BackgroundSyncScheduler {
    override fun scheduleSyncUsers(handles: List<String>) {
        // No WorkManager on iOS - background sync not implemented yet
    }

    override fun observeSyncStatus(): Flow<SyncStatus> {
        return MutableStateFlow(SyncStatus.Idle)
    }
}
