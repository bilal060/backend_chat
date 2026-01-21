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
import com.chats.controller.models.Notification
import com.chats.controller.network.ApiClient
import com.chats.controller.utils.RealtimeUpdateManager
import coil.load
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment : Fragment() {
    
    private var _binding: FragmentDataListBinding? = null
    private val binding get() = _binding!!
    private var deviceId: String? = null
    private lateinit var adapter: NotificationAdapter
    private var isLoading = false
    
    companion object {
        private const val ARG_DEVICE_ID = "device_id"
        
        fun newInstance(deviceId: String): NotificationsFragment {
            return NotificationsFragment().apply {
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
        
        adapter = NotificationAdapter()
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
            
            // Listen for notification updates
            // Note: This will overwrite any existing listener, but since fragments
            // are shown one at a time in ViewPager, this should work fine
            webSocketService.setDataUpdateListener { updateDeviceId, type, data ->
                // Only process if fragment is still attached and matches our device
                if (isAdded && updateDeviceId == id && type == "notification") {
                    // Refresh notifications when new data arrives
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
                    val response = ApiClient.getApiService().getDeviceNotifications(id)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val notifications = response.body()?.data ?: emptyList()
                        if (notifications.isEmpty()) {
                            binding.textViewEmpty.text = getString(R.string.no_notifications)
                            binding.textViewEmpty.visibility = View.VISIBLE
                        } else {
                            adapter.submitList(notifications)
                        }
                    } else {
                        binding.textViewEmpty.text = getString(R.string.error)
                        binding.textViewEmpty.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error loading notifications")
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

class NotificationAdapter : androidx.recyclerview.widget.ListAdapter<Notification, NotificationAdapter.ViewHolder>(
    NotificationDiffCallback()
) {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position)
        holder.bind(notification)
    }
    
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        fun bind(notification: Notification) {
            val icon = itemView.findViewById<android.widget.ImageView>(R.id.icon)
            val text1 = itemView.findViewById<android.widget.TextView>(R.id.title)
            val text2 = itemView.findViewById<android.widget.TextView>(R.id.subtitle)
            
            text1.text = notification.title ?: notification.appName
            val details = "${notification.text ?: ""}\n${notification.appName} • ${dateFormat.format(Date(notification.timestamp))}"
            text2.text = details

            icon.load(notification.iconUrl) {
                placeholder(android.R.drawable.sym_def_app_icon)
                error(android.R.drawable.sym_def_app_icon)
            }
        }
    }
}

class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
    override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem.id == newItem.id
    }
    
    override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
        return oldItem == newItem
    }
}
