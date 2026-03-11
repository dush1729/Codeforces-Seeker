package com.dush1729.cfseeker.platform

import kotlinx.coroutines.flow.Flow

interface BackgroundSyncScheduler {
    fun scheduleSyncUsers(handles: List<String>)
    fun observeSyncStatus(): Flow<SyncStatus>
}

sealed class SyncStatus {
    data object Idle : SyncStatus()
    data class Running(val current: Int, val total: Int) : SyncStatus()
    data object Succeeded : SyncStatus()
    data object Failed : SyncStatus()
}
