package com.chats.capture.utils

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

/**
 * Tracks app installation and first run status
 */
object InstallationTracker {
    
    private const val PREFS_NAME = "installation_tracker"
    private const val KEY_FIRST_RUN = "first_run"
    private const val KEY_INSTALL_TIME = "install_time"
    private const val KEY_LAST_VERSION = "last_version"
    private const val KEY_INITIAL_SYNC_COMPLETE = "initial_sync_complete"
    
    /**
     * Check if this is the first run after installation
     */
    fun isFirstRun(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean(KEY_FIRST_RUN, true)
        
        if (isFirst) {
            // Mark as not first run
            prefs.edit()
                .putBoolean(KEY_FIRST_RUN, false)
                .putLong(KEY_INSTALL_TIME, System.currentTimeMillis())
                .putString(KEY_LAST_VERSION, getCurrentVersion(context))
                .apply()
            
            Timber.d("First run detected - installation time recorded")
        }
        
        return isFirst
    }
    
    /**
     * Get installation timestamp
     */
    fun getInstallTime(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_INSTALL_TIME, System.currentTimeMillis())
    }
    
    /**
     * Check if app was updated
     */
    fun isAppUpdated(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastVersion = prefs.getString(KEY_LAST_VERSION, "")
        val currentVersion = getCurrentVersion(context)
        
        if (lastVersion != null && lastVersion != currentVersion && lastVersion.isNotEmpty()) {
            prefs.edit().putString(KEY_LAST_VERSION, currentVersion).apply()
            Timber.d("App updated from $lastVersion to $currentVersion")
            return true
        }
        
        return false
    }
    
    private fun getCurrentVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Check if initial sync is complete
     */
    fun isInitialSyncComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_INITIAL_SYNC_COMPLETE, false)
    }
    
    /**
     * Mark initial sync as complete
     */
    fun markInitialSyncComplete(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_INITIAL_SYNC_COMPLETE, true)
            .apply()
        Timber.d("Initial sync marked as complete")
    }
}
