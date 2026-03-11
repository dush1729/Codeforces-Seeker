package com.dush1729.cfseeker.data.remote.config

interface RemoteConfigService {
    /**
     * Fetch and activate remote config values
     * @return true if fetch and activate succeeded
     */
    suspend fun fetchAndActivate(): Boolean

    /**
     * Get a string value from remote config
     */
    fun getString(key: String): String

    /**
     * Get a boolean value from remote config
     */
    fun getBoolean(key: String): Boolean

    /**
     * Get a long value from remote config
     */
    fun getLong(key: String): Long

    /**
     * Get a double value from remote config
     */
    fun getDouble(key: String): Double

    // Feature flags
    fun isAddUserEnabled(): Boolean
    fun isSyncAllUsersEnabled(): Boolean
    fun isSyncUserEnabled(): Boolean

    // Sync all settings
    fun getSyncAllCooldownMinutes(): Long
    fun getSyncAllUserDelaySeconds(): Long

    // Contest refresh settings
    fun getContestRefreshIntervalMinutes(): Long
    fun getContestStandingsRefreshIntervalMinutes(): Long

    // Users info refresh settings
    fun getUsersInfoRefreshIntervalMinutes(): Long

    companion object {
        const val ADD_USER_ENABLED = "add_user_enabled"
        const val SYNC_ALL_USERS_ENABLED = "sync_all_users_enabled"
        const val SYNC_USER_ENABLED = "sync_user_enabled"
        const val SYNC_ALL_COOLDOWN_MINUTES = "sync_all_cooldown_minutes"
        const val SYNC_ALL_USER_DELAY_SECONDS = "sync_all_user_delay_seconds"
        const val CONTEST_REFRESH_INTERVAL_MINUTES = "contest_refresh_interval_minutes"
        const val CONTEST_STANDINGS_REFRESH_INTERVAL_MINUTES = "contest_standings_refresh_interval_minutes"
        const val USERS_INFO_REFRESH_INTERVAL_MINUTES = "users_info_refresh_interval_minutes"
    }
}
