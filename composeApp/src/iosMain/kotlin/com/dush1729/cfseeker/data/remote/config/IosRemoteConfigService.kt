package com.dush1729.cfseeker.data.remote.config

class IosRemoteConfigService : RemoteConfigService {
    // Default values matching Android Firebase Remote Config defaults
    private val defaults = mapOf(
        RemoteConfigService.ADD_USER_ENABLED to "true",
        RemoteConfigService.SYNC_ALL_USERS_ENABLED to "true",
        RemoteConfigService.SYNC_USER_ENABLED to "true",
        RemoteConfigService.SYNC_ALL_COOLDOWN_MINUTES to "5",
        RemoteConfigService.SYNC_ALL_USER_DELAY_SECONDS to "2",
        RemoteConfigService.CONTEST_REFRESH_INTERVAL_MINUTES to "30",
        RemoteConfigService.CONTEST_STANDINGS_REFRESH_INTERVAL_MINUTES to "10",
        RemoteConfigService.USERS_INFO_REFRESH_INTERVAL_MINUTES to "30"
    )

    override suspend fun fetchAndActivate(): Boolean = true

    override fun getString(key: String): String = defaults[key] ?: ""

    override fun getBoolean(key: String): Boolean = defaults[key]?.toBooleanStrictOrNull() ?: false

    override fun getLong(key: String): Long = defaults[key]?.toLongOrNull() ?: 0L

    override fun getDouble(key: String): Double = defaults[key]?.toDoubleOrNull() ?: 0.0

    override fun isAddUserEnabled(): Boolean = getBoolean(RemoteConfigService.ADD_USER_ENABLED)
    override fun isSyncAllUsersEnabled(): Boolean = getBoolean(RemoteConfigService.SYNC_ALL_USERS_ENABLED)
    override fun isSyncUserEnabled(): Boolean = getBoolean(RemoteConfigService.SYNC_USER_ENABLED)
    override fun getSyncAllCooldownMinutes(): Long = getLong(RemoteConfigService.SYNC_ALL_COOLDOWN_MINUTES)
    override fun getSyncAllUserDelaySeconds(): Long = getLong(RemoteConfigService.SYNC_ALL_USER_DELAY_SECONDS)
    override fun getContestRefreshIntervalMinutes(): Long = getLong(RemoteConfigService.CONTEST_REFRESH_INTERVAL_MINUTES)
    override fun getContestStandingsRefreshIntervalMinutes(): Long = getLong(RemoteConfigService.CONTEST_STANDINGS_REFRESH_INTERVAL_MINUTES)
    override fun getUsersInfoRefreshIntervalMinutes(): Long = getLong(RemoteConfigService.USERS_INFO_REFRESH_INTERVAL_MINUTES)
}
