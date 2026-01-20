package com.chats.controller.models

data class CredentialsResponse(
    val success: Boolean,
    val credentials: List<Credential>,
    val count: Int
)
