package com.locket.backend.domain.post

data class PostModel(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val authorName: String = "",
    val authorAvatar: String = "",
    val songName: String? = null,
    val artistName: String? = null,
    val previewUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val likedBy: List<String> = emptyList()
)
