package com.chats.controller.ui.devices

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chats.controller.R
import com.chats.controller.auth.AuthManager
import com.chats.controller.databinding.FragmentDeviceListBinding
import com.chats.controller.models.Device
import com.chats.controller.network.ApiClient
import com.chats.controller.utils.RealtimeUpdateManager
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceListFragment : Fragment() {
    
    private var _binding: FragmentDeviceListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var deviceAdapter: DeviceAdapter
    private val devices = mutableListOf<Device>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadDevices()
        setupRealtimeUpdates()
        
        // Setup pull-to-refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadDevices()
        }
    }
    
    private fun setupRealtimeUpdates() {
        val realtimeManager = RealtimeUpdateManager.getInstance(requireContext())
        val webSocketService = realtimeManager.getWebSocketService()
        
        // Listen for device status updates (online/offline, heartbeat)
        webSocketService.setDeviceStatusUpdateListener { deviceId, status ->
            // Update device status in the list
            updateDeviceStatus(deviceId, status)
        }
        
        // Listen for data updates - could trigger a refresh if needed
        webSocketService.setDataUpdateListener { deviceId, type, data ->
            Timber.d("Real-time data update received: deviceId=$deviceId, type=$type")
            // Optionally refresh device list or specific device
            // For now, we'll just update the lastSeen timestamp by refreshing
            refreshDeviceIfVisible(deviceId)
        }
    }
    
    private fun updateDeviceStatus(deviceId: String, status: String) {
        val deviceIndex = devices.indexOfFirst { it.deviceId == deviceId }
        if (deviceIndex >= 0) {
            // Update device status
            val device = devices[deviceIndex]
            val updatedDevice = device.copy(
                status = status,
                lastSeen = System.currentTimeMillis() // Update last seen when status changes
            )
            devices[deviceIndex] = updatedDevice
            deviceAdapter.notifyItemChanged(deviceIndex)
        }
    }
    
    private fun refreshDeviceIfVisible(deviceId: String) {
        // If the device is in our list, refresh its data
        val deviceIndex = devices.indexOfFirst { it.deviceId == deviceId }
        if (deviceIndex >= 0) {
            // Update lastSeen timestamp
            val device = devices[deviceIndex]
            val updatedDevice = device.copy(lastSeen = System.currentTimeMillis())
            devices[deviceIndex] = updatedDevice
            deviceAdapter.notifyItemChanged(deviceIndex)
        }
    }
    
    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(devices) { device ->
            // Navigate to device detail
            val intent = DeviceDetailActivity.newIntent(requireContext(), device.deviceId)
            startActivity(intent)
        }
        
        binding.recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }
    
    private fun loadDevices() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.textViewEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getDevices()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val deviceList = response.body()?.data ?: emptyList()
                    
                    devices.clear()
                    devices.addAll(deviceList)
                    deviceAdapter.notifyDataSetChanged()
                    
                    // Show empty state if no devices
                    if (devices.isEmpty()) {
                        binding.textViewEmpty.visibility = View.VISIBLE
                        binding.textViewEmpty.text = getString(R.string.no_devices)
                    } else {
                        binding.textViewEmpty.visibility = View.GONE
                    }
                } else {
                    val errorMessage = response.body()?.message ?: response.message()
                    Toast.makeText(requireContext(), errorMessage ?: "Failed to load devices", Toast.LENGTH_LONG).show()
                    Timber.e("Failed to load devices: $errorMessage")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading devices")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
