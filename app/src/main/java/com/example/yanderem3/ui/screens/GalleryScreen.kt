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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Explicit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.yanderem3.data.local.UserSettings
import com.example.yanderem3.model.Post
import com.example.yanderem3.ui.GalleryUiState
import com.example.yanderem3.ui.components.PostCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun GalleryScreen(
    state: GalleryUiState,
    favoriteIds: Set<Long>,
    settings: UserSettings,
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
    onDownloadFolderChange: (String) -> Unit,
    onWifiOnlyDownloadsChange: (Boolean) -> Unit,
    onFilenamePatternChange: (String) -> Unit,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                actions = {
                    IconButton(onClick = onRefresh) { Icon(Icons.Rounded.Refresh, contentDescription = "Refresh") }
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Rounded.Settings, contentDescription = "Settings") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, tonalElevation = 1.dp) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        placeholder = { Text("Search tags", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingIconButton(selected = state.safeEnabled, icon = Icons.Rounded.Shield, contentDescription = "Safe") {
                                    onRatingsChange(!state.safeEnabled, state.questionableEnabled, state.explicitEnabled)
                                }
                                RatingIconButton(selected = state.questionableEnabled, icon = Icons.Rounded.WarningAmber, contentDescription = "Questionable") {
                                    onRatingsChange(state.safeEnabled, !state.questionableEnabled, state.explicitEnabled)
                                }
                                RatingIconButton(selected = state.explicitEnabled, icon = Icons.Rounded.Explicit, contentDescription = "Explicit") {
                                    onRatingsChange(state.safeEnabled, state.questionableEnabled, !state.explicitEnabled)
                                }
                                TextButton(onClick = onSearch, contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.height(32.dp)) {
                                    Text("Go", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.bodySmall,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() })
                    )
                }
            }

            PaginationBar(
                page = state.page,
                pageSize = state.pageSize,
                pageSizes = pageSizes,
                jumpPageText = jumpPageText,
                onJumpPageTextChange = { jumpPageText = it.filter(Char::isDigit).take(4) },
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
                    loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
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
                        columns = StaggeredGridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).verticalScroll(rememberScrollState())) {
                Text("Settings", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                SettingRow("Dark mode", "Use dark theme", settings.darkMode, onToggleDarkMode)
                SettingRow("Dynamic color", "Use Material You colors on Android 12+", settings.dynamicColor, onToggleDynamicColor)
                Spacer(Modifier.height(16.dp))
                Text("Downloads", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = settings.downloadFolder,
                    onValueChange = onDownloadFolderChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Download subfolder") },
                    placeholder = { Text("Pictures/YandeM3") }
                )
                Spacer(Modifier.height(8.dp))
                SettingRow("Wi-Fi only downloads", "Allow downloads only on Wi-Fi", settings.wifiOnlyDownloads, onWifiOnlyDownloadsChange)
                Spacer(Modifier.height(8.dp))
                Text("Filename pattern", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = settings.filenamePattern == "post_id", onClick = { onFilenamePatternChange("post_id") }, label = { Text("Post ID") }, shape = RoundedCornerShape(16.dp))
                    FilterChip(selected = settings.filenamePattern == "yande_id_md5", onClick = { onFilenamePatternChange("yande_id_md5") }, label = { Text("yande_<id>_<hash>") }, shape = RoundedCornerShape(16.dp))
                }
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
    val compactFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalIconButton(onClick = onPrev, enabled = page > 1, modifier = Modifier.size(34.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous page", modifier = Modifier.size(18.dp))
            }
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Text("$page", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
            }
            FilledTonalIconButton(onClick = onNext, modifier = Modifier.size(34.dp), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "Next page", modifier = Modifier.size(18.dp))
            }
            OutlinedTextField(
                value = jumpPageText,
                onValueChange = onJumpPageTextChange,
                singleLine = true,
                modifier = Modifier.width(64.dp).height(40.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onJump() }),
                textStyle = MaterialTheme.typography.labelSmall,
                colors = compactFieldColors
            )
            TextButton(onClick = onJump, contentPadding = PaddingValues(horizontal = 8.dp), modifier = Modifier.height(34.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Go", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.weight(1f))
            ExposedDropdownMenuBox(expanded = pageSizeExpanded, onExpandedChange = onPageSizeExpandedChange) {
                OutlinedTextField(
                    value = "$pageSize",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pageSizeExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).width(84.dp).height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.labelSmall,
                    colors = compactFieldColors
                )
                ExposedDropdownMenu(expanded = pageSizeExpanded, onDismissRequest = { onPageSizeExpandedChange(false) }) {
                    pageSizes.forEach { size ->
                        DropdownMenuItem(text = { Text("$size") }, onClick = { onPageSizeExpandedChange(false); onPageSizeChange(size) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingIconButton(selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(30.dp),
        shape = RoundedCornerShape(10.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(16.dp))
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
