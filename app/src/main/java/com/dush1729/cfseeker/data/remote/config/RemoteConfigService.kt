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

    companion object {
        const val ADD_USER_ENABLED = "add_user_enabled"
        const val SYNC_ALL_USERS_ENABLED = "sync_all_users_enabled"
        const val SYNC_USER_ENABLED = "sync_user_enabled"
    }
}
