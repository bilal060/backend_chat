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
import com.chats.capture.ui.adapters.ChatsAdapter
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerViewChats)
        adapter = ChatsAdapter()
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        loadChats()
    }
    
    private fun loadChats() {
        val database = (requireActivity().application as CaptureApplication).database
        val chatDao = database.chatDao()
        
        lifecycleScope.launch {
            chatDao.getAllChats().collect { chats ->
                adapter.submitList(chats)
                Timber.d("Loaded ${chats.size} chats")
            }
        }
    }
}
