package com.dush1729.cfseeker.data.local

import platform.Foundation.NSUserDefaults

class IosAppPreferences : AppPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun incrementLaunchCount(): Int {
        val current = defaults.integerForKey("launch_count").toInt()
        val newCount = current + 1
        defaults.setInteger(newCount.toLong(), forKey = "launch_count")
        return newCount
    }

    override suspend fun setLastSyncAllTime(timestamp: Long) {
        defaults.setObject(timestamp, forKey = "last_sync_all_time")
    }

    override suspend fun getLastSyncAllTime(): Long {
        return defaults.integerForKey("last_sync_all_time")
    }

    override suspend fun setContestLastSyncTime(timestamp: Long) {
        defaults.setObject(timestamp, forKey = "contest_last_sync_time")
    }

    override suspend fun getContestLastSyncTime(): Long {
        return defaults.integerForKey("contest_last_sync_time")
    }

    override suspend fun setContestStandingsLastSyncTime(contestId: Int, timestamp: Long) {
        defaults.setObject(timestamp, forKey = "contest_standings_sync_$contestId")
    }

    override suspend fun getContestStandingsLastSyncTime(contestId: Int): Long {
        return defaults.integerForKey("contest_standings_sync_$contestId")
    }

    override suspend fun setUsersInfoLastSyncTime(timestamp: Long) {
        defaults.setObject(timestamp, forKey = "users_info_last_sync_time")
    }

    override suspend fun getUsersInfoLastSyncTime(): Long {
        return defaults.integerForKey("users_info_last_sync_time")
    }

    override suspend fun clearContestPreferences(contestIds: List<Int>) {
        contestIds.forEach { contestId ->
            defaults.removeObjectForKey("contest_standings_sync_$contestId")
        }
    }
}
