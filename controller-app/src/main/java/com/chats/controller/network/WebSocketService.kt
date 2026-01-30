package com.chats.controller.network

import android.content.Context
import com.chats.controller.auth.AuthManager
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException
import timber.log.Timber

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
            Timber.d("WebSocket already connected")
            return
        }
        
        try {
            val token = AuthManager.getAuthToken(context)
            if (token == null) {
                Timber.w("Cannot connect WebSocket: No auth token")
                return
            }
            
            // Validate and normalize server URL
            if (serverUrl.isBlank()) {
                Timber.e("Cannot connect WebSocket: Server URL is blank")
                return
            }
            
            // Normalize URL - ensure proper format
            var normalizedUrl = serverUrl.trim()
            
            // Remove trailing slash
            normalizedUrl = normalizedUrl.trimEnd('/')
            
            // Ensure URL has protocol
            if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
                Timber.w("Server URL missing protocol, defaulting to http://")
                normalizedUrl = "http://$normalizedUrl"
            }
            
            // Validate URL format
            try {
                java.net.URI(normalizedUrl)
            } catch (e: Exception) {
                Timber.e(e, "Invalid server URL format: $normalizedUrl")
                return
            }
            
            Timber.d("Connecting WebSocket to: $normalizedUrl")
            
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
                // Force new connection (don't reuse)
                forceNew = true
                // Use proper transports
                transports = arrayOf("websocket", "polling")
            }
            
            // Disconnect existing socket if any
            socket?.disconnect()
            socket = null
            
            // Create new socket connection
            socket = IO.socket(normalizedUrl, options)
            setupSocketListeners()
            
            // Connect
            socket?.connect()
            
            Timber.d("WebSocket connection initiated to: $normalizedUrl")
            
        } catch (e: URISyntaxException) {
            Timber.e(e, "Invalid URI for WebSocket connection: $serverUrl")
        } catch (e: java.net.ConnectException) {
            Timber.e(e, "Failed to connect WebSocket to: $serverUrl - ${e.message}")
        } catch (e: Exception) {
            Timber.e(e, "Error connecting WebSocket: ${e.message}")
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
                Timber.d("WebSocket connected successfully")
                connectionListener?.invoke(true)
            }
            
            s.on(Socket.EVENT_DISCONNECT) { args ->
                isConnected = false
                Timber.d("WebSocket disconnected: ${args.contentToString()}")
                connectionListener?.invoke(false)
            }
            
            s.on(Socket.EVENT_CONNECT_ERROR) { args ->
                isConnected = false
                val error = args.getOrNull(0)?.toString() ?: "Unknown error"
                Timber.e("WebSocket connection error: $error")
                connectionListener?.invoke(false)
                
                // Auto-reconnect if under max attempts
                if (reconnectAttempts < maxReconnectAttempts) {
                    reconnectAttempts++
                    Timber.d("Will attempt to reconnect (attempt $reconnectAttempts/$maxReconnectAttempts)")
                } else {
                    Timber.w("Max reconnection attempts reached")
                }
            }
            
            s.on(Socket.EVENT_RECONNECT) {
                isConnected = true
                reconnectAttempts = 0
                Timber.d("WebSocket reconnected successfully")
                connectionListener?.invoke(true)
            }
            
            s.on(Socket.EVENT_RECONNECT_ERROR) { args ->
                val error = args.getOrNull(0)?.toString() ?: "Unknown error"
                Timber.e("WebSocket reconnection error: $error")
            }
            
            s.on(Socket.EVENT_RECONNECT_ATTEMPT) {
                Timber.d("WebSocket reconnection attempt: $reconnectAttempts")
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
