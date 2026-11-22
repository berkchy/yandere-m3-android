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
    val lastTags: String = "",
    val downloadFolder: String = "Pictures/YandeM3",
    val wifiOnlyDownloads: Boolean = false,
    val filenamePattern: String = "post_id"
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
        val DOWNLOAD_FOLDER = stringPreferencesKey("download_folder")
        val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        val FILENAME_PATTERN = stringPreferencesKey("filename_pattern")
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
            lastTags = prefs[Keys.LAST_TAGS] ?: "",
            downloadFolder = prefs[Keys.DOWNLOAD_FOLDER] ?: "Pictures/YandeM3",
            wifiOnlyDownloads = prefs[Keys.WIFI_ONLY_DOWNLOADS] ?: false,
            filenamePattern = prefs[Keys.FILENAME_PATTERN] ?: "post_id"
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
    suspend fun setDownloadFolder(folder: String) = context.dataStore.edit { it[Keys.DOWNLOAD_FOLDER] = folder }
    suspend fun setWifiOnlyDownloads(enabled: Boolean) = context.dataStore.edit { it[Keys.WIFI_ONLY_DOWNLOADS] = enabled }
    suspend fun setFilenamePattern(pattern: String) = context.dataStore.edit { it[Keys.FILENAME_PATTERN] = pattern }
}
