package com.Locket.backend.domain.friend

import androidx.room.Entity

@Entity(
    tableName = "friendships",
    primaryKeys = ["senderPhone", "receiverPhone"]
)
data class FriendshipEntity(
    val senderPhone: String,
    val receiverPhone: String,
    val status: String, // "PENDING" (Chờ duyệt) hoặc "ACCEPTED" (Đã kết bạn)
    val timestamp: Long = System.currentTimeMillis()
)