package com.chats.capture.utils

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import timber.log.Timber

/**
 * Utility for generating and managing device identification
 */
object DeviceInfo {
    
    /**
     * Generate unique device ID
     * Uses Android ID + package name hash for uniqueness
     */
    fun getDeviceId(context: Context): String {
        return try {
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
            val packageName = context.packageName
            
            // Create hash of Android ID + package name
            val combined = "$androidId:$packageName"
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(combined.toByteArray())
            
            // Convert to hex string and take first 32 characters
            hash.joinToString("") { "%02x".format(it) }.take(32)
        } catch (e: Exception) {
            Timber.e(e, "Error generating device ID")
            // Fallback to timestamp-based ID
            "device_${System.currentTimeMillis()}"
        }
    }
    
    /**
     * Get device model name
     */
    fun getDeviceModel(): String {
        return try {
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Get Android OS version
     */
    fun getOsVersion(): String {
        return try {
            android.os.Build.VERSION.RELEASE
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    /**
     * Get device IMEI (if available and permission granted)
     */
    fun getImei(context: Context): String? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10+ requires special permission
                null
            } else {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
                telephonyManager.deviceId
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting IMEI")
            null
        }
    }
    
    /**
     * Get device name (if set by user)
     */
    fun getDeviceName(context: Context): String? {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                Settings.Global.getString(context.contentResolver, "device_name") ?:
                Settings.Secure.getString(context.contentResolver, "bluetooth_name") ?:
                android.os.Build.MODEL
            } else {
                android.os.Build.MODEL
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting device name")
            null
        }
    }
}
