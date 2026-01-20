package com.chats.capture.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "media_files",
    indices = [
        Index(value = ["notificationId"]),
        Index(value = ["uploadStatus"]),
        Index(value = ["checksum"]),
        Index(value = ["deviceId"])
    ]
)
data class MediaFile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val deviceId: String? = null,
    val notificationId: String,
    val appPackage: String? = null, // App package name for organizing uploads
    val localPath: String,
    val remoteUrl: String? = null,
    val fileSize: Long,
    val mimeType: String,
    val checksum: String, // MD5 or SHA256
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
    val uploadAttempts: Int = 0,
    val lastUploadAttempt: Long? = null,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UploadStatus {
    PENDING,      // Ready to upload
    UPLOADING,    // Currently uploading
    SUCCESS,      // Successfully uploaded
    FAILED,       // Upload failed (will retry)
    PERMANENTLY_FAILED // Failed after max attempts
}
