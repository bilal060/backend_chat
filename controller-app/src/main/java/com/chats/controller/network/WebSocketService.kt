package com.chats.controller.network

import android.content.Context
import com.chats.controller.auth.AuthManager
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class WebSocketService private constructor(private val context: Context) {
    
    private var socket: Socket? = null
    private var isConnected = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val reconnectDelayMs = 3000L
    
    private var dataUpdateListener: ((String, String, Any) -> Unit)? = null
    private var deviceStatusUpdateListener: ((String, String) -> Unit)? = null
    private var commandUpdateListener: ((String, String, String, Any?) -> Unit)? = null
    private var connectionListener: ((Boolean) -> Unit)? = null
    
    companion object {
        @Volatile
        private var INSTANCE: WebSocketService? = null
        
        fun getInstance(context: Context): WebSocketService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WebSocketService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Connect to WebSocket server
     */
    fun connect(serverUrl: String) {
        if (socket?.connected() == true) {
            return
        }
        
        try {
            val token = AuthManager.getAuthToken(context)
            if (token == null) {
                return
            }
            
            // Socket.IO uses the same URL as HTTP server (not ws:// or wss://)
            // Remove trailing slash if present
            val wsUrl = serverUrl.trimEnd('/')
            
            val options = IO.Options().apply {
                // Send token in auth
                auth = mapOf("token" to token)
                // Also set in extraHeaders as fallback
                extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
                reconnection = true
                reconnectionAttempts = maxReconnectAttempts
                reconnectionDelay = reconnectDelayMs
                reconnectionDelayMax = 10000L
                timeout = 20000
            }
            
            socket = IO.socket(wsUrl, options)
            setupSocketListeners()
            
            socket?.connect()
            
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        socket?.disconnect()
        socket = null
        isConnected = false
        reconnectAttempts = 0
    }
    
    /**
     * Reconnect with new token (after login/refresh)
     */
    fun reconnect(serverUrl: String) {
        disconnect()
        connect(serverUrl)
    }
    
    private fun setupSocketListeners() {
        socket?.let { s ->
            s.on(Socket.EVENT_CONNECT) {
                isConnected = true
                reconnectAttempts = 0
                connectionListener?.invoke(true)
            }
            
            s.on(Socket.EVENT_DISCONNECT) { args ->
                isConnected = false
                connectionListener?.invoke(false)
            }
            
            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                connectionListener?.invoke(false)
                
                // Auto-reconnect if under max attempts
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++
                }
            }
            
            // Custom events from server
            s.on("data_update") { args ->
                try {
                    val data = args.getOrNull(0) as? Map<*, *>
                    if (data != null) {
                        val deviceId = data["deviceId"] as? String ?: ""
                        val type = data["type"] as? String ?: ""
                        val updateData = data["data"] ?: ""
                        dataUpdateListener?.invoke(deviceId, type, updateData)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            s.on("device_status_update") { args ->
                try {
                    val data = args.getOrNull(0) as? Map<*, *>
                    if (data != null) {
                        val deviceId = data["deviceId"] as? String ?: ""
                        val status = data["status"] as? String ?: ""
                        deviceStatusUpdateListener?.invoke(deviceId, status)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            s.on("command_update") { args ->
                try {
                    val data = args.getOrNull(0) as? Map<*, *>
                    if (data != null) {
                        val deviceId = data["deviceId"] as? String ?: ""
                        val commandId = data["commandId"] as? String ?: ""
                        val status = data["status"] as? String ?: ""
                        val result = data["result"]
                        commandUpdateListener?.invoke(deviceId, commandId, status, result)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * Set listener for data updates
     */
    fun setDataUpdateListener(listener: (deviceId: String, type: String, data: Any) -> Unit) {
        dataUpdateListener = listener
    }
    
    /**
     * Set listener for device status updates
     */
    fun setDeviceStatusUpdateListener(listener: (deviceId: String, status: String) -> Unit) {
        deviceStatusUpdateListener = listener
    }
    
    /**
     * Set listener for command updates
     */
    fun setCommandUpdateListener(listener: (deviceId: String, commandId: String, status: String, result: Any?) -> Unit) {
        commandUpdateListener = listener
    }
    
    /**
     * Set listener for connection status changes
     */
    fun setConnectionListener(listener: (connected: Boolean) -> Unit) {
        connectionListener = listener
    }
    
    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean {
        return isConnected && socket?.connected() == true
    }
}
