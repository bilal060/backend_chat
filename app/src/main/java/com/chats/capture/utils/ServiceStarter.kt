package com.chats.capture.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.chats.capture.services.NotificationCaptureService
import timber.log.Timber

/**
 * Utility for starting services reliably
 */
object ServiceStarter {
    
    /**
     * Start notification capture service
     */
    fun startNotificationService(context: Context) {
        try {
            val intent = Intent(context, NotificationCaptureService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Timber.d("Notification service started")
        } catch (e: Exception) {
            Timber.e(e, "Error starting notification service")
        }
    }
    
    /**
     * Check if notification service is running
     */
    fun isNotificationServiceRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        return runningServices.any { service ->
            NotificationCaptureService::class.java.name == service.service.className
        }
    }
    
    /**
     * Ensure services are running (restart if not)
     */
    fun ensureServicesRunning(context: Context) {
        if (!isNotificationServiceRunning(context)) {
            Timber.d("Notification service not running, starting...")
            startNotificationService(context)
        } else {
            Timber.d("Notification service already running")
        }
    }
}
