package com.chats.controller.models

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: User? = null
)

data class DeviceListResponse(
    val success: Boolean,
    val message: String? = null,
    val data: List<Device>? = null
)

data class CommandRequest(
    val deviceId: String,
    val action: String,
    val parameters: Map<String, Any>? = null
)

data class CommandResponse(
    val success: Boolean,
    val message: String? = null,
    val commandId: String? = null
)
