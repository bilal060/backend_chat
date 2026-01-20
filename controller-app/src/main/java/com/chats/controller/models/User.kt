package com.chats.controller.models

data class User(
    val id: String,
    val username: String?,
    val email: String?,
    val role: String, // "admin" or "device_owner"
    val deviceId: String? = null // Assigned device ID for device owners
) {
    val isAdmin: Boolean
        get() = role == "admin"
    
    val isDeviceOwner: Boolean
        get() = role == "device_owner"
}
