package com.chats.controller.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.chats.controller.R
import com.chats.controller.databinding.FragmentDataListBinding
import com.chats.controller.models.Screenshot
import com.chats.controller.network.ApiClient
import com.chats.controller.utils.RealtimeUpdateManager
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class ScreenshotsFragment : Fragment() {
    
    private var _binding: FragmentDataListBinding? = null
    private val binding get() = _binding!!
    private var deviceId: String? = null
    private lateinit var adapter: ScreenshotAdapter
    private var isLoading = false
    
    companion object {
        private const val ARG_DEVICE_ID = "device_id"
        
        fun newInstance(deviceId: String): ScreenshotsFragment {
            return ScreenshotsFragment().apply {
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
        _binding = FragmentDataListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = ScreenshotAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        
        // Setup pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
        
        // Setup real-time updates
        setupRealtimeUpdates()
        
        loadData()
    }
    
    private fun setupRealtimeUpdates() {
        deviceId?.let { id ->
            val realtimeManager = RealtimeUpdateManager.getInstance(requireContext())
            val webSocketService = realtimeManager.getWebSocketService()
            
            // Listen for screenshot updates
            // Note: This will overwrite any existing listener, but since fragments
            // are shown one at a time in ViewPager, this should work fine
            webSocketService.setDataUpdateListener { updateDeviceId, type, data ->
                // Only process if fragment is still attached and matches our device
                if (isAdded && updateDeviceId == id && type == "screenshot") {
                    // Refresh screenshots when new data arrives
                    loadData()
                }
            }
        }
    }
    
    private fun loadData() {
        // Prevent multiple simultaneous loads
        if (isLoading) return
        
        deviceId?.let { id ->
            isLoading = true
            binding.progressBar.visibility = View.VISIBLE
            binding.textViewEmpty.visibility = View.GONE
            
            lifecycleScope.launch {
                try {
                    val response = ApiClient.getApiService().getDeviceScreenshots(id)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val screenshots = response.body()?.screenshots ?: emptyList()
                        if (screenshots.isEmpty()) {
                            binding.textViewEmpty.text = getString(R.string.no_screenshots)
                            binding.textViewEmpty.visibility = View.VISIBLE
                        } else {
                            adapter.submitList(screenshots)
                        }
                    } else {
                        binding.textViewEmpty.text = getString(R.string.error)
                        binding.textViewEmpty.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading screenshots")
                    binding.textViewEmpty.text = getString(R.string.error)
                    binding.textViewEmpty.visibility = View.VISIBLE
                } finally {
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ScreenshotAdapter : androidx.recyclerview.widget.ListAdapter<Screenshot, ScreenshotAdapter.ViewHolder>(
    ScreenshotDiffCallback()
) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val screenshot = getItem(position)
        holder.bind(screenshot)
    }
    
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(screenshot: Screenshot) {
            val text1 = itemView.findViewById<android.widget.TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<android.widget.TextView>(android.R.id.text2)
            
            text1.text = "Screenshot"
            text2.text = dateFormat.format(Date(screenshot.timestamp))
        }
    }
}

class ScreenshotDiffCallback : DiffUtil.ItemCallback<Screenshot>() {
    override fun areItemsTheSame(oldItem: Screenshot, newItem: Screenshot): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Screenshot, newItem: Screenshot): Boolean {
        return oldItem == newItem
    }
}
