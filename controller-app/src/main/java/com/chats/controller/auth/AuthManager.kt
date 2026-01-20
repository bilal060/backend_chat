package com.chats.controller.auth

import android.content.Context
import android.content.SharedPreferences
import com.chats.controller.models.User
import com.google.gson.Gson
import timber.log.Timber

object AuthManager {
    private const val PREFS_NAME = "controller_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER = "user"
    private const val KEY_ASSIGNED_DEVICE_ID = "assigned_device_id"
    
    private val gson = Gson()
    
    fun saveAuthToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
        Timber.d("Auth token saved")
    }
    
    fun getAuthToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    fun saveUser(context: Context, user: User) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
        Timber.d("User saved: ${user.role}")
    }
    
    fun getUser(context: Context): User? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error parsing user from preferences")
            null
        }
    }
    
    fun saveAssignedDeviceId(context: Context, deviceId: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (deviceId != null) {
            prefs.edit().putString(KEY_ASSIGNED_DEVICE_ID, deviceId).apply()
        } else {
            prefs.edit().remove(KEY_ASSIGNED_DEVICE_ID).apply()
        }
    }
    
    fun getAssignedDeviceId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ASSIGNED_DEVICE_ID, null)
    }
    
    fun isLoggedIn(context: Context): Boolean {
        return getAuthToken(context) != null && getUser(context) != null
    }
    
    fun logout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER)
            .remove(KEY_ASSIGNED_DEVICE_ID)
            .apply()
        Timber.d("User logged out")
    }
    
    fun isAdmin(context: Context): Boolean {
        return getUser(context)?.isAdmin == true
    }
    
    fun isDeviceOwner(context: Context): Boolean {
        return getUser(context)?.isDeviceOwner == true
    }
}
