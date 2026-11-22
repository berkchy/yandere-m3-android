package com.example.yanderem3.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yanderem3.ui.navigation.Routes
import com.example.yanderem3.ui.screens.DetailScreen
import com.example.yanderem3.ui.screens.FavoritesScreen
import com.example.yanderem3.ui.screens.GalleryScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun YandereApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val galleryState by viewModel.gallery.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedPost by viewModel.selectedPost.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.initialize() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.GALLERY || currentRoute == Routes.FAVORITES

    SharedTransitionLayout {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == Routes.GALLERY,
                            onClick = {
                                navController.navigate(Routes.GALLERY) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Filled.Explore, contentDescription = "Explore") },
                            label = { Text("Explore") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.FAVORITES,
                            onClick = {
                                navController.navigate(Routes.FAVORITES) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Favorites") },
                            label = { Text("Favorites") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.GALLERY,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.GALLERY) {
                    GalleryScreen(
                        state = galleryState,
                        favoriteIds = favoriteIds,
                        darkMode = settings.darkMode,
                        dynamicColor = settings.dynamicColor,
                        onQueryChange = viewModel::onQueryChange,
                        onSearch = viewModel::applySearch,
                        onTagClick = viewModel::searchTag,
                        onPageChange = viewModel::setPage,
                        onPrevPage = viewModel::prevPage,
                        onNextPage = viewModel::nextPage,
                        onPageSizeChange = viewModel::setPageSize,
                        onRatingsChange = viewModel::setRatings,
                        onToggleDarkMode = viewModel::toggleDarkMode,
                        onToggleDynamicColor = viewModel::toggleDynamicColor,
                        onRefresh = viewModel::refresh,
                        onToggleFavorite = viewModel::toggleFavorite,
                        onPostClick = { post ->
                            viewModel.selectPost(post)
                            navController.navigate(Routes.detail(post.id))
                        },
                        animatedVisibilityScope = this@composable,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )
                }
                composable(
                    route = Routes.DETAIL,
                    arguments = listOf(navArgument("postId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getLong("postId") ?: -1L
                    val post = selectedPost?.takeIf { it.id == postId }
                        ?: galleryState.posts.firstOrNull { it.id == postId }
                        ?: favorites.firstOrNull { it.id == postId }?.let {
                            com.example.yanderem3.model.Post(
                                id = it.id, tags = it.tags, createdAt = it.createdAt, width = it.width,
                                height = it.height, score = it.score, rating = it.rating,
                                previewUrl = it.previewUrl, sampleUrl = it.sampleUrl, fileUrl = it.fileUrl, author = it.author
                            )
                        }
                    DetailScreen(
                        post = post,
                        isFavorite = post?.let { favoriteIds.contains(it.id) } ?: false,
                        onBack = { navController.popBackStack() },
                        onToggleFavorite = { post?.let(viewModel::toggleFavorite) },
                        onTagClick = { tag ->
                            viewModel.searchTag(tag)
                            navController.popBackStack(Routes.GALLERY, inclusive = false)
                        }
                    )
                }
                composable(Routes.FAVORITES) {
                    FavoritesScreen(
                        favorites = favorites,
                        onPostClick = { fav ->
                            val post = com.example.yanderem3.model.Post(
                                id = fav.id, tags = fav.tags, createdAt = fav.createdAt, width = fav.width,
                                height = fav.height, score = fav.score, rating = fav.rating,
                                previewUrl = fav.previewUrl, sampleUrl = fav.sampleUrl, fileUrl = fav.fileUrl, author = fav.author
                            )
                            viewModel.selectPost(post)
                            navController.navigate(Routes.detail(fav.id))
                        },
                        onRemove = viewModel::removeFavorite,
                        onTagClick = { tag ->
                            viewModel.searchTag(tag)
                            navController.navigate(Routes.GALLERY) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        animatedVisibilityScope = this@composable,
                        sharedTransitionScope = this@SharedTransitionLayout
                    )
                }
            }
        }
    }
}
