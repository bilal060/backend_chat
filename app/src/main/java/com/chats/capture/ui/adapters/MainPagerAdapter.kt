package com.chats.capture.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.chats.capture.ui.fragments.ChatsFragment
import com.chats.capture.ui.fragments.CredentialsFragment
import com.chats.capture.ui.fragments.MDMManagementFragment
import com.chats.capture.ui.fragments.NotificationsFragment
import com.chats.capture.ui.fragments.SettingsFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 5 // Added Credentials fragment
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NotificationsFragment()
            1 -> ChatsFragment()
            2 -> CredentialsFragment() // Credentials fragment
            3 -> MDMManagementFragment()
            4 -> SettingsFragment()
            else -> NotificationsFragment()
        }
    }
}
