package com.chats.capture.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber

/**
 * Scheduler for initial sync worker that runs once on app install
 */
object InitialSyncScheduler {
    
    private const val INITIAL_SYNC_WORK_NAME = "initial_sync_work"
    
    /**
     * Schedule initial sync worker (one-time execution)
     */
    fun scheduleInitialSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false) // Allow even if battery is low for initial sync
            .build()
        
        val initialSyncWork = OneTimeWorkRequestBuilder<InitialSyncWorker>()
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            INITIAL_SYNC_WORK_NAME,
            ExistingWorkPolicy.KEEP, // Keep existing work if already scheduled
            initialSyncWork
        )
        
        Timber.d("Initial sync scheduled")
    }
    
    /**
     * Cancel initial sync work
     */
    fun cancelInitialSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(INITIAL_SYNC_WORK_NAME)
        Timber.d("Initial sync cancelled")
    }
}
