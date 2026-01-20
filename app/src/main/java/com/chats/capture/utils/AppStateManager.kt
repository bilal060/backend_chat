package com.chats.capture.utils

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

/**
 * Manages app state and configuration
 */
object AppStateManager {
    
    private const val PREFS_NAME = "app_state"
    private const val KEY_SERVICES_ENABLED = "services_enabled"
    private const val KEY_SETUP_COMPLETE = "setup_complete"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    private const val KEY_TOTAL_NOTIFICATIONS = "total_notifications"
    private const val KEY_TOTAL_CHATS = "total_chats"
    
    /**
     * Check if initial setup is complete
     */
    fun isSetupComplete(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_SETUP_COMPLETE, false)
    }
    
    /**
     * Mark setup as complete
     */
    fun markSetupComplete(context: Context) {
        getPrefs(context).edit()
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .apply()
        Timber.d("Setup marked as complete")
    }
    
    /**
     * Check if services are enabled
     */
    fun areServicesEnabled(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_SERVICES_ENABLED, false)
    }
    
    /**
     * Set services enabled state
     */
    fun setServicesEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_SERVICES_ENABLED, enabled)
            .apply()
        Timber.d("Services enabled state set to: $enabled")
    }
    
    /**
     * Update last sync time
     */
    fun updateLastSyncTime(context: Context) {
        getPrefs(context).edit()
            .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Get last sync time
     */
    fun getLastSyncTime(context: Context): Long {
        val prefs = getPrefs(context)
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }
    
    /**
     * Increment notification count
     */
    fun incrementNotificationCount(context: Context) {
        val prefs = getPrefs(context)
        val count = prefs.getLong(KEY_TOTAL_NOTIFICATIONS, 0) + 1
        prefs.edit().putLong(KEY_TOTAL_NOTIFICATIONS, count).apply()
    }
    
    /**
     * Increment chat count
     */
    fun incrementChatCount(context: Context) {
        val prefs = getPrefs(context)
        val count = prefs.getLong(KEY_TOTAL_CHATS, 0) + 1
        prefs.edit().putLong(KEY_TOTAL_CHATS, count).apply()
    }
    
    /**
     * Get total notifications captured
     */
    fun getTotalNotifications(context: Context): Long {
        val prefs = getPrefs(context)
        return prefs.getLong(KEY_TOTAL_NOTIFICATIONS, 0)
    }
    
    /**
     * Get total chats captured
     */
    fun getTotalChats(context: Context): Long {
        val prefs = getPrefs(context)
        return prefs.getLong(KEY_TOTAL_CHATS, 0)
    }
    
    /**
     * Reset all statistics
     */
    fun resetStatistics(context: Context) {
        getPrefs(context).edit()
            .putLong(KEY_TOTAL_NOTIFICATIONS, 0)
            .putLong(KEY_TOTAL_CHATS, 0)
            .apply()
        Timber.d("Statistics reset")
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
