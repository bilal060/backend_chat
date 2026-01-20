package com.chats.controller.ui.remote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chats.controller.R
import com.chats.controller.auth.AuthManager
import com.chats.controller.databinding.FragmentRemoteControlBinding
import com.chats.controller.models.CommandRequest
import com.chats.controller.models.UICommand
import com.chats.controller.network.ApiClient
import kotlinx.coroutines.launch

class RemoteControlFragment : Fragment() {
    
    private var _binding: FragmentRemoteControlBinding? = null
    private val binding get() = _binding!!
    
    private var deviceId: String? = null
    private var selectedPackage: String? = null
    
    companion object {
        private const val ARG_DEVICE_ID = "device_id"
        
        fun newInstance(deviceId: String): RemoteControlFragment {
            return RemoteControlFragment().apply {
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
        _binding = FragmentRemoteControlBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if user is admin (only admins can use remote control)
        if (!AuthManager.isAdmin(requireContext())) {
            binding.textViewAccessDenied.visibility = View.VISIBLE
            binding.scrollViewControls.visibility = View.GONE
            return
        }
        
        setupViews()
        setupListeners()
        loadInstalledApps()
    }
    
    private fun setupViews() {
        // Setup app selector
        binding.spinnerApp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPackage = binding.spinnerApp.selectedItem as? String
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPackage = null
            }
        }
    }
    
    private fun setupListeners() {
        // Click at coordinates
        binding.buttonClickCoordinates.setOnClickListener {
            val x = binding.editTextX.text.toString().toFloatOrNull() ?: 0f
            val y = binding.editTextY.text.toString().toFloatOrNull() ?: 0f
            sendUICommand(UICommand.click(x, y, selectedPackage))
        }
        
        // Find and click by text
        binding.buttonFindAndClick.setOnClickListener {
            val text = binding.editTextFindText.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Enter text to find", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendUICommand(UICommand.findAndClick(text, selectedPackage))
        }
        
        // Find and click by ID
        binding.buttonFindAndClickById.setOnClickListener {
            val viewId = binding.editTextViewId.text.toString()
            if (viewId.isEmpty()) {
                Toast.makeText(requireContext(), "Enter view ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendUICommand(UICommand.findAndClickById(viewId, selectedPackage))
        }
        
        // Input text
        binding.buttonInputText.setOnClickListener {
            val text = binding.editTextInputText.text.toString()
            if (text.isEmpty()) {
                Toast.makeText(requireContext(), "Enter text to input", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val findText = binding.editTextInputFindText.text.toString().takeIf { it.isNotEmpty() }
            val viewId = binding.editTextInputViewId.text.toString().takeIf { it.isNotEmpty() }
            sendUICommand(UICommand.input(text, findText, viewId, selectedPackage))
        }
        
        // Scroll
        binding.buttonScrollUp.setOnClickListener {
            sendUICommand(UICommand.scroll("up", selectedPackage))
        }
        binding.buttonScrollDown.setOnClickListener {
            sendUICommand(UICommand.scroll("down", selectedPackage))
        }
        binding.buttonScrollLeft.setOnClickListener {
            sendUICommand(UICommand.scroll("left", selectedPackage))
        }
        binding.buttonScrollRight.setOnClickListener {
            sendUICommand(UICommand.scroll("right", selectedPackage))
        }
        
        // Swipe
        binding.buttonSwipe.setOnClickListener {
            val startX = binding.editTextSwipeStartX.text.toString().toFloatOrNull() ?: 0f
            val startY = binding.editTextSwipeStartY.text.toString().toFloatOrNull() ?: 0f
            val endX = binding.editTextSwipeEndX.text.toString().toFloatOrNull() ?: 0f
            val endY = binding.editTextSwipeEndY.text.toString().toFloatOrNull() ?: 0f
            sendUICommand(UICommand.swipe(startX, startY, endX, endY))
        }
        
        // Launch app
        binding.buttonLaunchApp.setOnClickListener {
            val packageName = selectedPackage
            if (packageName == null || packageName.isEmpty()) {
                Toast.makeText(requireContext(), "Select an app first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendUICommand(UICommand.launchApp(packageName))
        }
    }
    
    private fun loadInstalledApps() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.getApiService().getDevice(deviceId ?: return@launch)
                if (response.isSuccessful && response.body()?.success == true) {
                    // For now, use common app packages
                    val commonApps = listOf(
                        "com.whatsapp",
                        "com.instagram.android",
                        "com.facebook.katana",
                        "com.facebook.orca",
                        "org.telegram.messenger",
                        "com.snapchat.android",
                        "com.twitter.android",
                        "com.discord",
                        "com.viber.voip",
                        "com.skype.raider"
                    )
                    
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        commonApps
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    binding.spinnerApp.adapter = adapter
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun sendUICommand(uiCommand: UICommand) {
        val deviceId = this.deviceId ?: return
        
        lifecycleScope.launch {
            try {
                val command = CommandRequest(
                    deviceId = deviceId,
                    action = uiCommand.action,
                    parameters = uiCommand.parameters.mapValues { it.value.toString() }
                )
                
                val response = ApiClient.getApiService().sendCommand(command)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Command sent", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMessage = response.body()?.message ?: response.message()
                    Toast.makeText(requireContext(), errorMessage ?: "Command failed", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
