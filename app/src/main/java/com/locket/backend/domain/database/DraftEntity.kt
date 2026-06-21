package com.locket.backend.domain.database

import androidx.room.Entity
import androidx.room.PrimaryKey

import java.util.UUID

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val imageUri: String,
    val caption: String = "",
    val songName: String? = null,
    val artistName: String? = null,
    val artworkUrl: String? = null,
    val previewUrl: String? = null,
    val status: String = "UPLOADING", // "UPLOADING" hoặc "FAILED"
    val createdAt: Long = System.currentTimeMillis()
)
