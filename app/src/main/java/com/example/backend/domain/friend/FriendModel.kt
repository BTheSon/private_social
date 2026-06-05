package com.example.backend.domain.friend

data class FriendModel(
    val phoneNumber: String,
    val displayName: String,
    val relationStatus: String // "NONE", "SENT", "RECEIVED", "FRIEND"
)