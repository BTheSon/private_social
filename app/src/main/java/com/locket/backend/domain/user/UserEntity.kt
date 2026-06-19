package com.locket.backend.domain.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phoneNumber: String,
    val displayName: String,
    val isMe: Boolean = false
)