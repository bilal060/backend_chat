package com.chats.capture.managers

import android.content.Context
import com.chats.capture.database.MediaFileDao
import com.chats.capture.database.NotificationDao
import com.chats.capture.database.ChatDao
import com.chats.capture.models.MediaFile
import com.chats.capture.models.UploadStatus
import com.chats.capture.network.ApiClient
import com.chats.capture.network.UploadManager
import com.chats.capture.utils.RetryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class MediaUploadManager(
    private val context: Context,
    private val mediaFileDao: MediaFileDao,
    private val notificationDao: NotificationDao? = null,
    private val chatDao: ChatDao? = null
) {
    
    private val uploadManager = UploadManager(context, ApiClient.getApiService())
    
    suspend fun uploadMediaFile(mediaFile: MediaFile): Boolean = withContext(Dispatchers.IO) {
        try {
            // Update status to UPLOADING
            val updatedFile = mediaFile.copy(uploadStatus = UploadStatus.UPLOADING)
            mediaFileDao.updateMediaFile(updatedFile)
            
            // Verify file exists
            val file = File(mediaFile.localPath)
            if (!file.exists()) {
                Timber.e("Media file does not exist: ${mediaFile.localPath}")
                mediaFileDao.markUploadAttempt(
                    mediaFile.id,
                    UploadStatus.FAILED,
                    System.currentTimeMillis(),
                    "File does not exist"
                )
                return@withContext false
            }
            
            // Re-verify checksum
            val calculatedChecksum = calculateChecksum(file)
            if (calculatedChecksum != mediaFile.checksum) {
                Timber.e("Checksum mismatch for file: ${mediaFile.id}")
                mediaFileDao.markUploadAttempt(
                    mediaFile.id,
                    UploadStatus.FAILED,
                    System.currentTimeMillis(),
                    "Checksum mismatch"
                )
                return@withContext false
            }
            
            // Check network availability
            if (!ApiClient.isNetworkAvailable(context)) {
                Timber.w("Network not available, skipping upload")
                mediaFileDao.updateMediaFile(updatedFile.copy(uploadStatus = UploadStatus.PENDING))
                return@withContext false
            }
            
            // Upload file
            val result = if (mediaFile.fileSize > 5 * 1024 * 1024) {
                // Large file - use chunked upload (if supported)
                uploadManager.uploadLargeFileChunked(mediaFile)
            } else {
                // Small file - regular upload
                uploadManager.uploadMediaFile(mediaFile)
            }
            
            result.fold(
                onSuccess = { fileUrl ->
                    // Mark as success
                    mediaFileDao.markAsUploaded(mediaFile.id, UploadStatus.SUCCESS, fileUrl)
                    Timber.d("Media file uploaded successfully: ${mediaFile.id}")
                    
                    // Update notification or chat with server URL
                    if (mediaFile.notificationId.startsWith("chat_")) {
                        // Chat media - update chat record
                        val chatId = mediaFile.notificationId.removePrefix("chat_")
                        chatDao?.let { dao ->
                            updateChatWithServerUrls(chatId, dao, fileUrl)
                        }
                    } else {
                        // Notification media - update notification record
                        notificationDao?.let { dao ->
                            updateNotificationWithServerUrls(mediaFile.notificationId, dao)
                        }
                    }
                    
                    true
                },
                onFailure = { error ->
                    // Mark as failed, will retry later with exponential backoff
                    val newAttempts = mediaFile.uploadAttempts + 1
                    val newStatus = if (!RetryManager.shouldRetry(newAttempts, RetryManager.MaxAttempts.MEDIA_UPLOAD)) {
                        UploadStatus.PERMANENTLY_FAILED
                    } else {
                        UploadStatus.FAILED
                    }
                    
                    mediaFileDao.markUploadAttempt(
                        mediaFile.id,
                        newStatus,
                        System.currentTimeMillis(),
                        error.message
                    )
                    Timber.e(error, "Failed to upload media file: ${mediaFile.id} (attempt $newAttempts/${RetryManager.MaxAttempts.MEDIA_UPLOAD})")
                    false
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error uploading media file: ${mediaFile.id}")
            mediaFileDao.markUploadAttempt(
                mediaFile.id,
                UploadStatus.FAILED,
                System.currentTimeMillis(),
                e.message
            )
            false
        }
    }
    
    suspend fun uploadPendingMediaFiles(limit: Int = 3): Int = withContext(Dispatchers.IO) {
        // Limit concurrent uploads to 2-3 for battery optimization
        val maxConcurrent = 2
        val pendingFiles = mediaFileDao.getPendingUploads(limit.coerceAtMost(maxConcurrent * 2))
        var successCount = 0
        var activeUploads = 0
        
        for (file in pendingFiles) {
            // Wait if we've reached max concurrent uploads
            while (activeUploads >= maxConcurrent) {
                delay(1000) // Wait 1 second before checking again
            }
            
            // Apply exponential backoff if this is a retry
            if (file.uploadAttempts > 0) {
                RetryManager.delayWithBackoff(file.uploadAttempts)
            }
            
            activeUploads++
            try {
                if (uploadMediaFile(file)) {
                    successCount++
                }
            } finally {
                activeUploads--
            }
            
            // Small delay between uploads
            delay(500)
        }
        
        successCount
    }
    
    private suspend fun updateNotificationWithServerUrls(notificationId: String, notificationDao: NotificationDao) {
        try {
            // Get all successfully uploaded media files for this notification
            val mediaFiles = mediaFileDao.getMediaFilesByNotification(notificationId)
            val serverUrls = mediaFiles
                .filter { it.uploadStatus == UploadStatus.SUCCESS && it.remoteUrl != null }
                .mapNotNull { it.remoteUrl }
            
            if (serverUrls.isNotEmpty()) {
                notificationDao.updateServerMediaUrls(notificationId, serverUrls)
                Timber.d("Updated notification $notificationId with ${serverUrls.size} server media URLs")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating notification with server URLs")
        }
    }
    
    private suspend fun updateChatWithServerUrls(chatId: String, chatDao: ChatDao, serverUrl: String) {
        try {
            // Get chat record
            val chat = chatDao.getChatById(chatId) ?: return
            
            // Get all successfully uploaded media files for this chat
            val chatNotificationId = "chat_$chatId"
            val mediaFiles = mediaFileDao.getMediaFilesByNotification(chatNotificationId)
            val serverUrls = mediaFiles
                .filter { it.uploadStatus == UploadStatus.SUCCESS && it.remoteUrl != null }
                .mapNotNull { it.remoteUrl }
            
            // Update chat with server URLs (replace local paths with server URLs)
            if (serverUrls.isNotEmpty()) {
                val updatedChat = chat.copy(mediaUrls = serverUrls)
                chatDao.updateChat(updatedChat)
                Timber.d("Updated chat $chatId with ${serverUrls.size} server media URLs")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating chat with server URLs")
        }
    }
    
    private fun calculateChecksum(file: File): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
