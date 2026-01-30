package com.chats.controller

import android.app.Application
import com.chats.controller.auth.AuthManager
import com.chats.controller.network.ApiClient
import com.chats.controller.utils.RealtimeUpdateManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class ControllerApplication : Application() {
    
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "controller_prefs")
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize API client with default server URL
        val prefs = getSharedPreferences("controller_prefs", MODE_PRIVATE)
        val defaultUrl = "http://192.168.1.169:3000/"
        val serverUrl = prefs.getString("server_url", defaultUrl)
        ApiClient.initialize(this, serverUrl ?: defaultUrl)
        
        // Initialize WebSocket if user is logged in
        if (AuthManager.isLoggedIn(this)) {
            val realtimeManager = RealtimeUpdateManager.getInstance(this)
            realtimeManager.initialize(serverUrl ?: defaultUrl)
        }
    }
}
