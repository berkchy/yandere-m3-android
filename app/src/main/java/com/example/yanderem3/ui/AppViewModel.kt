package com.example.yanderem3.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.yanderem3.data.local.UserSettings
import com.example.yanderem3.data.repository.PostRepository
import com.example.yanderem3.model.FavoritePost
import com.example.yanderem3.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val pageSize: Int = 40,
    val query: String = "",
    val appliedTags: String = "",
    val safeEnabled: Boolean = true,
    val questionableEnabled: Boolean = false,
    val explicitEnabled: Boolean = false
)

class AppViewModel(private val repository: PostRepository) : ViewModel() {
    val settings: StateFlow<UserSettings> = repository.settings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings()
    )
    val favorites: StateFlow<List<FavoritePost>> = repository.favorites.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val favoriteIds: StateFlow<Set<Long>> = repository.favoriteIds
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _gallery = MutableStateFlow(GalleryUiState())
    val gallery: StateFlow<GalleryUiState> = _gallery.asStateFlow()

    private val _selectedPost = MutableStateFlow<Post?>(null)
    val selectedPost: StateFlow<Post?> = _selectedPost.asStateFlow()

    private var initialized = false

    fun initialize() {
        if (initialized) return
        initialized = true
        viewModelScope.launch {
            settings.collect { s ->
                val current = _gallery.value
                if (!current.isLoading && current.posts.isEmpty() && current.appliedTags.isEmpty() && current.query.isEmpty() && current.error == null && current.page == 1) {
                    _gallery.update {
                        it.copy(
                            page = s.lastPage.coerceAtLeast(1),
                            pageSize = s.pageSize,
                            query = s.lastTags,
                            appliedTags = s.lastTags,
                            safeEnabled = s.safeEnabled,
                            questionableEnabled = s.questionableEnabled,
                            explicitEnabled = s.explicitEnabled
                        )
                    }
                    loadPosts()
                } else {
                    _gallery.update {
                        it.copy(
                            safeEnabled = s.safeEnabled,
                            questionableEnabled = s.questionableEnabled,
                            explicitEnabled = s.explicitEnabled,
                            pageSize = s.pageSize
                        )
                    }
                }
            }
        }
    }

    fun refresh() = loadPosts()

    fun onQueryChange(query: String) {
        _gallery.update { it.copy(query = query) }
    }

    fun applySearch() {
        val q = _gallery.value.query.trim()
        _gallery.update { it.copy(appliedTags = q, page = 1) }
        persistQueryState()
        loadPosts()
    }

    fun searchTag(tag: String) {
        _gallery.update { it.copy(query = tag, appliedTags = tag, page = 1) }
        persistQueryState()
        loadPosts()
    }

    fun setPage(page: Int) {
        val target = page.coerceAtLeast(1)
        if (target == _gallery.value.page) return
        _gallery.update { it.copy(page = target) }
        persistQueryState()
        loadPosts()
    }

    fun nextPage() = setPage(_gallery.value.page + 1)
    fun prevPage() = setPage((_gallery.value.page - 1).coerceAtLeast(1))

    fun setPageSize(size: Int) {
        _gallery.update { it.copy(pageSize = size, page = 1) }
        viewModelScope.launch {
            repository.setPageSize(size)
            repository.setLastPage(1)
        }
        loadPosts()
    }

    fun setRatings(safe: Boolean, q: Boolean, e: Boolean) {
        val normalizedSafe = if (!safe && !q && !e) true else safe
        _gallery.update { it.copy(safeEnabled = normalizedSafe, questionableEnabled = q, explicitEnabled = e, page = 1) }
        viewModelScope.launch {
            repository.setRatings(normalizedSafe, q, e)
            repository.setLastPage(1)
        }
        loadPosts()
    }

    fun toggleDarkMode(enabled: Boolean) = viewModelScope.launch { repository.setDarkMode(enabled) }
    fun toggleDynamicColor(enabled: Boolean) = viewModelScope.launch { repository.setDynamicColor(enabled) }
    fun setDownloadFolder(folder: String) = viewModelScope.launch { repository.setDownloadFolder(folder.trim().ifBlank { "Pictures/YandeM3" }) }
    fun setWifiOnlyDownloads(enabled: Boolean) = viewModelScope.launch { repository.setWifiOnlyDownloads(enabled) }
    fun setFilenamePattern(pattern: String) = viewModelScope.launch { repository.setFilenamePattern(pattern) }

    fun selectPost(post: Post?) { _selectedPost.value = post }

    fun isFavorite(id: Long): Boolean = favoriteIds.value.contains(id)

    fun toggleFavorite(post: Post) {
        val fav = isFavorite(post.id)
        viewModelScope.launch { repository.toggleFavorite(post, fav) }
    }

    fun removeFavorite(id: Long) {
        viewModelScope.launch { repository.removeFavorite(id) }
    }

    private fun persistQueryState() {
        val g = _gallery.value
        viewModelScope.launch {
            repository.setLastPage(g.page)
            repository.setLastTags(g.appliedTags)
        }
    }

    private fun loadPosts() {
        val snapshot = _gallery.value
        viewModelScope.launch {
            _gallery.update { it.copy(isLoading = true, error = null) }
            try {
                val ratingTag = buildRatingApiTag(snapshot.safeEnabled, snapshot.questionableEnabled, snapshot.explicitEnabled)
                val tags = listOf(snapshot.appliedTags.trim(), ratingTag)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .trim()
                    .ifBlank { null }
                val fetched = repository.fetchPosts(snapshot.page, snapshot.pageSize, tags)
                val filtered = fetched.filter { post ->
                    when (post.rating.lowercase()) {
                        "s" -> snapshot.safeEnabled
                        "q" -> snapshot.questionableEnabled
                        "e" -> snapshot.explicitEnabled
                        else -> true
                    }
                }
                _gallery.update { it.copy(posts = filtered, isLoading = false, error = null) }
            } catch (e: Exception) {
                _gallery.update {
                    it.copy(
                        isLoading = false,
                        error = e.message?.takeIf { m -> m.isNotBlank() } ?: "Failed to load posts."
                    )
                }
            }
        }
    }

    private fun buildRatingApiTag(safe: Boolean, q: Boolean, e: Boolean): String {
        val selected = buildList {
            if (safe) add("s")
            if (q) add("q")
            if (e) add("e")
        }
        return when (selected.size) {
            1 -> "rating:${selected.first()}"
            else -> ""
        }
    }

    companion object {
        fun factory(repository: PostRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(repository) as T
            }
    }
}
