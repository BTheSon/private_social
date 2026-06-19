package com.example.backend.domain.contact

data class DeviceContact (
    val name: String,
    val rawPhoneNumber: String,
    val normalizedPhoneNumber: String
)