package com.chats.capture.updates

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class UpdateDownloader(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()
    
    suspend fun downloadUpdate(
        updateInfo: UpdateInfo,
        onProgress: (Long, Long) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            return@withContext withTimeout(300_000) { // 5 minutes timeout
                downloadWithRetry(updateInfo, onProgress)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error downloading update")
            Result.failure(e)
        }
    }
    
    private suspend fun downloadWithRetry(
        updateInfo: UpdateInfo,
        onProgress: (Long, Long) -> Unit
    ): Result<File> {
        val updatesDir = File(context.filesDir, "updates")
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }
        
        val apkFile = File(updatesDir, "app-v${updateInfo.versionCode}.apk")
        
        val request = Request.Builder()
            .url(updateInfo.downloadUrl)
            .build()
        
        val response = client.newCall(request).execute()
        
        if (!response.isSuccessful) {
            return Result.failure(Exception("Download failed: ${response.code}"))
        }
        
        val body = response.body ?: return Result.failure(Exception("Response body is null"))
        val contentLength = body.contentLength()
        
        // Download file
        FileOutputStream(apkFile).use { output ->
            body.byteStream().use { input ->
                val buffer = ByteArray(8192)
                var totalBytesRead = 0L
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    onProgress(totalBytesRead, contentLength)
                }
            }
        }
        
        // Verify checksum
        if (updateInfo.checksum.isNotEmpty()) {
            val calculatedChecksum = calculateChecksum(apkFile)
            val expectedChecksum = updateInfo.checksum.removePrefix("sha256:")
            
            if (calculatedChecksum != expectedChecksum) {
                apkFile.delete()
                return Result.failure(Exception("Checksum mismatch"))
            }
        }
        
        Timber.d("Update downloaded successfully: ${apkFile.absolutePath}")
        return Result.success(apkFile)
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
