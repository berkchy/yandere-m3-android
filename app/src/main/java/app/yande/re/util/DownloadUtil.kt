package app.yande.re.util

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import app.yande.re.data.local.UserSettings
import app.yande.re.model.DownloadRecord
import app.yande.re.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

object DownloadUtil {
    fun enqueueDownload(context: Context, post: Post, settings: UserSettings): DownloadRecord {
        val url = post.fileUrl?.takeIf { it.isNotBlank() }
            ?: post.sampleUrl?.takeIf { it.isNotBlank() }
            ?: post.previewUrl.orEmpty()
        require(url.isNotBlank()) { "No downloadable image URL available." }

        val ext = resolveExtension(url, post.fileExt)
        val baseName = when (settings.filenamePattern) {
            "yande_id_md5" -> {
                val hash = extractHash(url)
                if (hash.isNotBlank()) "yande_${post.id}_$hash" else "yande_${post.id}"
            }
            else -> "post_${post.id}"
        }
        val fileName = "$baseName.$ext"
        val subPath = sanitizeSubPath(settings.downloadFolder)
        val filePath = "$subPath/$fileName"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading image")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(!settings.wifiOnlyDownloads)
            .setAllowedOverRoaming(!settings.wifiOnlyDownloads)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filePath)
            .addRequestHeader("User-Agent", "YandereApp/1.0 (Android)")

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = dm.enqueue(request)
        return DownloadRecord(
            downloadId = id,
            postId = post.id,
            title = "Post #${post.id}",
            thumbnailUrl = post.previewUrl ?: post.sampleUrl ?: post.fileUrl.orEmpty(),
            fileUrl = url,
            fileName = fileName,
            filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).resolve(filePath).absolutePath,
            mimeType = mimeTypeForExt(ext),
            status = "queued"
        )
    }

    suspend fun monitorDownload(context: Context, downloadId: Long, onProgress: suspend (String, Long, Long, String, String, String) -> Unit) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        withContext(Dispatchers.IO) {
            while (true) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query)
                if (cursor == null) {
                    onProgress("failed", 0L, 0L, "Download not found", "", "")
                    return@withContext
                }
                cursor.use {
                    if (!it.moveToFirst()) {
                        onProgress("cancelled", 0L, 0L, "Cancelled", "", "")
                        return@withContext
                    }
                    val status = it.intColumn(DownloadManager.COLUMN_STATUS)
                    val downloaded = it.longColumn(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val total = it.longColumn(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val reason = it.intColumn(DownloadManager.COLUMN_REASON)
                    val localUri = it.stringColumn(DownloadManager.COLUMN_LOCAL_URI)
                    val mediaType = it.stringColumn(DownloadManager.COLUMN_MEDIA_TYPE)
                    val path = localUri?.let { uri -> runCatching { Uri.parse(uri).path }.getOrNull() }.orEmpty()
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            onProgress("completed", downloaded, total, "", path, mediaType.orEmpty())
                            return@withContext
                        }
                        DownloadManager.STATUS_FAILED -> {
                            onProgress("failed", downloaded, total, failureReason(reason), path, mediaType.orEmpty())
                            return@withContext
                        }
                        DownloadManager.STATUS_PAUSED -> onProgress("paused", downloaded, total, pausedReason(reason), path, mediaType.orEmpty())
                        DownloadManager.STATUS_RUNNING -> onProgress("downloading", downloaded, total, "", path, mediaType.orEmpty())
                        DownloadManager.STATUS_PENDING -> onProgress("queued", downloaded, total, "", path, mediaType.orEmpty())
                    }
                }
                delay(800)
            }
        }
    }

    fun cancelDownload(context: Context, downloadId: Long) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.remove(downloadId)
    }

    fun openDownload(context: Context, record: DownloadRecord) {
        val file = File(record.filePath)
        val uri = if (file.exists()) {
            androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            runCatching {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.getUriForDownloadedFile(record.downloadId)
            }.getOrNull() ?: Uri.parse(record.fileUrl)
        }
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, record.mimeType.ifBlank { "image/*" })
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun removeCompletedFile(record: DownloadRecord) {
        val file = File(record.filePath)
        if (file.exists()) file.delete()
    }

    private fun Cursor.intColumn(name: String): Int = getInt(getColumnIndexOrThrow(name))
    private fun Cursor.longColumn(name: String): Long = getLong(getColumnIndexOrThrow(name))
    private fun Cursor.stringColumn(name: String): String? = getColumnIndex(name).takeIf { it >= 0 }?.let { getString(it) }

    private fun failureReason(reason: Int): String = when (reason) {
        DownloadManager.ERROR_CANNOT_RESUME -> "Cannot resume"
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage not found"
        DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "File already exists"
        DownloadManager.ERROR_FILE_ERROR -> "File error"
        DownloadManager.ERROR_HTTP_DATA_ERROR -> "HTTP data error"
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Insufficient space"
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "Too many redirects"
        DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "HTTP error"
        DownloadManager.ERROR_UNKNOWN -> "Unknown error"
        else -> "Download failed"
    }

    private fun pausedReason(reason: Int): String = when (reason) {
        DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "Waiting for Wi-Fi"
        DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "Waiting for network"
        DownloadManager.PAUSED_WAITING_TO_RETRY -> "Retrying"
        DownloadManager.PAUSED_UNKNOWN -> "Paused"
        else -> "Paused"
    }

    private fun mimeTypeForExt(ext: String): String = when (ext.lowercase()) {
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        else -> "image/*"
    }

    private fun sanitizeSubPath(raw: String): String {
        val cleaned = raw.trim().trim('/').ifBlank { "Pictures/yande.re" }
        val parts = cleaned.split('/').mapNotNull { part ->
            val safe = part.trim().replace(Regex("[^A-Za-z0-9._ -]"), "").trim()
            safe.takeIf { it.isNotBlank() && it != "." && it != ".." }
        }
        return parts.joinToString("/").ifBlank { "Pictures/yande.re" }
    }

    private fun resolveExtension(url: String, fileExt: String): String {
        val fromField = fileExt.trim().lowercase().removePrefix(".")
        if (fromField.matches(Regex("[a-z0-9]{2,5}"))) return fromField
        val path = Uri.parse(url).path.orEmpty()
        val fromUrl = path.substringAfterLast('.', "").lowercase()
        return if (fromUrl.matches(Regex("[a-z0-9]{2,5}"))) fromUrl else "jpg"
    }

    private fun extractHash(url: String): String {
        val name = Uri.parse(url).lastPathSegment.orEmpty().substringBeforeLast('.')
        val match = Regex("[a-fA-F0-9]{32}").find(name)?.value ?: ""
        return match.lowercase()
    }
}
