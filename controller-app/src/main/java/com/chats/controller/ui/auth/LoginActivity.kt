package com.chats.controller.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chats.controller.R
import com.chats.controller.auth.AuthManager
import com.chats.controller.databinding.ActivityLoginBinding
import com.chats.controller.network.ApiClient
import com.chats.controller.network.LoginRequest
import com.chats.controller.ui.MainActivity
import com.chats.controller.utils.RealtimeUpdateManager
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private var isAdminMode = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if already logged in
        if (AuthManager.isLoggedIn(this)) {
            navigateToMain()
            return
        }
        
        setupViews()
        setupListeners()
    }
    
    private fun setupViews() {
        // Set initial mode to Admin
        updateLoginMode(true)
    }
    
    private fun setupListeners() {
        binding.switchLoginMode.setOnCheckedChangeListener { _, isChecked ->
            isAdminMode = !isChecked // Switch is "Device Owner", so checked = device owner mode
            updateLoginMode(!isAdminMode)
        }
        
        binding.buttonLogin.setOnClickListener {
            attemptLogin()
        }
        
        // Add input validation for 6-digit alphanumeric
        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isAdminMode) {
                    val text = s.toString()
                    // Only allow alphanumeric, max 6 characters
                    if (text.length > 6 || !text.matches(Regex("^[A-Za-z0-9]*$"))) {
                        val filtered = text.filter { it.isLetterOrDigit() }.take(6)
                        if (filtered != text) {
                            binding.editTextUsername.setText(filtered)
                            binding.editTextUsername.setSelection(filtered.length)
                        }
                    }
                }
            }
        })
        
        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isAdminMode) {
                    val text = s.toString()
                    // Only allow alphanumeric, max 6 characters
                    if (text.length > 6 || !text.matches(Regex("^[A-Za-z0-9]*$"))) {
                        val filtered = text.filter { it.isLetterOrDigit() }.take(6)
                        if (filtered != text) {
                            binding.editTextPassword.setText(filtered)
                            binding.editTextPassword.setSelection(filtered.length)
                        }
                    }
                }
            }
        })
    }
    
    private fun updateLoginMode(isAdmin: Boolean) {
        isAdminMode = isAdmin
        
        if (isAdmin) {
            // Admin mode: Email + Password
            binding.textInputLayoutEmail.visibility = View.VISIBLE
            binding.textInputLayoutUsername.visibility = View.GONE
            binding.textInputLayoutEmail.hint = getString(R.string.email)
            binding.textInputLayoutPassword.hint = getString(R.string.password)
            binding.textViewLoginTitle.text = getString(R.string.admin_login)
        } else {
            // Device Owner mode: Username + Password (6-digit alphanumeric)
            binding.textInputLayoutEmail.visibility = View.GONE
            binding.textInputLayoutUsername.visibility = View.VISIBLE
            binding.textInputLayoutUsername.hint = getString(R.string.username) + " (6 alphanumeric)"
            binding.textInputLayoutPassword.hint = getString(R.string.password) + " (6 alphanumeric)"
            binding.textViewLoginTitle.text = getString(R.string.device_owner_login)
            
            // Clear fields
            binding.editTextEmail.setText("")
            binding.editTextUsername.setText("")
            binding.editTextPassword.setText("")
        }
    }
    
    private fun attemptLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val username = binding.editTextUsername.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        
        // Validation
        if (isAdminMode) {
            if (email.isEmpty()) {
                binding.editTextEmail.error = "Email is required"
                return
            }
            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password is required"
                return
            }
        } else {
            if (username.isEmpty()) {
                binding.editTextUsername.error = "Username is required"
                return
            }
            if (username.length != 6) {
                binding.editTextUsername.error = "Username must be 6 alphanumeric characters"
                return
            }
            if (password.isEmpty()) {
                binding.editTextPassword.error = "Password is required"
                return
            }
            if (password.length != 6) {
                binding.editTextPassword.error = "Password must be 6 alphanumeric characters"
                return
            }
        }
        
        // Show loading
        binding.buttonLogin.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val loginRequest = if (isAdminMode) {
                    LoginRequest(
                        email = email,
                        password = password,
                        loginType = "admin"
                    )
                } else {
                    LoginRequest(
                        username = username.uppercase(), // Backend expects uppercase
                        password = password,
                        loginType = "device_owner"
                    )
                }
                
                val response = ApiClient.getApiService().login(loginRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()!!
                    
                    // Save auth token and user
                    loginResponse.token?.let { AuthManager.saveAuthToken(this@LoginActivity, it) }
                    loginResponse.user?.let { AuthManager.saveUser(this@LoginActivity, it) }
                    
                    // If device owner, fetch assigned device ID
                    if (loginResponse.user?.isDeviceOwner == true) {
                        // Will be fetched in MainActivity
                    }
                    
                    // Initialize WebSocket for real-time updates
                    val prefs = getSharedPreferences("controller_prefs", MODE_PRIVATE)
                    val serverUrl = prefs.getString("server_url", "https://your-server.com/")
                    val realtimeManager = RealtimeUpdateManager.getInstance(this@LoginActivity)
                    realtimeManager.initialize(serverUrl ?: "https://your-server.com/")
                    
                    Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val errorMessage = response.body()?.message ?: response.message()
                    Toast.makeText(this@LoginActivity, errorMessage ?: getString(R.string.login_failed), Toast.LENGTH_LONG).show()
                    Timber.e("Login failed: $errorMessage")
                }
            } catch (e: Exception) {
                Timber.e(e, "Login error")
                Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.buttonLogin.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
