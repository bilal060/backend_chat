package com.chats.capture.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chats.capture.CaptureApplication
import com.chats.capture.R
import com.chats.capture.ui.adapters.NotificationsAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class NotificationsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerViewNotifications)
        adapter = NotificationsAdapter()
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        loadNotifications()
    }
    
    private fun loadNotifications() {
        val database = (requireActivity().application as CaptureApplication).database
        val notificationDao = database.notificationDao()
        
        lifecycleScope.launch {
            notificationDao.getAllNotifications().collect { notifications ->
                adapter.submitList(notifications)
                Timber.d("Loaded ${notifications.size} notifications")
            }
        }
    }
}
