package com.chats.capture.mdm

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import timber.log.Timber

class DeviceOwnerReceiver : DeviceAdminReceiver() {
    
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Timber.d("Device Owner enabled - Full MDM capabilities active")
    }
    
    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Timber.w("Device Owner disabled")
    }
    
    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Timber.d("Kiosk mode entered for package: $pkg")
    }
    
    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Timber.d("Kiosk mode exited")
    }
    
    override fun onUserAdded(context: Context, intent: Intent, addedUser: android.os.UserHandle) {
        super.onUserAdded(context, intent, addedUser)
        val userId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            addedUser.hashCode().toString()
        } else {
            "unknown"
        }
        Timber.d("User added: $userId")
    }
    
    override fun onUserRemoved(context: Context, intent: Intent, removedUser: android.os.UserHandle) {
        super.onUserRemoved(context, intent, removedUser)
        val userId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            removedUser.hashCode().toString()
        } else {
            "unknown"
        }
        Timber.d("User removed: $userId")
    }
    
    override fun onUserStarted(context: Context, intent: Intent, startedUser: android.os.UserHandle) {
        super.onUserStarted(context, intent, startedUser)
        val userId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            startedUser.hashCode().toString()
        } else {
            "unknown"
        }
        Timber.d("User started: $userId")
    }
    
    override fun onUserStopped(context: Context, intent: Intent, stoppedUser: android.os.UserHandle) {
        super.onUserStopped(context, intent, stoppedUser)
        val userId = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            stoppedUser.hashCode().toString()
        } else {
            "unknown"
        }
        Timber.d("User stopped: $userId")
    }
    
    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, DeviceOwnerReceiver::class.java)
        }
    }
}
