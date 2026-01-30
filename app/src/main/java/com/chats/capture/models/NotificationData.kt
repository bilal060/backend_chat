package com.chats.capture.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["appPackage"]),
        Index(value = ["timestamp"]),
        Index(value = ["synced"]),
        Index(value = ["deviceId"]),
        Index(value = ["lastSynced"])
    ]
)
data class NotificationData(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deviceId: String? = null,
    val appPackage: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val messageLines: List<String>? = null,
    val isGroupSummary: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUrls: List<String>? = null,
    val serverMediaUrls: List<String>? = null,
    val iconUrl: String? = null,
    val synced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null,
    val errorMessage: String? = null,
    val lastSynced: Long? = null // Track when notification was last synced to server
)
