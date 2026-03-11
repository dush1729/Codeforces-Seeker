package com.dush1729.cfseeker.data.local

interface AppPreferences {
    suspend fun incrementLaunchCount(): Int
    suspend fun setLastSyncAllTime(timestamp: Long)
    suspend fun getLastSyncAllTime(): Long
    suspend fun setContestLastSyncTime(timestamp: Long)
    suspend fun getContestLastSyncTime(): Long
    suspend fun setContestStandingsLastSyncTime(contestId: Int, timestamp: Long)
    suspend fun getContestStandingsLastSyncTime(contestId: Int): Long
    suspend fun setUsersInfoLastSyncTime(timestamp: Long)
    suspend fun getUsersInfoLastSyncTime(): Long
    suspend fun clearContestPreferences(contestIds: List<Int>)
}
