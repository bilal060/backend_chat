package com.chats.controller.models

import com.google.gson.annotations.SerializedName

data class Screenshot(
    val id: String,
    val deviceId: String,
    val url: String,
    val timestamp: Long,
    val createdAt: Long?
)
