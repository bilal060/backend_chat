package com.chats.capture.utils

import android.content.Context
import android.content.pm.PackageManager
import timber.log.Timber

/**
 * Utility to check app installation status and provide diagnostics
 */
object AppInstallationChecker {
    
    /**
     * Check if the app is installed and get detailed information
     */
    fun checkInstallationStatus(context: Context): InstallationStatus {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName
            
            // Try to get package info
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            
            // Get application info
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            
            // Check component state
            val componentName = android.content.ComponentName(
                packageName,
                "com.chats.capture.ui.SettingsLauncherActivity"
            )
            val componentState = packageManager.getComponentEnabledSetting(componentName)
            
            // Get app label
            val appLabel = packageManager.getApplicationLabel(appInfo).toString()
            
            InstallationStatus(
                isInstalled = true,
                packageName = packageName,
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                },
                appLabel = appLabel,
                componentState = componentState,
                componentStateDescription = when (componentState) {
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> "DEFAULT (0) - Enabled by default"
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> "ENABLED (1) - Explicitly enabled (VISIBLE)"
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> "DISABLED (2) - Explicitly disabled (HIDDEN)"
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> "DISABLED_USER (3) - Disabled by user"
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> "DISABLED_UNTIL_USED (4) - Disabled until used"
                    else -> "UNKNOWN ($componentState)"
                },
                isHidden = componentState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            )
        } catch (e: PackageManager.NameNotFoundException) {
            InstallationStatus(
                isInstalled = false,
                packageName = context.packageName,
                error = "Package not found: ${e.message}"
            )
        } catch (e: Exception) {
            InstallationStatus(
                isInstalled = false,
                packageName = context.packageName,
                error = "Error checking installation: ${e.message}"
            )
        }
    }
    
    /**
     * Log installation status for debugging
     */
    fun logInstallationStatus(context: Context) {
        val status = checkInstallationStatus(context)
        
        Timber.tag("APP_INSTALL_CHECK").i("=== App Installation Status ===")
        Timber.tag("APP_INSTALL_CHECK").i("Installed: ${status.isInstalled}")
        Timber.tag("APP_INSTALL_CHECK").i("Package: ${status.packageName}")
        
        if (status.isInstalled) {
            Timber.tag("APP_INSTALL_CHECK").i("App Label: ${status.appLabel}")
            Timber.tag("APP_INSTALL_CHECK").i("Version: ${status.versionName} (${status.versionCode})")
            Timber.tag("APP_INSTALL_CHECK").i("Component State: ${status.componentStateDescription}")
            Timber.tag("APP_INSTALL_CHECK").i("Is Hidden: ${status.isHidden}")
            
            if (!status.isHidden) {
                Timber.tag("APP_INSTALL_CHECK").w("⚠️ App is VISIBLE in launcher (component state: ${status.componentState})")
            } else {
                Timber.tag("APP_INSTALL_CHECK").i("✅ App is HIDDEN from launcher")
            }
        } else {
            Timber.tag("APP_INSTALL_CHECK").e("❌ App is NOT installed: ${status.error}")
        }
        
        Timber.tag("APP_INSTALL_CHECK").i("================================")
    }
    
    /**
     * Check if app is installed (simple check)
     */
    fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            Timber.e(e, "Error checking if app is installed")
            false
        }
    }
    
    /**
     * Check if app is hidden from launcher
     */
    fun isHiddenFromLauncher(context: Context): Boolean {
        return try {
            val componentName = android.content.ComponentName(
                context.packageName,
                "com.chats.capture.ui.SettingsLauncherActivity"
            )
            val state = context.packageManager.getComponentEnabledSetting(componentName)
            state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } catch (e: Exception) {
            Timber.e(e, "Error checking if app is hidden")
            false
        }
    }
}

data class InstallationStatus(
    val isInstalled: Boolean,
    val packageName: String,
    val versionName: String? = null,
    val versionCode: Long = 0,
    val appLabel: String? = null,
    val componentState: Int = -1,
    val componentStateDescription: String = "",
    val isHidden: Boolean = false,
    val error: String? = null
)
