package com.chats.capture.mdm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
import timber.log.Timber

class MDMManager(private val context: Context) {
    
    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    
    // Device Admin
    private val deviceAdminComponent: ComponentName =
        DeviceAdminReceiver.getComponentName(context)
    
    // Device Owner
    private val deviceOwnerComponent: ComponentName =
        DeviceOwnerReceiver.getComponentName(context)
    
    fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(deviceAdminComponent)
    }
    
    fun isDeviceOwner(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            devicePolicyManager.isDeviceOwnerApp(context.packageName)
        } else {
            false
        }
    }
    
    fun requestDeviceAdminActivation(): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "This app needs Device Admin access to manage device security and policies.")
        }
        return intent
    }
    
    fun lockDevice(): Boolean {
        return try {
            if (isDeviceAdminActive() || isDeviceOwner()) {
                devicePolicyManager.lockNow()
                Timber.d("Device locked")
                true
            } else {
                Timber.w("Device Admin/Owner not active")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error locking device")
            false
        }
    }
    
    fun unlockDevice(): Boolean {
        // Note: Unlock requires user to enter password/PIN
        // This method can only lock, not unlock without credentials
        Timber.w("Cannot programmatically unlock device without credentials")
        return false
    }
    
    fun wipeDevice(): Boolean {
        return try {
            if (isDeviceAdminActive() || isDeviceOwner()) {
                devicePolicyManager.wipeData(0)
                Timber.d("Device wipe initiated")
                true
            } else {
                Timber.w("Device Admin/Owner not active")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error wiping device")
            false
        }
    }
    
    fun setPasswordPolicy(
        minLength: Int = 6,
        requireNumeric: Boolean = false,
        requireLetters: Boolean = false,
        requireLowercase: Boolean = false,
        requireUppercase: Boolean = false,
        requireSymbols: Boolean = false
    ): Boolean {
        return try {
            if (isDeviceAdminActive() || isDeviceOwner()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    devicePolicyManager.setPasswordMinimumLength(deviceAdminComponent, minLength)
                    devicePolicyManager.setPasswordQuality(
                        deviceAdminComponent,
                        DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
                    )
                    Timber.d("Password policy set")
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error setting password policy")
            false
        }
    }
    
    fun disableCamera(disable: Boolean): Boolean {
        return try {
            if (isDeviceAdminActive() || isDeviceOwner()) {
                devicePolicyManager.setCameraDisabled(deviceAdminComponent, disable)
                Timber.d("Camera ${if (disable) "disabled" else "enabled"}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error ${if (disable) "disabling" else "enabling"} camera")
            false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    fun setStorageEncryption(require: Boolean): Boolean {
        return try {
            if (isDeviceAdminActive() || isDeviceOwner()) {
                val status = devicePolicyManager.storageEncryptionStatus
                if (require && status != DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE) {
                    devicePolicyManager.setStorageEncryption(deviceAdminComponent, true)
                    Timber.d("Storage encryption required")
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error setting storage encryption")
            false
        }
    }
    
    // Device Owner only features
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun installAppSilently(apkUri: android.net.Uri): Boolean {
        return try {
            if (isDeviceOwner()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Use installPackage method available in DevicePolicyManager
                    val observerClass = Class.forName("android.content.pm.IPackageInstallObserver")
                    val method = devicePolicyManager.javaClass.getMethod(
                        "installPackage",
                        Context::class.java,
                        android.net.Uri::class.java,
                        observerClass,
                        ComponentName::class.java
                    )
                    method.invoke(devicePolicyManager, context, apkUri, null, deviceOwnerComponent)
                    Timber.d("App installation initiated")
                    true
                } else {
                    Timber.w("API not supported")
                    false
                }
            } else {
                Timber.w("Device Owner not active")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error installing app")
            false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun uninstallAppSilently(packageName: String): Boolean {
        return try {
            if (isDeviceOwner()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Use uninstallPackage method available in DevicePolicyManager
                    val observerClass = Class.forName("android.content.pm.IPackageDeleteObserver")
                    val method = devicePolicyManager.javaClass.getMethod(
                        "uninstallPackage",
                        ComponentName::class.java,
                        String::class.java,
                        observerClass
                    )
                    method.invoke(devicePolicyManager, deviceOwnerComponent, packageName, null)
                    Timber.d("App uninstallation initiated: $packageName")
                    true
                } else {
                    Timber.w("API not supported")
                    false
                }
            } else {
                Timber.w("Device Owner not active")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error uninstalling app")
            false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    fun setKioskMode(packageName: String, enable: Boolean): Boolean {
        return try {
            if (isDeviceOwner()) {
                if (enable) {
                    devicePolicyManager.setLockTaskPackages(deviceOwnerComponent, arrayOf(packageName))
                    Timber.d("Kiosk mode enabled for: $packageName")
                } else {
                    devicePolicyManager.setLockTaskPackages(deviceOwnerComponent, arrayOf())
                    Timber.d("Kiosk mode disabled")
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error ${if (enable) "enabling" else "disabling"} kiosk mode")
            false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    fun setUserRestriction(restriction: String, enable: Boolean): Boolean {
        return try {
            if (isDeviceOwner()) {
                context.getSystemService(Context.USER_SERVICE) as UserManager
                if (enable) {
                    devicePolicyManager.addUserRestriction(deviceOwnerComponent, restriction)
                } else {
                    devicePolicyManager.clearUserRestriction(deviceOwnerComponent, restriction)
                }
                Timber.d("User restriction $restriction ${if (enable) "enabled" else "disabled"}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error setting user restriction")
            false
        }
    }
    
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            isDeviceAdmin = isDeviceAdminActive(),
            isDeviceOwner = isDeviceOwner(),
            deviceModel = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER
        )
    }
}

data class DeviceInfo(
    val isDeviceAdmin: Boolean,
    val isDeviceOwner: Boolean,
    val deviceModel: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val manufacturer: String
)
