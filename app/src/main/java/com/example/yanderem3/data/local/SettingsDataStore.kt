package com.example.yanderem3.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class UserSettings(
    val darkMode: Boolean = false,
    val dynamicColor: Boolean = true,
    val safeEnabled: Boolean = true,
    val questionableEnabled: Boolean = false,
    val explicitEnabled: Boolean = false,
    val pageSize: Int = 40,
    val lastPage: Int = 1,
    val lastTags: String = ""
)

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SAFE = booleanPreferencesKey("safe")
        val QUESTIONABLE = booleanPreferencesKey("questionable")
        val EXPLICIT = booleanPreferencesKey("explicit")
        val PAGE_SIZE = intPreferencesKey("page_size")
        val LAST_PAGE = intPreferencesKey("last_page")
        val LAST_TAGS = stringPreferencesKey("last_tags")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            safeEnabled = prefs[Keys.SAFE] ?: true,
            questionableEnabled = prefs[Keys.QUESTIONABLE] ?: false,
            explicitEnabled = prefs[Keys.EXPLICIT] ?: false,
            pageSize = prefs[Keys.PAGE_SIZE] ?: 40,
            lastPage = prefs[Keys.LAST_PAGE] ?: 1,
            lastTags = prefs[Keys.LAST_TAGS] ?: ""
        )
    }

    suspend fun setDarkMode(enabled: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    suspend fun setDynamicColor(enabled: Boolean) = context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    suspend fun setRatings(safe: Boolean, questionable: Boolean, explicit: Boolean) = context.dataStore.edit {
        it[Keys.SAFE] = safe
        it[Keys.QUESTIONABLE] = questionable
        it[Keys.EXPLICIT] = explicit
    }
    suspend fun setPageSize(size: Int) = context.dataStore.edit { it[Keys.PAGE_SIZE] = size }
    suspend fun setLastPage(page: Int) = context.dataStore.edit { it[Keys.LAST_PAGE] = page }
    suspend fun setLastTags(tags: String) = context.dataStore.edit { it[Keys.LAST_TAGS] = tags }
}
