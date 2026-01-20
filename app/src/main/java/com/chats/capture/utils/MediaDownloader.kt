package com.chats.capture.utils

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class MediaDownloader(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    suspend fun downloadMedia(
        url: String,
        notificationId: String,
        maxRetries: Int = 3
    ): Result<DownloadedMedia> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return@withContext withTimeout(30_000) {
                    downloadWithRetry(url, notificationId, attempt)
                }
            } catch (e: Exception) {
                lastException = e
                Timber.w(e, "Download attempt ${attempt + 1} failed")
                if (attempt < maxRetries - 1) {
                    kotlinx.coroutines.delay((attempt + 1) * 1000L) // Exponential backoff
                }
            }
        }
        
        Result.failure(lastException ?: Exception("Download failed after $maxRetries attempts"))
    }
    
    private suspend fun downloadWithRetry(
        url: String,
        notificationId: String,
        attempt: Int
    ): Result<DownloadedMedia> {
        val request = Request.Builder()
            .url(url)
            .build()
        
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            return Result.failure(IOException("Failed to download: ${response.code}"))
        }
        
        val body = response.body ?: return Result.failure(IOException("Response body is null"))
        body.contentLength()
        
        // Determine file extension from URL or content type
        val extension = getFileExtension(url, response.header("Content-Type"))
        val fileName = "${notificationId}_${System.currentTimeMillis()}.$extension"
        
        // Create directory structure
        val appPackage = url.substringAfter("//").substringBefore("/").replace(".", "_")
        val dateDir = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .format(java.util.Date())
        val mediaDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "notifications/$appPackage/$dateDir"
        )
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        
        val file = File(mediaDir, fileName)
        
        // Download file
        FileOutputStream(file).use { output ->
            body.byteStream().use { input ->
                input.copyTo(output)
            }
        }
        
        // Validate file
        if (!file.exists() || file.length() == 0L) {
            file.delete()
            return Result.failure(IOException("Downloaded file is empty or invalid"))
        }
        
        // Calculate checksum
        val checksum = calculateChecksum(file)
        
        // Determine MIME type
        val mimeType = response.header("Content-Type") ?: getMimeTypeFromExtension(extension)
        
        Timber.d("Media downloaded successfully: ${file.absolutePath}, size: ${file.length()}, checksum: $checksum")
        
        return Result.success(
            DownloadedMedia(
                localPath = file.absolutePath,
                fileSize = file.length(),
                mimeType = mimeType ?: "application/octet-stream",
                checksum = checksum
            )
        )
    }
    
    private fun getFileExtension(url: String, contentType: String?): String {
        // Try to get extension from URL
        val urlExtension = url.substringAfterLast('.', "").substringBefore('?').substringBefore('#')
        if (urlExtension.isNotEmpty() && urlExtension.length <= 5) {
            return urlExtension.lowercase()
        }
        
        // Try to get from content type
        contentType?.let {
            when {
                it.contains("image/jpeg") || it.contains("image/jpg") -> return "jpg"
                it.contains("image/png") -> return "png"
                it.contains("image/gif") -> return "gif"
                it.contains("image/webp") -> return "webp"
                it.contains("video/mp4") -> return "mp4"
                it.contains("video/webm") -> return "webm"
                it.contains("video/quicktime") -> return "mov"
                it.contains("audio/mpeg") || it.contains("audio/mp3") -> return "mp3"
                it.contains("audio/ogg") -> return "ogg"
                it.contains("audio/wav") -> return "wav"
                it.contains("audio/mp4") || it.contains("audio/m4a") -> return "m4a"
                else -> return "bin"
            }
        }
        
        return "jpg" // Default
    }
    
    private fun getMimeTypeFromExtension(extension: String): String? {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            else -> null
        }
    }
    
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
}

data class DownloadedMedia(
    val localPath: String,
    val fileSize: Long,
    val mimeType: String,
    val checksum: String
)
