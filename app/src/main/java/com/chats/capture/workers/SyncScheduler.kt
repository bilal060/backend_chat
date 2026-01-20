package com.chats.capture.workers

import android.content.Context
import android.os.BatteryManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit

object SyncScheduler {
    
    private const val SYNC_WORK_NAME = "sync_work"
    private const val NORMAL_SYNC_INTERVAL_MINUTES = 15L
    private const val LOW_BATTERY_SYNC_INTERVAL_MINUTES = 60L // 1 hour when battery < 20%
    private const val LOW_BATTERY_THRESHOLD = 20 // Battery percentage threshold
    
    fun scheduleSync(context: Context, intervalMinutes: Long? = null) {
        // Check battery level and adjust interval
        val batteryLevel = getBatteryLevel(context)
        val syncInterval = intervalMinutes ?: if (batteryLevel < LOW_BATTERY_THRESHOLD) {
            LOW_BATTERY_SYNC_INTERVAL_MINUTES
        } else {
            NORMAL_SYNC_INTERVAL_MINUTES
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(batteryLevel >= LOW_BATTERY_THRESHOLD) // Don't require battery not low if battery is low
            .build()
        
        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            syncInterval, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncWork
        )
        
        Timber.d("Sync scheduled every $syncInterval minutes (battery: $batteryLevel%)")
    }
    
    /**
     * Get current battery level percentage
     */
    private fun getBatteryLevel(context: Context): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        } catch (e: Exception) {
            Timber.e(e, "Error getting battery level")
            100 // Default to 100% if can't read
        }
    }
    
    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        Timber.d("Sync cancelled")
    }
}
