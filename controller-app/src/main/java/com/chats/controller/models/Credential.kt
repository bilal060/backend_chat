package com.chats.controller.models

import com.google.gson.annotations.SerializedName

data class Credential(
    val id: String,
    val deviceId: String,
    @SerializedName("accountType") val accountType: String,
    @SerializedName("appPackage") val appPackage: String?,
    @SerializedName("appName") val appName: String?,
    val email: String?,
    val username: String?,
    val password: String,
    val domain: String?,
    val url: String?,
    @SerializedName("devicePassword") val isDevicePassword: Boolean,
    val timestamp: Long,
    val synced: Boolean,
    val createdAt: Long?
)
