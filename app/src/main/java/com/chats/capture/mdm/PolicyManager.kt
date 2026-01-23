package com.chats.capture.mdm

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import timber.log.Timber

class PolicyManager(private val context: Context, private val mdmManager: MDMManager) {
    
    fun applySecurityPolicy(policy: SecurityPolicy): Boolean {
        return try {
            var success = true
            
            // Password policy
            if (policy.enforcePasswordPolicy) {
                success = success && mdmManager.setPasswordPolicy(
                    minLength = policy.minPasswordLength,
                    requireNumeric = policy.requireNumeric,
                    requireLetters = policy.requireLetters
                )
            }
            
            // Camera restriction - always set the camera state based on policy
            // This ensures camera is enabled if disableCamera is false
            val previousCameraState = mdmManager.isCameraDisabled()
            val cameraSuccess = mdmManager.disableCamera(policy.disableCamera)
            if (previousCameraState != policy.disableCamera) {
                Timber.d("Camera state changed: ${if (previousCameraState) "disabled" else "enabled"} -> ${if (policy.disableCamera) "disabled" else "enabled"}")
            }
            success = success && cameraSuccess
            
            // Storage encryption
            if (policy.requireStorageEncryption && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                success = success && mdmManager.setStorageEncryption(true)
            }
            
            // User restrictions (Device Owner only)
            if (mdmManager.isDeviceOwner() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                policy.userRestrictions.forEach { restriction ->
                    mdmManager.setUserRestriction(restriction, true)
                }
            }
            
            Timber.d("Security policy applied: $success")
            success
        } catch (e: Exception) {
            Timber.e(e, "Error applying security policy")
            false
        }
    }
    
    fun removeSecurityPolicy(): Boolean {
        return try {
            mdmManager.disableCamera(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mdmManager.setStorageEncryption(false)
            }
            Timber.d("Security policy removed")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error removing security policy")
            false
        }
    }
}

data class SecurityPolicy(
    val enforcePasswordPolicy: Boolean = false,
    val minPasswordLength: Int = 6,
    val requireNumeric: Boolean = false,
    val requireLetters: Boolean = false,
    val disableCamera: Boolean = false,
    val requireStorageEncryption: Boolean = false,
    val userRestrictions: List<String> = emptyList()
)
