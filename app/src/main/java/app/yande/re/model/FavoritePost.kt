package app.yande.re.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoritePost(
    @PrimaryKey val id: Long,
    val tags: String,
    val previewUrl: String?,
    val sampleUrl: String?,
    val fileUrl: String?,
    val width: Int,
    val height: Int,
    val rating: String,
    val score: Int,
    val author: String,
    val createdAt: Long,
    val savedAt: Long
)

fun Post.toFavorite(savedAt: Long = System.currentTimeMillis()): FavoritePost = FavoritePost(
    id = id,
    tags = tags,
    previewUrl = previewUrl,
    sampleUrl = sampleUrl,
    fileUrl = fileUrl,
    width = width,
    height = height,
    rating = rating,
    score = score,
    author = author,
    createdAt = createdAt,
    savedAt = savedAt
)

fun FavoritePost.toPost(): Post = Post(
    id = id,
    tags = tags,
    previewUrl = previewUrl,
    sampleUrl = sampleUrl,
    fileUrl = fileUrl,
    width = width,
    height = height,
    rating = rating,
    score = score,
    author = author,
    createdAt = createdAt
)
