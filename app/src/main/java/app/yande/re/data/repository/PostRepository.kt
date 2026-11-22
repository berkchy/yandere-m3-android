package app.yande.re.data.repository

import app.yande.re.data.local.DownloadsDao
import app.yande.re.data.local.FavoritesDao
import app.yande.re.data.local.SettingsDataStore
import app.yande.re.data.local.UserSettings
import app.yande.re.data.remote.YandereApi
import app.yande.re.model.DownloadRecord
import app.yande.re.model.FavoritePost
import app.yande.re.model.Post
import app.yande.re.model.toFavorite
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val api: YandereApi,
    private val favoritesDao: FavoritesDao,
    private val downloadsDao: DownloadsDao,
    private val settingsDataStore: SettingsDataStore
) {
    val settings: Flow<UserSettings> = settingsDataStore.settings
    val favorites: Flow<List<FavoritePost>> = favoritesDao.observeFavorites()
    val favoriteIds: Flow<List<Long>> = favoritesDao.observeFavoriteIds()
    val downloads: Flow<List<DownloadRecord>> = downloadsDao.observeDownloads()

    suspend fun fetchPosts(page: Int, limit: Int, tags: String?): List<Post> {
        val normalized = tags?.trim()?.takeIf { it.isNotEmpty() }
        return api.getPosts(page = page, limit = limit, tags = normalized).posts
    }

    fun observeIsFavorite(id: Long): Flow<Boolean> = favoritesDao.observeIsFavorite(id)

    suspend fun toggleFavorite(post: Post, isFavorite: Boolean) {
        if (isFavorite) favoritesDao.deleteById(post.id) else favoritesDao.upsert(post.toFavorite())
    }

    suspend fun removeFavorite(id: Long) = favoritesDao.deleteById(id)

    suspend fun upsertDownload(record: DownloadRecord) = downloadsDao.upsert(record)
    suspend fun getDownload(id: Long): DownloadRecord? = downloadsDao.getById(id)
    suspend fun updateDownloadProgress(id: Long, status: String, downloadedBytes: Long, sizeBytes: Long, reason: String = "", filePath: String = "", mimeType: String = "") = downloadsDao.updateProgress(id, status, downloadedBytes, sizeBytes, reason, filePath, mimeType)
    suspend fun updateDownloadStatus(id: Long, status: String, reason: String = "") = downloadsDao.updateStatus(id, status, reason)
    suspend fun removeDownload(id: Long) = downloadsDao.deleteById(id)
    suspend fun clearFinishedDownloads() = downloadsDao.clearFinished()

    suspend fun setDarkMode(enabled: Boolean) = settingsDataStore.setDarkMode(enabled)
    suspend fun setDynamicColor(enabled: Boolean) = settingsDataStore.setDynamicColor(enabled)
    suspend fun setRatings(safe: Boolean, q: Boolean, e: Boolean) = settingsDataStore.setRatings(safe, q, e)
    suspend fun setPageSize(size: Int) = settingsDataStore.setPageSize(size)
    suspend fun setLastPage(page: Int) = settingsDataStore.setLastPage(page)
    suspend fun setLastTags(tags: String) = settingsDataStore.setLastTags(tags)
    suspend fun setDownloadFolder(folder: String) = settingsDataStore.setDownloadFolder(folder)
    suspend fun setWifiOnlyDownloads(enabled: Boolean) = settingsDataStore.setWifiOnlyDownloads(enabled)
    suspend fun setFilenamePattern(pattern: String) = settingsDataStore.setFilenamePattern(pattern)
}
