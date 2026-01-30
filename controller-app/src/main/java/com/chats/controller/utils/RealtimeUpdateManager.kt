package com.chats.controller.utils

import android.content.Context
import com.chats.controller.auth.AuthManager
import com.chats.controller.network.WebSocketService
import timber.log.Timber

class RealtimeUpdateManager private constructor(private val context: Context) {
    
    private val webSocketService = WebSocketService.getInstance(context)
    private var serverUrl: String? = null
    
    companion object {
        @Volatile
        private var INSTANCE: RealtimeUpdateManager? = null
        
        fun getInstance(context: Context): RealtimeUpdateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealtimeUpdateManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Initialize and connect to WebSocket
     */
    fun initialize(serverUrl: String) {
        // Validate server URL
        if (serverUrl.isBlank()) {
            Timber.e("Cannot initialize WebSocket: Server URL is blank")
            return
        }
        
        this.serverUrl = serverUrl.trim()
        
        if (!AuthManager.isLoggedIn(context)) {
            Timber.d("User not logged in, skipping WebSocket initialization")
            return
        }
        
        // Setup listeners
        setupListeners()
        
        // Connect with validated URL
        Timber.d("Initializing WebSocket connection to: $serverUrl")
        webSocketService.connect(serverUrl)
    }
    
    /**
     * Reconnect after login/token refresh
     */
    fun reconnect(serverUrl: String) {
        this.serverUrl = serverUrl
        webSocketService.reconnect(serverUrl)
    }
    
    /**
     * Disconnect WebSocket
     */
    fun disconnect() {
        webSocketService.disconnect()
    }
    
    /**
     * Check if WebSocket is connected
     */
    fun isConnected(): Boolean {
        return webSocketService.isConnected()
    }
    
    private fun setupListeners() {
        // Connection status listener
        webSocketService.setConnectionListener { connected ->
        }
        
        // Data update listener (notifications, chats, credentials, etc.)
        webSocketService.setDataUpdateListener { deviceId, type, data ->
            onDataUpdate(deviceId, type, data)
        }
        
        // Device status update listener
        webSocketService.setDeviceStatusUpdateListener { deviceId, status ->
            onDeviceStatusUpdate(deviceId, status)
        }
        
        // Command update listener
        webSocketService.setCommandUpdateListener { deviceId, commandId, status, result ->
            onCommandUpdate(deviceId, commandId, status, result)
        }
    }
    
    /**
     * Handle data updates (notifications, chats, credentials, etc.)
     */
    private fun onDataUpdate(deviceId: String, type: String, data: Any) {
    }
    
    /**
     * Handle device status updates (online/offline, heartbeat, etc.)
     */
    private fun onDeviceStatusUpdate(deviceId: String, status: String) {
    }
    
    /**
     * Handle command status updates
     */
    private fun onCommandUpdate(deviceId: String, commandId: String, status: String, result: Any?) {
    }
    
    /**
     * Get WebSocket service instance for direct access if needed
     */
    fun getWebSocketService(): WebSocketService {
        return webSocketService
    }
}
