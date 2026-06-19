package com.locket.backend.domain.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: Int = 1, // Only 1 draft at a time for simplicity
    val imageUri: String? = null,
    val caption: String = "",
    val songName: String? = null,
    val artistName: String? = null,
    val artworkUrl: String? = null,
    val previewUrl: String? = null
)
