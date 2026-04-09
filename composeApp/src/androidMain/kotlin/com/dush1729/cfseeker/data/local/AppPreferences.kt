package com.dush1729.cfseeker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferencesImpl(
    private val context: Context
) : AppPreferences {
    private object PreferencesKeys {
        val LAUNCH_COUNT = intPreferencesKey("launch_count")
        val LAST_SYNC_ALL_TIME = longPreferencesKey("last_sync_all_time")
        val CONTEST_LAST_SYNC_TIME = longPreferencesKey("contest_last_sync_time")
        val USERS_INFO_LAST_SYNC_TIME = longPreferencesKey("users_info_last_sync_time")
        val SIGNED_IN_HANDLE = stringPreferencesKey("signed_in_handle")
        val RATED_USER_LAST_SYNC_TIME = longPreferencesKey("rated_user_last_sync_time")
        val KNOWN_MENU_ITEM_COUNT = intPreferencesKey("known_menu_item_count")
        val RATED_USER_ACTIVE_ONLY = booleanPreferencesKey("rated_user_active_only")
        val RATED_USER_INCLUDE_RETIRED = booleanPreferencesKey("rated_user_include_retired")
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

    override suspend fun setUsersInfoLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERS_INFO_LAST_SYNC_TIME] = timestamp
        }
    }

    override suspend fun getUsersInfoLastSyncTime(): Long = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.USERS_INFO_LAST_SYNC_TIME] ?: 0L
    }

    override suspend fun setSignedInHandle(handle: String?) {
        context.dataStore.edit { preferences ->
            if (handle != null) {
                preferences[PreferencesKeys.SIGNED_IN_HANDLE] = handle
            } else {
                preferences.remove(PreferencesKeys.SIGNED_IN_HANDLE)
            }
        }
    }

    override suspend fun getSignedInHandle(): String? = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.SIGNED_IN_HANDLE]
    }

    override suspend fun setRatedUserLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RATED_USER_LAST_SYNC_TIME] = timestamp
        }
    }

    override suspend fun getRatedUserLastSyncTime(): Long = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.RATED_USER_LAST_SYNC_TIME] ?: 0L
    }

    override suspend fun setKnownMenuItemCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KNOWN_MENU_ITEM_COUNT] = count
        }
    }

    override suspend fun getKnownMenuItemCount(): Int = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.KNOWN_MENU_ITEM_COUNT] ?: 0
    }

    override suspend fun setRatedUserActiveOnly(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RATED_USER_ACTIVE_ONLY] = value
        }
    }

    override suspend fun getRatedUserActiveOnly(): Boolean = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.RATED_USER_ACTIVE_ONLY] ?: false
    }

    override suspend fun setRatedUserIncludeRetired(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RATED_USER_INCLUDE_RETIRED] = value
        }
    }

    override suspend fun getRatedUserIncludeRetired(): Boolean = withContext(Dispatchers.IO) {
        val preferences = context.dataStore.data.first()
        preferences[PreferencesKeys.RATED_USER_INCLUDE_RETIRED] ?: false
    }

    override suspend fun clearContestPreferences(contestIds: List<Int>) {
        context.dataStore.edit { preferences ->
            contestIds.forEach { contestId ->
                val key = longPreferencesKey("contest_standings_sync_$contestId")
                preferences.remove(key)
            }
        }
    }
}
