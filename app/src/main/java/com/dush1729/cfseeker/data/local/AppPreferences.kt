package com.dush1729.cfseeker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

interface AppPreferences {
    suspend fun incrementLaunchCount(): Int
    suspend fun setLastSyncAllTime(timestamp: Long)
    suspend fun getLastSyncAllTime(): Long
    suspend fun setContestLastSyncTime(timestamp: Long)
    suspend fun getContestLastSyncTime(): Long
    suspend fun setContestStandingsLastSyncTime(contestId: Int, timestamp: Long)
    suspend fun getContestStandingsLastSyncTime(contestId: Int): Long
}

class AppPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferences {
    private object PreferencesKeys {
        val LAUNCH_COUNT = intPreferencesKey("launch_count")
        val LAST_SYNC_ALL_TIME = longPreferencesKey("last_sync_all_time")
        val CONTEST_LAST_SYNC_TIME = longPreferencesKey("contest_last_sync_time")
    }

    override suspend fun incrementLaunchCount(): Int {
        var newCount = 0
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.LAUNCH_COUNT] ?: 0
            newCount = currentCount + 1
            preferences[PreferencesKeys.LAUNCH_COUNT] = newCount
        }
        return newCount
    }

    override suspend fun setLastSyncAllTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_ALL_TIME] = timestamp
        }
    }

    override suspend fun getLastSyncAllTime(): Long = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.LAST_SYNC_ALL_TIME] ?: 0L
    }

    override suspend fun setContestLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTEST_LAST_SYNC_TIME] = timestamp
        }
    }

    override suspend fun getContestLastSyncTime(): Long = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.CONTEST_LAST_SYNC_TIME] ?: 0L
    }

    override suspend fun setContestStandingsLastSyncTime(contestId: Int, timestamp: Long) {
        context.dataStore.edit { preferences ->
            val key = longPreferencesKey("contest_standings_sync_$contestId")
            preferences[key] = timestamp
        }
    }

    override suspend fun getContestStandingsLastSyncTime(contestId: Int): Long = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        val key = longPreferencesKey("contest_standings_sync_$contestId")
        preferences[key] ?: 0L
    }
}
