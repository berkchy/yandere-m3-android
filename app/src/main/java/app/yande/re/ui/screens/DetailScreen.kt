package app.yande.re.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.yande.re.data.local.UserSettings
import app.yande.re.model.Post
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    post: Post?,
    isFavorite: Boolean,
    settings: UserSettings,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onTagClick: (String) -> Unit,
    onDownload: (Post) -> Unit
) {
    if (post == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Post unavailable")
        }
        return
    }

    val context = LocalContext.current
    var showInfo by rememberSaveable { mutableStateOf(false) }
    var fullHdRequested by rememberSaveable(post.id) { mutableStateOf(false) }
    var fullHdLoaded by remember(post.id) { mutableStateOf(false) }
    var fullHdError by remember(post.id) { mutableStateOf<String?>(null) }
    var fullHdLoading by remember(post.id) { mutableStateOf(false) }
    val previewModel = post.sampleUrl ?: post.previewUrl ?: post.fileUrl
    val fullModel = post.fileUrl ?: post.sampleUrl ?: post.previewUrl



    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        var scale by remember(post.id) { mutableStateOf(1f) }
        var offset by remember(post.id) { mutableStateOf(Offset.Zero) }
        val imageTransformState = rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 6f)
            offset = if (scale > 1f) offset + panChange else Offset.Zero
        }

        Box(Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context).data(previewModel).crossfade(true).build(),
                contentDescription = "Post ${post.id} preview image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                },
                error = {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Unable to load image", color = Color.White)
                    }
                },
                success = { SubcomposeAsyncImageContent() }
            )

            if (fullHdRequested && !fullModel.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context).data(fullModel).crossfade(true).build(),
                    contentDescription = "Post ${post.id} full image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .transformable(imageTransformState),
                    loading = {
                        LaunchedEffect(Unit) {
                            fullHdLoading = true
                            fullHdError = null
                        }
                        Box(Modifier.fillMaxSize())
                    },
                    error = {
                        LaunchedEffect(Unit) {
                            fullHdLoading = false
                            fullHdLoaded = false
                            fullHdError = "Failed to load full HD image"
                        }
                        Box(Modifier.fillMaxSize())
                    },
                    success = {
                        LaunchedEffect(Unit) {
                            fullHdLoading = false
                            fullHdLoaded = true
                            fullHdError = null
                        }
                        SubcomposeAsyncImageContent()
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White) }
                Text("Post #${post.id}", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
                IconButton(onClick = {
                    runCatching { onDownload(post) }
                        .onSuccess { Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show() }
                        .onFailure { Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show() }
                }) { Icon(Icons.Filled.Download, contentDescription = "Download image", tint = Color.White) }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!fullHdRequested) {
                    Button(onClick = { fullHdRequested = true; fullHdError = null }) {
                        Text("Load Full HD")
                    }
                } else if (fullHdLoading) {
                    Surface(color = Color.Black.copy(alpha = 0.55f), shape = MaterialTheme.shapes.small) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                            Text("Loading Full HD", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else if (fullHdError != null) {
                    Surface(color = Color.Black.copy(alpha = 0.55f), shape = MaterialTheme.shapes.small) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(fullHdError!!, color = Color.White, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { fullHdError = null; fullHdLoaded = false; fullHdRequested = false; fullHdRequested = true }) { Text("Retry") }
                                Button(onClick = { fullHdRequested = false; fullHdLoading = false; fullHdError = null }) { Text("Cancel") }
                            }
                        }
                    }
                }
                Surface(
                    onClick = { showInfo = true },
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Details & Tags", modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (showInfo) {
            ModalBottomSheet(onDismissRequest = { showInfo = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Post #${post.id}", style = MaterialTheme.typography.titleLarge)
                    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.large) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetaRow("Rating", post.ratingLabel)
                            MetaRow("Score", post.score.toString())
                            MetaRow("Dimensions", "${post.width} × ${post.height}")
                            MetaRow("Author", post.author.ifBlank { "Unknown" })
                            MetaRow("File type", post.fileExt.ifBlank { "Unknown" }.uppercase())
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = {
                                runCatching { onDownload(post) }
                                    .onSuccess { Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show() }
                                    .onFailure { Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show() }
                            },
                            label = { Text("Download") },
                            leadingIcon = { Icon(Icons.Filled.Download, contentDescription = null) }
                        )
                        AssistChip(
                            onClick = {
                                val url = post.fileUrl ?: post.detailImageUrl
                                if (url.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            label = { Text("Open Original") },
                            leadingIcon = { Icon(Icons.Filled.OpenInBrowser, contentDescription = null) }
                        )
                    }
                    val tags = post.tags.split(' ').map { it.trim() }.filter { it.isNotBlank() }
                    if (tags.isNotEmpty()) {
                        Text("Tags", style = MaterialTheme.typography.titleMedium)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tags.take(120).forEach { tag ->
                                AssistChip(onClick = { showInfo = false; onTagClick(tag) }, label = { Text(tag, maxLines = 1, overflow = TextOverflow.Ellipsis) })
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
