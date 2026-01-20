package com.chats.capture.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chats.capture.R
import com.chats.capture.mdm.AppInfo
import com.google.android.material.button.MaterialButton

class AppsAdapter(
    private val onUninstallClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppsAdapter.ViewHolder>(DiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return ViewHolder(view, onUninstallClick)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ViewHolder(
        itemView: View,
        private val onUninstallClick: (AppInfo) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val appName: TextView = itemView.findViewById(R.id.textViewAppName)
        private val packageName: TextView = itemView.findViewById(R.id.textViewPackageName)
        private val uninstallButton: MaterialButton = itemView.findViewById(R.id.buttonUninstall)
        
        fun bind(appInfo: AppInfo) {
            appName.text = appInfo.appName
            packageName.text = appInfo.packageName
            
            uninstallButton.setOnClickListener {
                onUninstallClick(appInfo)
            }
        }
    }
    
    class DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.packageName == newItem.packageName
        }
        
        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem == newItem
        }
    }
}
