package com.chats.capture.utils

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

/**
 * Utility to validate and fix server URLs
 */
object ServerUrlValidator {
    
    private const val DEFAULT_SERVER_URL = "https://backend-chat-yq33.onrender.com/"
    private const val PREFS_NAME = "capture_prefs"
    private const val KEY_SERVER_URL = "server_url"
    
    /**
     * Get a valid server URL, fixing it if necessary
     */
    fun getValidServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var serverUrl = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        
        // Validate and fix server URL
        if (isInvalidUrl(serverUrl)) {
            Timber.tag("SERVER_URL").w("Invalid server URL detected: $serverUrl, resetting to default: $DEFAULT_SERVER_URL")
            serverUrl = DEFAULT_SERVER_URL
            prefs.edit().putString(KEY_SERVER_URL, DEFAULT_SERVER_URL).apply()
        }
        
        // Normalize URL
        val normalizedUrl = normalizeUrl(serverUrl)
        if (normalizedUrl != serverUrl) {
            Timber.tag("SERVER_URL").d("Normalizing URL: $serverUrl -> $normalizedUrl")
            prefs.edit().putString(KEY_SERVER_URL, normalizedUrl).apply()
        }
        
        Timber.tag("SERVER_URL").d("Using server URL: $normalizedUrl")
        return normalizedUrl
    }
    
    /**
     * Check if URL is invalid (localhost, malformed, etc.)
     */
    fun isInvalidUrl(url: String): Boolean {
        if (url.isEmpty()) return true
        if (url.contains("127.0.0.1")) return true
        if (url.contains("localhost")) return true
        if (url.startsWith("http://https://")) return true
        if (url.startsWith("https://http://")) return true
        return false
    }
    
    /**
     * Normalize URL (ensure proper format)
     */
    fun normalizeUrl(url: String): String {
        var normalized = url.trim()
        
        // Remove duplicate protocols
        normalized = normalized.replace("http://https://", "https://")
        normalized = normalized.replace("https://http://", "https://")
        
        // Add protocol if missing
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }
        
        // Ensure trailing slash
        if (!normalized.endsWith("/")) {
            normalized = "$normalized/"
        }
        
        return normalized
    }
    
    /**
     * Validate and save server URL
     */
    fun validateAndSave(context: Context, url: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        if (isInvalidUrl(url)) {
            Timber.tag("SERVER_URL").w("Invalid URL provided: $url, using default")
            val defaultUrl = DEFAULT_SERVER_URL
            prefs.edit().putString(KEY_SERVER_URL, defaultUrl).apply()
            return defaultUrl
        }
        
        val normalizedUrl = normalizeUrl(url)
        prefs.edit().putString(KEY_SERVER_URL, normalizedUrl).apply()
        Timber.tag("SERVER_URL").d("Server URL saved: $normalizedUrl")
        return normalizedUrl
    }
    
    /**
     * Clear invalid server URL from preferences
     */
    fun clearInvalidUrl(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentUrl = prefs.getString(KEY_SERVER_URL, null)
        
        if (currentUrl != null && isInvalidUrl(currentUrl)) {
            Timber.tag("SERVER_URL").w("Clearing invalid server URL: $currentUrl")
            prefs.edit().putString(KEY_SERVER_URL, DEFAULT_SERVER_URL).apply()
        }
    }
}
