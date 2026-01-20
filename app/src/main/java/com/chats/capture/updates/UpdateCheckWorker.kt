package com.chats.capture.updates

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chats.capture.CaptureApplication
import com.chats.capture.database.UpdateStatusDao
import com.chats.capture.models.UpdateStatusEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class UpdateCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("UpdateCheckWorker started")
            
            val database = (applicationContext as CaptureApplication).database
            val updateStatusDao = database.updateStatusDao()
            val updateManager = UpdateManager(applicationContext, updateStatusDao)
            
            // Check if enough time has passed since last check
            val currentStatus = updateStatusDao.getUpdateStatus()
            val checkInterval = 6 * 60 * 60 * 1000L // 6 hours
            
            if (currentStatus != null) {
                val timeSinceLastCheck = System.currentTimeMillis() - currentStatus.lastCheckTime
                if (timeSinceLastCheck < checkInterval) {
                    Timber.d("Skipping update check, too soon since last check")
                    return@withContext Result.success()
                }
            }
            
            // Perform update check
            val result = updateManager.performUpdate()
            
            when (result) {
                UpdateManager.UpdateResult.NO_UPDATE_AVAILABLE -> {
                    Timber.d("No update available")
                    Result.success()
                }
                UpdateManager.UpdateResult.UPDATE_INSTALLED -> {
                    Timber.d("Update installed successfully")
                    Result.success()
                }
                UpdateManager.UpdateResult.PERMISSION_REQUIRED -> {
                    Timber.w("Permission required for update")
                    Result.success() // Don't retry - permission needs user action
                }
                UpdateManager.UpdateResult.UPDATE_FAILED -> {
                    Timber.w("Update failed - will retry on next scheduled check")
                    Result.success() // Don't retry immediately - wait for next scheduled check
                }
                else -> Result.success()
            }
        } catch (e: Exception) {
            // Network errors are expected and handled gracefully
            // Don't retry immediately - wait for next scheduled check
            Timber.d("Update check completed with error: ${e.message}")
            Result.success()
        }
    }
}
