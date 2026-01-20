package com.chats.controller.models

data class Contact(
    val id: String,
    val deviceId: String?,
    val name: String,
    val phoneNumber: String?,
    val email: String?,
    val organization: String?,
    val jobTitle: String?,
    val address: String?,
    val notes: String?,
    val photoUri: String?,
    val timestamp: Long,
    val createdAt: Long?
)
