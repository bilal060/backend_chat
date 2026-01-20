package com.chats.capture.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import timber.log.Timber

/**
 * Utility to check all required permissions status
 */
object PermissionChecker {
    
    /**
     * Check if notification listener service is enabled
     */
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        val enabled = flat?.contains(packageName) == true
        Timber.d("Notification service enabled: $enabled")
        return enabled
    }
    
    /**
     * Check if accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        val enabled = enabledServices.any { serviceInfo ->
            serviceInfo.resolveInfo.serviceInfo.packageName == context.packageName
        }
        Timber.d("Accessibility service enabled: $enabled")
        return enabled
    }
    
    /**
     * Check if battery optimization is ignored
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            val ignored = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            Timber.d("Battery optimization ignored: $ignored")
            ignored
        } else {
            true
        }
    }
    
    /**
     * Check if usage stats permission is granted
     */
    fun isUsageStatsPermissionGranted(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    context.packageName
                )
                val granted = mode == AppOpsManager.MODE_ALLOWED
                Timber.d("Usage stats permission granted: $granted")
                granted
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking usage stats permission")
            false
        }
    }
    
    /**
     * Check if install packages permission is granted
     */
    fun canInstallPackages(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canInstall = context.packageManager.canRequestPackageInstalls()
            Timber.d("Can install packages: $canInstall")
            canInstall
        } else {
            true
        }
    }
    
    /**
     * Get all permission statuses
     */
    fun getAllPermissionStatus(context: Context): PermissionStatus {
        return PermissionStatus(
            notificationService = isNotificationServiceEnabled(context),
            accessibilityService = isAccessibilityServiceEnabled(context),
            batteryOptimization = isBatteryOptimizationIgnored(context),
            usageStats = isUsageStatsPermissionGranted(context),
            installPackages = canInstallPackages(context)
        )
    }
    
    /**
     * Check if all critical permissions are granted
     */
    fun areCriticalPermissionsGranted(context: Context): Boolean {
        val status = getAllPermissionStatus(context)
        return status.notificationService && status.accessibilityService
    }
}

data class PermissionStatus(
    val notificationService: Boolean,
    val accessibilityService: Boolean,
    val batteryOptimization: Boolean,
    val usageStats: Boolean,
    val installPackages: Boolean
)
