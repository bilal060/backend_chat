package com.chats.controller.ui.devices

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chats.controller.R
import com.chats.controller.models.Device
import java.text.SimpleDateFormat
import java.util.*

class DeviceAdapter(
    private val devices: List<Device>,
    private val onDeviceClick: (Device) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)
    }
    
    override fun getItemCount() = devices.size
    
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewDeviceName: TextView = itemView.findViewById(R.id.textViewDeviceName)
        private val textViewDeviceId: TextView = itemView.findViewById(R.id.textViewDeviceId)
        private val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        private val textViewLastSeen: TextView = itemView.findViewById(R.id.textViewLastSeen)
        private val textViewModel: TextView = itemView.findViewById(R.id.textViewModel)
        
        fun bind(device: Device) {
            textViewDeviceName.text = device.displayName
            textViewDeviceId.text = device.deviceId
            
            // Status
            val isOnline = device.isOnline
            textViewStatus.text = if (isOnline) {
                itemView.context.getString(R.string.device_online)
            } else {
                itemView.context.getString(R.string.device_offline)
            }
            textViewStatus.setTextColor(
                itemView.context.getColor(
                    if (isOnline) R.color.status_online else R.color.status_offline
                )
            )
            
            // Last seen
            if (device.lastSeen != null) {
                val lastSeenDate = Date(device.lastSeen)
                textViewLastSeen.text = itemView.context.getString(
                    R.string.last_seen,
                    dateFormat.format(lastSeenDate)
                )
            } else {
                textViewLastSeen.text = itemView.context.getString(R.string.last_seen, "Never")
            }
            
            // Model
            if (device.model != null) {
                textViewModel.text = itemView.context.getString(R.string.model, device.model)
                textViewModel.visibility = View.VISIBLE
            } else {
                textViewModel.visibility = View.GONE
            }
            
            // Click listener
            itemView.setOnClickListener {
                onDeviceClick(device)
            }
        }
    }
}
