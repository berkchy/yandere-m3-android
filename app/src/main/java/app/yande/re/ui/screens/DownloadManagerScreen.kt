package app.yande.re.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.yande.re.model.DownloadRecord
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerSheet(
    downloads: List<DownloadRecord>,
    onDismiss: () -> Unit,
    onCancel: (DownloadRecord) -> Unit,
    onRemove: (DownloadRecord) -> Unit,
    onRetry: (DownloadRecord) -> Unit,
    onOpen: (DownloadRecord) -> Unit,
    onClearFinished: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Download Manager", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onClearFinished) { Text("Clear finished") }
            }
            if (downloads.isEmpty()) {
                Text("No downloads", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                    items(downloads, key = { it.downloadId }) { record ->
                        DownloadRow(record, onCancel, onRemove, onRetry, onOpen)
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    record: DownloadRecord,
    onCancel: (DownloadRecord) -> Unit,
    onRemove: (DownloadRecord) -> Unit,
    onRetry: (DownloadRecord) -> Unit,
    onOpen: (DownloadRecord) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = MaterialTheme.shapes.large) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = record.thumbnailUrl,
                contentDescription = record.title,
                modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(record.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(record.fileName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(statusLabel(record), style = MaterialTheme.typography.labelMedium, color = statusColor(record))
                if (record.status in setOf("queued", "downloading", "paused")) {
                    val progress = if (record.sizeBytes > 0) record.downloadedBytes.toFloat() / record.sizeBytes.toFloat() else null
                    if (progress != null) LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                    else LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (record.reason.isNotBlank() && record.status in setOf("failed", "paused", "cancelled")) {
                    Text(record.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when (record.status) {
                    "completed" -> {
                        IconButton(onClick = { onOpen(record) }) { Icon(Icons.Filled.OpenInNew, contentDescription = "Open") }
                        IconButton(onClick = { onRemove(record) }) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
                    }
                    "failed", "cancelled" -> {
                        IconButton(onClick = { onRetry(record) }) { Icon(Icons.Filled.Refresh, contentDescription = "Retry") }
                        IconButton(onClick = { onRemove(record) }) { Icon(Icons.Filled.Delete, contentDescription = "Remove") }
                    }
                    else -> {
                        IconButton(onClick = { onCancel(record) }) { Icon(Icons.Filled.Cancel, contentDescription = "Cancel") }
                    }
                }
            }
        }
    }
}

@Composable
private fun statusColor(record: DownloadRecord) = when (record.status) {
    "completed" -> MaterialTheme.colorScheme.primary
    "failed" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun statusLabel(record: DownloadRecord): String {
    val size = formatBytes(record.downloadedBytes, record.sizeBytes)
    return when (record.status) {
        "queued" -> "Queued"
        "downloading" -> if (size.isNotBlank()) "Downloading $size" else "Downloading"
        "paused" -> if (size.isNotBlank()) "Paused $size" else "Paused"
        "completed" -> if (record.sizeBytes > 0) "Completed ${formatSingle(record.sizeBytes)}" else "Completed"
        "failed" -> "Failed"
        "cancelled" -> "Cancelled"
        else -> record.status.replaceFirstChar { it.uppercase() }
    }
}

private fun formatBytes(downloaded: Long, total: Long): String {
    if (total > 0) return "${formatSingle(downloaded)} / ${formatSingle(total)}"
    if (downloaded > 0) return formatSingle(downloaded)
    return ""
}

private fun formatSingle(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes >= gb -> String.format("%.1f GB", bytes / gb)
        bytes >= mb -> String.format("%.1f MB", bytes / mb)
        bytes >= kb -> String.format("%.1f KB", bytes / kb)
        else -> "$bytes B"
    }
}
