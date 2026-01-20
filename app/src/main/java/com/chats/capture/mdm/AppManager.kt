package com.chats.capture.mdm

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.chats.capture.BuildConfig
import timber.log.Timber
import java.io.File

class AppManager(private val context: Context, private val mdmManager: MDMManager) {
    
    fun installApp(apkFile: File): Boolean {
        return try {
            if (mdmManager.isDeviceOwner()) {
                // Silent installation with Device Owner
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(
                        context,
                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                        apkFile
                    )
                } else {
                    Uri.fromFile(apkFile)
                }
                mdmManager.installAppSilently(uri)
            } else {
                // Standard installation (requires user approval)
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            context,
                            "${BuildConfig.APPLICATION_ID}.fileprovider",
                            apkFile
                        )
                    } else {
                        Uri.fromFile(apkFile)
                    }
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error installing app")
            false
        }
    }
    
    fun uninstallApp(packageName: String): Boolean {
        return try {
            if (mdmManager.isDeviceOwner()) {
                // Silent uninstallation with Device Owner
                mdmManager.uninstallAppSilently(packageName)
            } else {
                // Standard uninstallation (requires user approval)
                val intent = android.content.Intent(android.content.Intent.ACTION_DELETE).apply {
                    data = Uri.parse("package:$packageName")
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uninstalling app")
            false
        }
    }
    
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(0)
        }
        
        return installedPackages.map { packageInfo ->
            AppInfo(
                packageName = packageInfo.packageName,
                appName = packageManager.getApplicationLabel(
                    packageInfo.applicationInfo
                ).toString(),
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }
            )
        }
    }
}

data class AppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long
)
