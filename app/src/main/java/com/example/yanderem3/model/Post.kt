package com.example.yanderem3.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Long,
    val tags: String = "",
    @SerialName("created_at") val createdAt: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val score: Int = 0,
    val rating: String = "s",
    @SerialName("sample_url") val sampleUrl: String? = null,
    @SerialName("sample_width") val sampleWidth: Int = 0,
    @SerialName("sample_height") val sampleHeight: Int = 0,
    @SerialName("preview_url") val previewUrl: String? = null,
    @SerialName("preview_width") val previewWidth: Int = 0,
    @SerialName("preview_height") val previewHeight: Int = 0,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("file_size") val fileSize: Long = 0L,
    @SerialName("file_ext") val fileExt: String = "",
    val source: String = "",
    val author: String = ""
) {
    val displayImageUrl: String
        get() = sampleUrl?.takeIf { it.isNotBlank() }
            ?: previewUrl?.takeIf { it.isNotBlank() }
            ?: fileUrl.orEmpty()

    val previewImageUrl: String
        get() = previewUrl?.takeIf { it.isNotBlank() }
            ?: sampleUrl?.takeIf { it.isNotBlank() }
            ?: fileUrl.orEmpty()

    val detailImageUrl: String
        get() = fileUrl?.takeIf { it.isNotBlank() }
            ?: sampleUrl?.takeIf { it.isNotBlank() }
            ?: previewUrl.orEmpty()

    val tagList: List<String>
        get() = tags.split(" ").map { it.trim() }.filter { it.isNotEmpty() }

    val ratingLabel: String
        get() = when (rating.lowercase()) {
            "s" -> "Safe"
            "q" -> "Questionable"
            "e" -> "Explicit"
            else -> "Unknown"
        }
}
