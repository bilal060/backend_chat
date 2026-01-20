package com.chats.capture.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chats.capture.managers.CommandPollingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Worker to trigger command polling
 * Note: Actual polling is handled by CommandPollingManager which runs continuously
 * This worker can be used to ensure polling starts if it was stopped
 */
class RemoteCommandWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("RemoteCommandWorker started - ensuring command polling is active")
            
            // Ensure command polling manager is running
            val commandPollingManager = CommandPollingManager(applicationContext)
            commandPollingManager.startPolling()
            
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in RemoteCommandWorker")
            Result.retry()
        }
    }
}
