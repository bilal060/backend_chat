package com.chats.controller.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chats.controller.R
import com.chats.controller.auth.AuthManager
import com.chats.controller.databinding.ActivityMainBinding
import com.chats.controller.network.ApiClient
import com.chats.controller.ui.auth.LoginActivity
import com.chats.controller.ui.devices.DeviceListFragment
import com.chats.controller.ui.settings.SettingsActivity
import com.chats.controller.utils.RealtimeUpdateManager
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is logged in
        if (!AuthManager.isLoggedIn(this)) {
            navigateToLogin()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        // Load user info and fetch assigned device ID if device owner
        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getCurrentUser()
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.data
                    user?.let {
                        AuthManager.saveUser(this@MainActivity, it)
                        // If device owner, save assigned device ID
                        if (it.isDeviceOwner && it.deviceId != null) {
                            AuthManager.saveAssignedDeviceId(this@MainActivity, it.deviceId)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching user info")
            }
        }
        
        // Initialize WebSocket for real-time updates
        val prefs = getSharedPreferences("controller_prefs", MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", "http://192.168.1.169:3000/")
        val realtimeManager = RealtimeUpdateManager.getInstance(this)
        if (!realtimeManager.isConnected()) {
            realtimeManager.initialize(serverUrl ?: "http://192.168.1.169:3000/")
        }
        
        // Load device list fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DeviceListFragment())
                .commit()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                logout()
                true
            }
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun logout() {
        // Disconnect WebSocket
        RealtimeUpdateManager.getInstance(this).disconnect()
        
        AuthManager.logout(this)
        navigateToLogin()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
