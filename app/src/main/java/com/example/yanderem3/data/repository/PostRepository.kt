package com.example.yanderem3.data.repository

import com.example.yanderem3.data.local.FavoritesDao
import com.example.yanderem3.data.local.SettingsDataStore
import com.example.yanderem3.data.local.UserSettings
import com.example.yanderem3.data.remote.YandereApi
import com.example.yanderem3.model.FavoritePost
import com.example.yanderem3.model.Post
import com.example.yanderem3.model.toFavorite
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val api: YandereApi,
    private val favoritesDao: FavoritesDao,
    private val settingsDataStore: SettingsDataStore
) {
    val settings: Flow<UserSettings> = settingsDataStore.settings
    val favorites: Flow<List<FavoritePost>> = favoritesDao.observeFavorites()
    val favoriteIds: Flow<List<Long>> = favoritesDao.observeFavoriteIds()

    suspend fun fetchPosts(page: Int, limit: Int, tags: String?): List<Post> {
        val normalized = tags?.trim()?.takeIf { it.isNotEmpty() }
        return api.getPosts(page = page, limit = limit, tags = normalized).posts
    }

    fun observeIsFavorite(id: Long): Flow<Boolean> = favoritesDao.observeIsFavorite(id)

    suspend fun toggleFavorite(post: Post, isFavorite: Boolean) {
        if (isFavorite) favoritesDao.deleteById(post.id) else favoritesDao.upsert(post.toFavorite())
    }

    suspend fun removeFavorite(id: Long) = favoritesDao.deleteById(id)

    suspend fun setDarkMode(enabled: Boolean) = settingsDataStore.setDarkMode(enabled)
    suspend fun setDynamicColor(enabled: Boolean) = settingsDataStore.setDynamicColor(enabled)
    suspend fun setRatings(safe: Boolean, q: Boolean, e: Boolean) = settingsDataStore.setRatings(safe, q, e)
    suspend fun setPageSize(size: Int) = settingsDataStore.setPageSize(size)
    suspend fun setLastPage(page: Int) = settingsDataStore.setLastPage(page)
    suspend fun setLastTags(tags: String) = settingsDataStore.setLastTags(tags)
}
