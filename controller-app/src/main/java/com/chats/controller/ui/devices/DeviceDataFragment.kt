package com.chats.controller.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chats.controller.databinding.FragmentDeviceDataBinding
import com.google.android.material.tabs.TabLayoutMediator

class DeviceDataFragment : Fragment() {
    
    private var _binding: FragmentDeviceDataBinding? = null
    private val binding get() = _binding!!
    
    private var deviceId: String? = null
    
    companion object {
        private const val ARG_DEVICE_ID = "device_id"
        
        fun newInstance(deviceId: String): DeviceDataFragment {
            return DeviceDataFragment().apply {
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
        _binding = FragmentDeviceDataBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        deviceId?.let { id ->
            // Setup ViewPager2 with tabs
            val adapter = DeviceDataPagerAdapter(requireActivity(), id)
            binding.viewPager.adapter = adapter
            
            // Setup TabLayout
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Notifications"
                    1 -> "Chats"
                    2 -> "Credentials"
                    3 -> "Screenshots"
                    4 -> "Commands"
                    else -> ""
                }
            }.attach()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
