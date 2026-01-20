package com.chats.capture.workers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.chats.capture.updates.UpdateCheckWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

object UpdateCheckScheduler {
    
    private const val UPDATE_CHECK_WORK_NAME = "update_check_work"
    
    fun scheduleUpdateCheck(context: Context, intervalHours: Long = 6) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val updateCheckWork = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UPDATE_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            updateCheckWork
        )
        
        Timber.d("Update check scheduled every $intervalHours hours")
    }
    
    fun cancelUpdateCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UPDATE_CHECK_WORK_NAME)
        Timber.d("Update check cancelled")
    }
}
