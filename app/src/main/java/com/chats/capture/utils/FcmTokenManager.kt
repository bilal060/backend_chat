package com.chats.capture.utils

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

/**
 * Utility for managing FCM token storage and retrieval
 */
object FcmTokenManager {
    
    private const val PREFS_NAME = "fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_TOKEN_TIMESTAMP = "fcm_token_timestamp"
    
    /**
     * Save FCM token to SharedPreferences
     */
    fun saveToken(context: Context, token: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_FCM_TOKEN, token)
                .putLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
                .apply()
            Timber.d("FCM token saved")
        } catch (e: Exception) {
            Timber.e(e, "Error saving FCM token")
        }
    }
    
    /**
     * Get saved FCM token
     */
    fun getToken(context: Context): String? {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_FCM_TOKEN, null)
        } catch (e: Exception) {
            Timber.e(e, "Error getting FCM token")
            null
        }
    }
    
    /**
     * Get token timestamp
     */
    fun getTokenTimestamp(context: Context): Long {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getLong(KEY_TOKEN_TIMESTAMP, 0)
        } catch (e: Exception) {
            Timber.e(e, "Error getting FCM token timestamp")
            0
        }
    }
    
    /**
     * Check if token exists
     */
    fun hasToken(context: Context): Boolean {
        return getToken(context) != null
    }
    
    /**
     * Clear saved token
     */
    fun clearToken(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_FCM_TOKEN)
                .remove(KEY_TOKEN_TIMESTAMP)
                .apply()
            Timber.d("FCM token cleared")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing FCM token")
        }
    }
}
