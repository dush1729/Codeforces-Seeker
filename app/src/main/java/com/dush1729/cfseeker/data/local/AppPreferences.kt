package com.dush1729.cfseeker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

interface AppPreferences {
    suspend fun incrementLaunchCount(): Int
}

class AppPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferences {
    private object PreferencesKeys {
        val LAUNCH_COUNT = intPreferencesKey("launch_count")
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
}
