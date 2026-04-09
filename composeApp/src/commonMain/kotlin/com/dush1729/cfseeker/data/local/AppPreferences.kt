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

    suspend fun setSignedInHandle(handle: String?)
    suspend fun getSignedInHandle(): String?

    suspend fun setRatedUserLastSyncTime(timestamp: Long)
    suspend fun getRatedUserLastSyncTime(): Long

    suspend fun setKnownMenuItemCount(count: Int)
    suspend fun getKnownMenuItemCount(): Int

    suspend fun setRatedUserActiveOnly(value: Boolean)
    suspend fun getRatedUserActiveOnly(): Boolean
    suspend fun setRatedUserIncludeRetired(value: Boolean)
    suspend fun getRatedUserIncludeRetired(): Boolean
}
