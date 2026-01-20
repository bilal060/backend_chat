package com.chats.capture.managers

import android.content.Context
import com.chats.capture.database.MediaFileDao
import com.chats.capture.models.UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MediaSyncMonitor(
    private val context: Context,
    private val mediaFileDao: MediaFileDao
) {
    
    suspend fun performHealthCheck() = withContext(Dispatchers.IO) {
        Timber.d("Performing media sync health check")
        
        // Reset stuck uploads (uploading for more than 10 minutes)
        val tenMinutesAgo = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)
        mediaFileDao.resetStuckUploads(tenMinutesAgo)
        
        // Check for orphaned files
        checkOrphanedFiles()
        
        // Retry permanently failed uploads after 24 hours
        retryPermanentlyFailedUploads()
        
        Timber.d("Health check completed")
    }
    
    private suspend fun checkOrphanedFiles() {
        // This would check for files without database entries
        // Implementation depends on specific requirements
    }
    
    private suspend fun retryPermanentlyFailedUploads() {
        // Retry permanently failed uploads after 24 hours
        val twentyFourHoursAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)
        val failedFiles = mediaFileDao.getMediaFilesByStatus(UploadStatus.PERMANENTLY_FAILED, 50)
        
        for (file in failedFiles) {
            if (file.lastUploadAttempt != null && file.lastUploadAttempt < twentyFourHoursAgo) {
                // Reset to PENDING for retry
                val updatedFile = file.copy(
                    uploadStatus = UploadStatus.PENDING,
                    uploadAttempts = 0,
                    errorMessage = null
                )
                mediaFileDao.updateMediaFile(updatedFile)
                Timber.d("Reset permanently failed upload for retry: ${file.id}")
            }
        }
    }
    
    suspend fun getSyncStatus(): SyncStatus = withContext(Dispatchers.IO) {
        val pendingCount = mediaFileDao.getPendingUploadCount()
        val pendingFiles = mediaFileDao.getMediaFilesByStatus(UploadStatus.PENDING, 10)
        val failedFiles = mediaFileDao.getMediaFilesByStatus(UploadStatus.FAILED, 10)
        
        SyncStatus(
            pendingCount = pendingCount,
            pendingFiles = pendingFiles,
            failedFiles = failedFiles
        )
    }
}

data class SyncStatus(
    val pendingCount: Int,
    val pendingFiles: List<com.chats.capture.models.MediaFile>,
    val failedFiles: List<com.chats.capture.models.MediaFile>
)
