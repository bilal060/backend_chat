package com.chats.capture.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chats.capture.R
import com.chats.capture.models.NotificationData
import java.text.SimpleDateFormat
import java.util.*

class NotificationsAdapter : ListAdapter<NotificationData, NotificationsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appName: TextView = itemView.findViewById(R.id.textViewAppName)
        private val title: TextView = itemView.findViewById(R.id.textViewTitle)
        private val text: TextView = itemView.findViewById(R.id.textViewText)
        private val timestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        
        fun bind(notification: NotificationData) {
            appName.text = notification.appName
            title.text = notification.title ?: "No title"
            text.text = notification.text ?: "No text"
            timestamp.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                .format(Date(notification.timestamp))
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<NotificationData>() {
        override fun areItemsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
            return oldItem == newItem
        }
    }
}
