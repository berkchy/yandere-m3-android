package com.example.yanderem3.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.yanderem3.data.local.UserSettings
import com.example.yanderem3.model.Post

object DownloadUtil {
    fun enqueueDownload(context: Context, post: Post, settings: UserSettings): Long {
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

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading full resolution image")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(!settings.wifiOnlyDownloads)
            .setAllowedOverRoaming(!settings.wifiOnlyDownloads)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$subPath/$fileName")
            .addRequestHeader("User-Agent", "YandereM3Android/1.0 (Android; Material3)")

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return dm.enqueue(request)
    }

    private fun sanitizeSubPath(raw: String): String {
        val cleaned = raw.trim().trim('/').ifBlank { "Pictures/YandeM3" }
        val parts = cleaned.split('/').mapNotNull { part ->
            val safe = part.trim().replace(Regex("[^A-Za-z0-9._ -]"), "").trim()
            safe.takeIf { it.isNotBlank() && it != "." && it != ".." }
        }
        return parts.joinToString("/").ifBlank { "Pictures/YandeM3" }
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
