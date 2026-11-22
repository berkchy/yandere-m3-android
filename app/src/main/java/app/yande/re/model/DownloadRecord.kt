package app.yande.re.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadRecord(
    @PrimaryKey val downloadId: Long,
    val postId: Long,
    val title: String,
    val thumbnailUrl: String,
    val fileUrl: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val sizeBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: String = "queued",
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
