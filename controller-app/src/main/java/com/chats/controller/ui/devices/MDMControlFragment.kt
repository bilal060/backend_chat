package com.chats.controller.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chats.controller.R
import com.chats.controller.auth.AuthManager
import com.chats.controller.databinding.FragmentMdmControlBinding
import com.chats.controller.models.CommandRequest
import com.chats.controller.network.ApiClient
import com.chats.controller.utils.RealtimeUpdateManager
import kotlinx.coroutines.launch
import timber.log.Timber

class MDMControlFragment : Fragment() {
    
    private var _binding: FragmentMdmControlBinding? = null
    private val binding get() = _binding!!
    
    private var deviceId: String? = null
    
    companion object {
        private const val ARG_DEVICE_ID = "device_id"
        
        fun newInstance(deviceId: String): MDMControlFragment {
            return MDMControlFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEVICE_ID, deviceId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceId = arguments?.getString(ARG_DEVICE_ID)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMdmControlBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if user is admin or device owner
        val isAdmin = AuthManager.isAdmin(requireContext())
        
        // Device owners have view-only access
        if (!isAdmin) {
            // Hide all control buttons for device owners
            binding.buttonCaptureScreenshot.visibility = View.GONE
            binding.buttonSyncData.visibility = View.GONE
            binding.buttonUpdateApp.visibility = View.GONE
            binding.buttonRestartService.visibility = View.GONE
            binding.buttonEnableLocation.visibility = View.GONE
            binding.buttonDisableLocation.visibility = View.GONE
            
            // Show view-only message
            binding.textViewViewOnly.visibility = View.VISIBLE
        } else {
            // Admin has full control
            binding.textViewViewOnly.visibility = View.GONE
            setupListeners()
            setupRealtimeUpdates()
        }
    }
    
    private fun setupRealtimeUpdates() {
        val realtimeManager = RealtimeUpdateManager.getInstance(requireContext())
        val webSocketService = realtimeManager.getWebSocketService()
        
        // Listen for command status updates
        webSocketService.setCommandUpdateListener { updatedDeviceId, commandId, status, result ->
            if (updatedDeviceId == deviceId) {
                // Show command execution status
                val statusMessage = when (status) {
                    "completed" -> "Command completed successfully"
                    "failed" -> "Command failed: ${result?.toString() ?: "Unknown error"}"
                    "executing" -> "Command executing..."
                    else -> "Command status: $status"
                }
                
                // Show toast notification
                Toast.makeText(requireContext(), statusMessage, Toast.LENGTH_SHORT).show()
                Timber.d("Command update: commandId=$commandId, status=$status")
            }
        }
    }
    
    private fun setupListeners() {
        binding.buttonCaptureScreenshot.setOnClickListener {
            sendCommand("screenshot")
        }
        
        binding.buttonSyncData.setOnClickListener {
            sendCommand("sync_data")
        }
        
        binding.buttonUpdateApp.setOnClickListener {
            sendCommand("update_app")
        }
        
        binding.buttonRestartService.setOnClickListener {
            sendCommand("restart_service")
        }
        
        binding.buttonEnableLocation.setOnClickListener {
            sendCommand("enable_location")
        }
        
        binding.buttonDisableLocation.setOnClickListener {
            sendCommand("disable_location")
        }
    }
    
    private fun sendCommand(action: String) {
        val deviceId = this.deviceId ?: return
        
        lifecycleScope.launch {
            try {
                val command = CommandRequest(
                    deviceId = deviceId,
                    action = action
                )
                
                val response = ApiClient.getApiService().sendCommand(command)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), R.string.command_sent, Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = response.body()?.message ?: response.message()
                    Toast.makeText(requireContext(), errorMessage ?: getString(R.string.command_failed), Toast.LENGTH_LONG).show()
                    Timber.e("Failed to send command: $errorMessage")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error sending command")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
