package com.example.backend.domain.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phoneNumber: String, // Số điện thoại làm khóa chính
    val displayName: String,
    val isMe: Boolean = false // Xác định đây có phải là tài khoản cá nhân của máy này không
)