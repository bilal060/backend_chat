package com.chats.controller.ui.devices

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.chats.controller.R
import com.chats.controller.auth.AuthManager
import com.chats.controller.databinding.ActivityDeviceDetailBinding
import com.chats.controller.models.Device
import com.chats.controller.network.ApiClient
import com.chats.controller.utils.RealtimeUpdateManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityDeviceDetailBinding
    private var deviceId: String? = null
    
    companion object {
        private const val EXTRA_DEVICE_ID = "device_id"
        
        fun newIntent(context: Context, deviceId: String): Intent {
            return Intent(context, DeviceDetailActivity::class.java).apply {
                putExtra(EXTRA_DEVICE_ID, deviceId)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        deviceId = intent.getStringExtra(EXTRA_DEVICE_ID)
        if (deviceId == null) {
            finish()
            return
        }
        
        binding = ActivityDeviceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        loadDeviceDetails()
        setupRealtimeUpdates()
        
        // Setup tabs for MDM Controls and Data
        if (savedInstanceState == null) {
            setupTabs()
        }
    }
    
    private fun setupRealtimeUpdates() {
        val realtimeManager = RealtimeUpdateManager.getInstance(this)
        val webSocketService = realtimeManager.getWebSocketService()
        
        // Listen for device status updates
        webSocketService.setDeviceStatusUpdateListener { updatedDeviceId, status ->
            if (updatedDeviceId == deviceId) {
                // Refresh device details when status changes
                loadDeviceDetails()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cleanup is handled by singleton, but we could remove listeners if needed
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun loadDeviceDetails() {
        deviceId?.let { id ->
            lifecycleScope.launch {
                try {
                    val response = ApiClient.getApiService().getDevice(id)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val device = response.body()?.data
                        device?.let {
                            updateUI(it)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading device details")
                }
            }
        }
    }
    
    private fun setupTabs() {
        deviceId?.let { id ->
            val adapter = DeviceDetailPagerAdapter(this, id)
            binding.viewPager.adapter = adapter
            
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.tab_mdm_controls)
                    1 -> getString(R.string.device_data)
                    else -> ""
                }
            }.attach()
        }
    }
    
    private fun updateUI(device: Device) {
        supportActionBar?.title = device.displayName
        binding.textViewDeviceId.text = device.deviceId
        binding.textViewModel.text = device.model ?: "Unknown"
        binding.textViewOsVersion.text = device.osVersion ?: "Unknown"
        binding.textViewStatus.text = if (device.isOnline) {
            getString(R.string.device_online)
        } else {
            getString(R.string.device_offline)
        }
    }
}

class DeviceDetailPagerAdapter(
    fragmentActivity: AppCompatActivity,
    private val deviceId: String
) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MDMControlFragment.newInstance(deviceId)
            1 -> DeviceDataFragment.newInstance(deviceId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
