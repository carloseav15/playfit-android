package com.carlosarancibia.playfit.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val keyOnboardingCompleted = booleanPreferencesKey("onboarding_completed")
    private val keyLastSyncAt = longPreferencesKey("last_sync_at")
    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keySelectedPlatforms = stringPreferencesKey("selected_platform_ids")

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[keyOnboardingCompleted] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[keyOnboardingCompleted] = completed }
    }

    suspend fun setLastSyncAt(timestamp: Long) {
        dataStore.edit { it[keyLastSyncAt] = timestamp }
    }

    val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[keyThemeMode] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[keyThemeMode] = mode }
    }

    val selectedPlatformIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[keySelectedPlatforms]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet() ?: emptySet()
    }

    suspend fun setSelectedPlatformIds(ids: Set<String>) {
        dataStore.edit { it[keySelectedPlatforms] = ids.joinToString(",") }
    }

    suspend fun resetTaste() {
        dataStore.edit { prefs ->
            prefs[keyOnboardingCompleted] = false
            prefs[keySelectedPlatforms] = ""
            prefs[keyLastSyncAt] = 0L
        }
    }
}
