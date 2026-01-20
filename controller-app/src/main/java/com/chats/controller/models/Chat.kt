package com.chats.controller.models

import com.google.gson.annotations.SerializedName

data class Chat(
    val id: String,
    val deviceId: String,
    @SerializedName("appPackage") val appPackage: String,
    @SerializedName("appName") val appName: String,
    @SerializedName("chatIdentifier") val chatIdentifier: String?,
    val text: String,
    val timestamp: Long,
    val synced: Boolean,
    val createdAt: Long?
)
