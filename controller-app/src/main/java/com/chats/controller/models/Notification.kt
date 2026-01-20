package com.chats.controller.models

import com.google.gson.annotations.SerializedName

data class Notification(
    val id: String,
    val deviceId: String,
    @SerializedName("appPackage") val appPackage: String,
    @SerializedName("appName") val appName: String,
    val title: String?,
    val text: String?,
    val timestamp: Long,
    @SerializedName("mediaUrls") val mediaUrls: List<String>?,
    val synced: Boolean,
    val createdAt: Long?
)
