package com.dush1729.cfseeker.data.remote.config

import com.dush1729.cfseeker.BuildConfig
import com.dush1729.cfseeker.R
import com.dush1729.cfseeker.crashlytics.CrashlyticsService
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.hours

@Singleton
class FirebaseRemoteConfigService @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val crashlyticsService: CrashlyticsService
) : RemoteConfigService {

    init {
        // Set default values from XML
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // Set config settings
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if(BuildConfig.DEBUG) {
                0
            } else {
                1.hours.inWholeSeconds
            }
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override suspend fun fetchAndActivate(): Boolean {
        return try {
            remoteConfig.fetchAndActivate().await()
        } catch (e: Exception) {
            crashlyticsService.logException(e)
            crashlyticsService.setCustomKey("operation", "remote_config_fetch")
            crashlyticsService.log("RemoteConfig: Error fetching remote config - ${e.message}")
            false
        }
    }

    override fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    override fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }

    override fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }

    override fun getDouble(key: String): Double {
        return remoteConfig.getDouble(key)
    }

    override fun isAddUserEnabled(): Boolean {
        return getBoolean(RemoteConfigService.ADD_USER_ENABLED)
    }

    override fun isSyncAllUsersEnabled(): Boolean {
        return getBoolean(RemoteConfigService.SYNC_ALL_USERS_ENABLED)
    }

    override fun isSyncUserEnabled(): Boolean {
        return getBoolean(RemoteConfigService.SYNC_USER_ENABLED)
    }

    override fun getSyncAllCooldownMinutes(): Long {
        return getLong(RemoteConfigService.SYNC_ALL_COOLDOWN_MINUTES)
    }

    override fun getSyncAllUserDelaySeconds(): Long {
        return getLong(RemoteConfigService.SYNC_ALL_USER_DELAY_SECONDS)
    }

    override fun getContestRefreshIntervalMinutes(): Long {
        return getLong(RemoteConfigService.CONTEST_REFRESH_INTERVAL_MINUTES)
    }

    override fun getContestStandingsRefreshIntervalMinutes(): Long {
        return getLong(RemoteConfigService.CONTEST_STANDINGS_REFRESH_INTERVAL_MINUTES)
    }

    override fun getUsersInfoRefreshIntervalMinutes(): Long {
        return getLong(RemoteConfigService.USERS_INFO_REFRESH_INTERVAL_MINUTES)
    }
}
