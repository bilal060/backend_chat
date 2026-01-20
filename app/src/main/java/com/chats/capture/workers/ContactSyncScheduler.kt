package com.chats.capture.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

object ContactSyncScheduler {
    
    private const val CONTACT_SYNC_WORK_NAME = "contact_sync_work"
    
    /**
     * Schedule daily contact sync (every 24 hours)
     */
    fun scheduleDailySync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        // Schedule daily (24 hours)
        val contactSyncWork = PeriodicWorkRequestBuilder<ContactSyncWorker>(
            24, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CONTACT_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            contactSyncWork
        )
        
        Timber.d("Daily contact sync scheduled (every 24 hours)")
    }
    
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CONTACT_SYNC_WORK_NAME)
        Timber.d("Contact sync cancelled")
    }
}
