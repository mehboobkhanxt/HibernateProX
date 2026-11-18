package com.hibernateprox.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserPrefs(
    val useDynamicColor: Boolean = false,
    val selectedProfileKey: String = "battery",
    val overlayPromptDismissed: Boolean = false,
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val SELECTED_PROFILE = stringPreferencesKey("selected_profile_key")
        val OVERLAY_DISMISSED = booleanPreferencesKey("overlay_prompt_dismissed")
    }

    val prefsFlow: Flow<UserPrefs> = dataStore.data.map { prefs ->
        UserPrefs(
            useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            selectedProfileKey = prefs[Keys.SELECTED_PROFILE] ?: "battery",
            overlayPromptDismissed = prefs[Keys.OVERLAY_DISMISSED] ?: false,
        )
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setSelectedProfile(key: String) {
        dataStore.edit { it[Keys.SELECTED_PROFILE] = key }
    }

    suspend fun setOverlayPromptDismissed(dismissed: Boolean) {
        dataStore.edit { it[Keys.OVERLAY_DISMISSED] = dismissed }
    }
}
