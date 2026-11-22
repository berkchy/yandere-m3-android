package com.example.yanderem3.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.yanderem3.model.Post
import com.example.yanderem3.ui.GalleryUiState
import com.example.yanderem3.ui.components.PostCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun GalleryScreen(
    state: GalleryUiState,
    favoriteIds: Set<Long>,
    darkMode: Boolean,
    dynamicColor: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onTagClick: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onPageSizeChange: (Int) -> Unit,
    onRatingsChange: (Boolean, Boolean, Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: (Post) -> Unit,
    onPostClick: (Post) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var jumpPageText by rememberSaveable(state.page) { mutableStateOf(state.page.toString()) }
    var pageSizeExpanded by remember { mutableStateOf(false) }
    val pageSizes = listOf(20, 40, 60)
    val quickTags = listOf("landscape", "night", "sky", "city", "water")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Refresh") }
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search tags") },
                        placeholder = { Text("e.g. landscape rating:s") },
                        trailingIcon = {
                            IconButton(onClick = onSearch) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() })
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        quickTags.forEach { tag ->
                            AssistChip(onClick = { onTagClick(tag) }, label = { Text(tag) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.safeEnabled,
                            onClick = { onRatingsChange(!state.safeEnabled, state.questionableEnabled, state.explicitEnabled) },
                            label = { Text("Safe") }
                        )
                        FilterChip(
                            selected = state.questionableEnabled,
                            onClick = { onRatingsChange(state.safeEnabled, !state.questionableEnabled, state.explicitEnabled) },
                            label = { Text("Questionable") }
                        )
                        FilterChip(
                            selected = state.explicitEnabled,
                            onClick = { onRatingsChange(state.safeEnabled, state.questionableEnabled, !state.explicitEnabled) },
                            label = { Text("Explicit") }
                        )
                    }
                }
            }

            PaginationBar(
                page = state.page,
                pageSize = state.pageSize,
                pageSizes = pageSizes,
                jumpPageText = jumpPageText,
                onJumpPageTextChange = { jumpPageText = it.filter(Char::isDigit).take(5) },
                onJump = { jumpPageText.toIntOrNull()?.let(onPageChange) },
                onPrev = onPrevPage,
                onNext = onNextPage,
                onPageSizeChange = onPageSizeChange,
                pageSizeExpanded = pageSizeExpanded,
                onPageSizeExpandedChange = { pageSizeExpanded = it }
            )

            AnimatedContent(
                targetState = Triple(state.isLoading, state.error, state.posts),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "galleryContent",
                modifier = Modifier.fillMaxSize()
            ) { (loading, error, posts) ->
                when {
                    loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Text("Unable to load posts", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = onRefresh) { Text("Retry") }
                        }
                    }
                    posts.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No posts found", style = MaterialTheme.typography.bodyLarge)
                    }
                    else -> LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Adaptive(170.dp),
                        contentPadding = PaddingValues(12.dp),
                        verticalItemSpacing = 12.dp,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                isFavorite = favoriteIds.contains(post.id),
                                onClick = { onPostClick(post) },
                                onToggleFavorite = { onToggleFavorite(post) },
                                sharedTransitionScope = sharedTransitionScope,
                                animatedVisibilityScope = animatedVisibilityScope,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                SettingRow("Dark mode", "Use dark theme", darkMode, onToggleDarkMode)
                SettingRow("Dynamic color", "Use Material You colors on Android 12+", dynamicColor, onToggleDynamicColor)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaginationBar(
    page: Int,
    pageSize: Int,
    pageSizes: List<Int>,
    jumpPageText: String,
    onJumpPageTextChange: (String) -> Unit,
    onJump: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPageSizeChange: (Int) -> Unit,
    pageSizeExpanded: Boolean,
    onPageSizeExpandedChange: (Boolean) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 1.dp) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrev, enabled = page > 1) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous page")
                }
                Text("Page $page", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = onNext) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next page")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = jumpPageText,
                    onValueChange = onJumpPageTextChange,
                    label = { Text("Jump to page") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onJump() }),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onJump) { Text("Go") }
                Spacer(Modifier.width(8.dp))
                ExposedDropdownMenuBox(expanded = pageSizeExpanded, onExpandedChange = onPageSizeExpandedChange) {
                    OutlinedTextField(
                        value = "$pageSize / page",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Page size") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pageSizeExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).width(140.dp)
                    )
                    ExposedDropdownMenu(expanded = pageSizeExpanded, onDismissRequest = { onPageSizeExpandedChange(false) }) {
                        pageSizes.forEach { size ->
                            DropdownMenuItem(
                                text = { Text("$size / page") },
                                onClick = {
                                    onPageSizeExpandedChange(false)
                                    onPageSizeChange(size)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
