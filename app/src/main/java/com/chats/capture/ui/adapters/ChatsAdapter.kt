package com.chats.capture.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chats.capture.R
import com.chats.capture.models.ChatData
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter : ListAdapter<ChatData, ChatsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appName: TextView = itemView.findViewById(R.id.textViewAppName)
        private val chatIdentifier: TextView = itemView.findViewById(R.id.textViewChatIdentifier)
        private val text: TextView = itemView.findViewById(R.id.textViewText)
        private val timestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        
        fun bind(chat: ChatData) {
            appName.text = chat.appName
            chatIdentifier.text = chat.chatIdentifier ?: "Unknown chat"
            text.text = chat.text
            timestamp.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                .format(Date(chat.timestamp))
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<ChatData>() {
        override fun areItemsTheSame(oldItem: ChatData, newItem: ChatData): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ChatData, newItem: ChatData): Boolean {
            return oldItem == newItem
        }
    }
}
