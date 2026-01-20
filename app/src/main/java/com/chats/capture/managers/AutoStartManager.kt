package com.chats.capture.managers

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import timber.log.Timber

/**
 * Manages auto-start permissions and ensures app starts automatically
 */
object AutoStartManager {
    
    /**
     * Check if auto-start is enabled (varies by manufacturer)
     */
    fun isAutoStartEnabled(context: Context): Boolean {
        // This is manufacturer-specific, so we'll check common ones
        return try {
            when {
                isXiaomi(context) -> checkXiaomiAutoStart(context)
                isHuawei(context) -> checkHuaweiAutoStart(context)
                isOppo(context) -> checkOppoAutoStart(context)
                isVivo(context) -> checkVivoAutoStart(context)
                isSamsung(context) -> checkSamsungAutoStart(context)
                else -> true // Assume enabled for other manufacturers
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking auto-start status")
            true // Default to enabled
        }
    }
    
    /**
     * Request auto-start permission (opens manufacturer-specific settings)
     */
    fun requestAutoStartPermission(context: Context): Intent? {
        return try {
            when {
                isXiaomi(context) -> getXiaomiAutoStartIntent(context)
                isHuawei(context) -> getHuaweiAutoStartIntent(context)
                isOppo(context) -> getOppoAutoStartIntent(context)
                isVivo(context) -> getVivoAutoStartIntent(context)
                isSamsung(context) -> getSamsungAutoStartIntent(context)
                else -> null // No specific settings for other manufacturers
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting auto-start intent")
            null
        }
    }
    
    // Manufacturer detection
    private fun isXiaomi(context: Context): Boolean {
        return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
               Build.MANUFACTURER.equals("Redmi", ignoreCase = true) ||
               Build.MANUFACTURER.equals("POCO", ignoreCase = true)
    }
    
    private fun isHuawei(context: Context): Boolean {
        return Build.MANUFACTURER.equals("Huawei", ignoreCase = true) ||
               Build.MANUFACTURER.equals("Honor", ignoreCase = true)
    }
    
    private fun isOppo(context: Context): Boolean {
        return Build.MANUFACTURER.equals("OPPO", ignoreCase = true) ||
               Build.MANUFACTURER.equals("OnePlus", ignoreCase = true) ||
               Build.MANUFACTURER.equals("Realme", ignoreCase = true)
    }
    
    private fun isVivo(context: Context): Boolean {
        return Build.MANUFACTURER.equals("vivo", ignoreCase = true)
    }
    
    private fun isSamsung(context: Context): Boolean {
        return Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }
    
    // Xiaomi auto-start
    private fun checkXiaomiAutoStart(context: Context): Boolean {
        // Check if app is in auto-start whitelist
        return Settings.Secure.getInt(
            context.contentResolver,
            "miui_app_auto_start_${context.packageName}",
            0
        ) == 1
    }
    
    private fun getXiaomiAutoStartIntent(context: Context): Intent {
        return Intent().apply {
            setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    // Huawei auto-start
    private fun checkHuaweiAutoStart(context: Context): Boolean {
        // Huawei uses its own settings
        return true // Assume enabled, as checking requires Huawei SDK
    }
    
    private fun getHuaweiAutoStartIntent(context: Context): Intent {
        return Intent().apply {
            setClassName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    // Oppo auto-start
    private fun checkOppoAutoStart(context: Context): Boolean {
        return true // Assume enabled
    }
    
    private fun getOppoAutoStartIntent(context: Context): Intent {
        return Intent().apply {
            setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    // Vivo auto-start
    private fun checkVivoAutoStart(context: Context): Boolean {
        return true // Assume enabled
    }
    
    private fun getVivoAutoStartIntent(context: Context): Intent {
        return Intent().apply {
            setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    
    // Samsung auto-start
    private fun checkSamsungAutoStart(context: Context): Boolean {
        return true // Samsung usually doesn't restrict auto-start
    }
    
    private fun getSamsungAutoStartIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
