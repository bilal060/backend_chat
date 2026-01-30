package com.chats.controller.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chats.controller.R
import com.chats.controller.databinding.ActivitySettingsBinding
import com.chats.controller.network.ApiClient
import com.chats.controller.utils.RealtimeUpdateManager
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.nav_settings)
        
        // Load current server URL
        val prefs = getSharedPreferences("controller_prefs", MODE_PRIVATE)
        val currentUrl = prefs.getString("server_url", "http://192.168.1.169:3000/")
        binding.editServerUrl.setText(currentUrl)
        
        // Save button click
        binding.buttonSave.setOnClickListener {
            saveSettings()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
    
    private fun saveSettings() {
        val serverUrl = binding.editServerUrl.text.toString().trim()
        
        if (serverUrl.isEmpty()) {
            binding.editServerUrl.error = getString(R.string.server_url_required)
            return
        }
        
        // Validate URL format
        if (!android.util.Patterns.WEB_URL.matcher(serverUrl).matches() && 
            !serverUrl.startsWith("http://") && 
            !serverUrl.startsWith("https://")) {
            binding.editServerUrl.error = getString(R.string.invalid_url)
            return
        }
        
        // Normalize URL (ensure it ends with /)
        val normalizedUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        
        // Save to SharedPreferences
        val prefs = getSharedPreferences("controller_prefs", MODE_PRIVATE)
        prefs.edit().putString("server_url", normalizedUrl).apply()
        
        // Update ApiClient
        ApiClient.updateBaseUrl(this, normalizedUrl)
        
        // Reconnect WebSocket if user is logged in
        lifecycleScope.launch {
            try {
                val realtimeManager = RealtimeUpdateManager.getInstance(this@SettingsActivity)
                realtimeManager.disconnect()
                realtimeManager.initialize(normalizedUrl)
            } catch (e: Exception) {
                Timber.e(e, "Error reconnecting WebSocket")
            }
        }
        
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
        Timber.d("Server URL updated to: $normalizedUrl")
        
        // Close settings
        finish()
    }
}
