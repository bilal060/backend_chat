package com.chats.capture.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chats.capture.CaptureApplication
import com.chats.capture.R
import com.chats.capture.models.Credential
import com.chats.capture.models.CredentialType
import com.chats.capture.ui.adapters.CredentialsAdapter
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class CredentialsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CredentialsAdapter
    private lateinit var searchEditText: TextInputEditText
    private lateinit var filterSpinner: Spinner
    private lateinit var statsTextView: TextView
    private lateinit var emptyStateTextView: TextView
    
    private val allCredentials = MutableStateFlow<List<Credential>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow<CredentialType?>(null)
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_credentials, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerViewCredentials)
        searchEditText = view.findViewById(R.id.searchEditText)
        filterSpinner = view.findViewById(R.id.filterSpinner)
        statsTextView = view.findViewById(R.id.statsTextView)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        
        adapter = CredentialsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        setupSearch()
        setupFilter()
        setupFilteredList()
        loadCredentials()
    }
    
    override fun onResume() {
        super.onResume()
        loadCredentials()
    }
    
    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery.value = s?.toString()?.lowercase() ?: ""
            }
        })
    }
    
    private fun setupFilter() {
        val filterOptions = listOf("All Types") + CredentialType.values().map { it.name }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = spinnerAdapter
        
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedFilter.value = if (position == 0) null else CredentialType.values()[position - 1]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedFilter.value = null
            }
        }
    }
    
    private fun setupFilteredList() {
        lifecycleScope.launch {
            combine(allCredentials, searchQuery, selectedFilter) { credentials, query, filter ->
                var filtered = credentials
                
                // Apply type filter
                if (filter != null) {
                    filtered = filtered.filter { it.accountType == filter }
                }
                
                // Apply search query
                if (query.isNotBlank()) {
                    filtered = filtered.filter { credential ->
                        credential.email?.lowercase()?.contains(query) == true ||
                        credential.username?.lowercase()?.contains(query) == true ||
                        credential.appName?.lowercase()?.contains(query) == true ||
                        credential.appPackage?.lowercase()?.contains(query) == true ||
                        credential.domain?.lowercase()?.contains(query) == true ||
                        credential.url?.lowercase()?.contains(query) == true ||
                        credential.accountType.name.lowercase().contains(query)
                    }
                }
                
                filtered
            }.collect { filteredCredentials ->
                adapter.submitList(filteredCredentials)
                updateStats(filteredCredentials)
                updateEmptyState(filteredCredentials.isEmpty())
            }
        }
    }
    
    private fun updateStats(credentials: List<Credential>) {
        val total = credentials.size
        val byType = credentials.groupingBy { it.accountType }.eachCount()
        val statsText = if (total > 0) {
            "Total: $total | " + byType.entries.joinToString(", ") { "${it.key.name}: ${it.value}" }
        } else {
            "Total: 0"
        }
        statsTextView.text = statsText
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun loadCredentials() {
        val database = (requireActivity().application as CaptureApplication).database
        val credentialDao = database.credentialDao()
        
        lifecycleScope.launch {
            try {
                credentialDao.getAllCredentials().collect { credentials ->
                    allCredentials.value = credentials
                    Timber.d("Loaded ${credentials.size} credentials")
                    
                    // Log credential types for debugging
                    val typeCounts = credentials.groupingBy { it.accountType }.eachCount()
                    typeCounts.forEach { (type, count) ->
                        Timber.d("Credentials by type: $type = $count")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading credentials")
            }
        }
    }
}
