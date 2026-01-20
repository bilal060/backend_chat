package com.chats.capture.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "update_status")
data class UpdateStatus(
    @PrimaryKey val id: Int = 1,
    val lastCheckTime: Long = 0,
    val lastUpdateTime: Long? = null,
    val currentVersion: String = "1.0.0",
    val pendingUpdateVersion: String? = null,
    val updateDownloadProgress: Int = 0,
    val updateStatus: UpdateStatusEnum = UpdateStatusEnum.IDLE
)

enum class UpdateStatusEnum {
    IDLE,
    CHECKING,
    DOWNLOADING,
    INSTALLING,
    FAILED
}
