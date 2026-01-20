package com.chats.capture.managers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import timber.log.Timber

class BatteryOptimizationManager(private val context: Context) {
    
    fun isIgnoringBatteryOptimizations(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Pre-Marshmallow, always considered as ignoring
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestBatteryOptimizationExemption(): Intent? {
        if (isIgnoringBatteryOptimizations()) {
            Timber.d("Already ignoring battery optimizations")
            return null
        }
        
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        Timber.d("Requesting battery optimization exemption")
        return intent
    }
    
    fun openBatteryOptimizationSettings(): Intent {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }
}
