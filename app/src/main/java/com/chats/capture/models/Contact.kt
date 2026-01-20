package com.chats.capture.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["email"]),
        Index(value = ["synced"]),
        Index(value = ["deviceId"]),
        Index(value = ["lastSynced"])
    ]
)
data class Contact(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deviceId: String? = null,
    val name: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val organization: String? = null,
    val jobTitle: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null,
    val errorMessage: String? = null,
    val lastSynced: Long? = null // Track when contact was last synced to server
)
