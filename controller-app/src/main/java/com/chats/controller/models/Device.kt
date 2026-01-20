package com.chats.controller.models

data class Device(
    val id: String,
    val deviceId: String,
    val deviceName: String?,
    val model: String?,
    val osVersion: String?,
    val imei: String?,
    val fcmToken: String?,
    val lastSeen: Long?,
    val status: String, // "active", "inactive", "offline"
    val ownerId: String?,
    val createdAt: Long?
) {
    val isOnline: Boolean
        get() = lastSeen != null && (System.currentTimeMillis() - lastSeen) < 300000 // 5 minutes
    
    val displayName: String
        get() = deviceName ?: deviceId
}
