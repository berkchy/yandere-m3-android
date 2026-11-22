package app.yande.re.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import app.yande.re.model.FavoritePost

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FavoritesScreen(
    favorites: List<FavoritePost>,
    onPostClick: (FavoritePost) -> Unit,
    onRemove: (Long) -> Unit,
    onTagClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Favorites") }) }) { padding ->
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No favorites yet", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(8.dp),
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            items(favorites, key = { it.id }) { fav ->
                with(sharedTransitionScope) {
                    Card(
                        onClick = { onPostClick(fav) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .fillMaxWidth()
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "post-${fav.id}"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .animateItem()
                    ) {
                        Box {
                            val ratio = if (fav.width > 0 && fav.height > 0) fav.width.toFloat() / fav.height.toFloat() else 1f
                            AsyncImage(
                                model = fav.previewUrl ?: fav.sampleUrl ?: fav.fileUrl,
                                contentDescription = "Favorite ${fav.id}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().aspectRatio(ratio.coerceIn(0.7f, 1.5f))
                            )
                            IconButton(onClick = { onRemove(fav.id) }, modifier = Modifier.align(Alignment.TopEnd)) {
                                Surface(color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.88f), shape = MaterialTheme.shapes.small) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove favorite", modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                        Column(Modifier.padding(8.dp)) {
                            Text("Post #${fav.id}", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${fav.rating.uppercase()} • ${fav.score}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
