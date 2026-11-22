package app.yande.re.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.yande.re.model.DownloadRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadsDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeDownloads(): Flow<List<DownloadRecord>>

    @Query("SELECT * FROM downloads WHERE downloadId = :id LIMIT 1")
    suspend fun getById(id: Long): DownloadRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DownloadRecord)

    @Query("UPDATE downloads SET status = :status, downloadedBytes = :downloadedBytes, sizeBytes = :sizeBytes, reason = :reason, filePath = CASE WHEN :filePath = '' THEN filePath ELSE :filePath END, mimeType = CASE WHEN :mimeType = '' THEN mimeType ELSE :mimeType END WHERE downloadId = :id")
    suspend fun updateProgress(id: Long, status: String, downloadedBytes: Long, sizeBytes: Long, reason: String, filePath: String = "", mimeType: String = "")

    @Query("UPDATE downloads SET status = :status, reason = :reason WHERE downloadId = :id")
    suspend fun updateStatus(id: Long, status: String, reason: String = "")

    @Query("DELETE FROM downloads WHERE downloadId = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM downloads WHERE status IN ('completed', 'failed', 'cancelled')")
    suspend fun clearFinished()
}
