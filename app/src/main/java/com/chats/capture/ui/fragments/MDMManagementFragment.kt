package com.chats.capture.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chats.capture.R
import com.chats.capture.mdm.AppManager
import com.chats.capture.mdm.MDMManager
import com.chats.capture.mdm.PolicyManager
import com.chats.capture.mdm.SecurityPolicy
import com.chats.capture.ui.adapters.AppsAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MDMManagementFragment : Fragment() {
    
    private lateinit var mdmManager: MDMManager
    private lateinit var appManager: AppManager
    private lateinit var policyManager: PolicyManager
    
    private lateinit var textViewDeviceAdminStatus: android.widget.TextView
    private lateinit var textViewDeviceOwnerStatus: android.widget.TextView
    private lateinit var textViewDeviceInfo: android.widget.TextView
    private lateinit var buttonActivateDeviceAdmin: MaterialButton
    private lateinit var buttonLockDevice: MaterialButton
    private lateinit var buttonWipeDevice: MaterialButton
    private lateinit var recyclerViewApps: RecyclerView
    private lateinit var switchPasswordPolicy: SwitchMaterial
    private lateinit var switchDisableCamera: SwitchMaterial
    private lateinit var switchKioskMode: SwitchMaterial
    private lateinit var buttonApplyPolicies: MaterialButton
    
    private lateinit var appsAdapter: AppsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mdm_management, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        mdmManager = MDMManager(requireContext())
        appManager = AppManager(requireContext(), mdmManager)
        policyManager = PolicyManager(requireContext(), mdmManager)
        
        initializeViews(view)
        setupListeners()
        updateDeviceStatus()
        loadInstalledApps()
    }
    
    override fun onResume() {
        super.onResume()
        updateDeviceStatus()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_DEVICE_ADMIN) {
            updateDeviceStatus()
        }
    }
    
    private fun initializeViews(view: View) {
        textViewDeviceAdminStatus = view.findViewById(R.id.textViewDeviceAdminStatus)
        textViewDeviceOwnerStatus = view.findViewById(R.id.textViewDeviceOwnerStatus)
        textViewDeviceInfo = view.findViewById(R.id.textViewDeviceInfo)
        buttonActivateDeviceAdmin = view.findViewById(R.id.buttonActivateDeviceAdmin)
        buttonLockDevice = view.findViewById(R.id.buttonLockDevice)
        buttonWipeDevice = view.findViewById(R.id.buttonWipeDevice)
        recyclerViewApps = view.findViewById(R.id.recyclerViewApps)
        switchPasswordPolicy = view.findViewById(R.id.switchPasswordPolicy)
        switchDisableCamera = view.findViewById(R.id.switchDisableCamera)
        switchKioskMode = view.findViewById(R.id.switchKioskMode)
        buttonApplyPolicies = view.findViewById(R.id.buttonApplyPolicies)
        
        appsAdapter = AppsAdapter { appInfo ->
            showUninstallDialog(appInfo)
        }
        recyclerViewApps.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewApps.adapter = appsAdapter
    }
    
    private fun setupListeners() {
        buttonActivateDeviceAdmin.setOnClickListener {
            val intent = mdmManager.requestDeviceAdminActivation()
            startActivityForResult(intent, REQUEST_DEVICE_ADMIN)
        }
        
        buttonLockDevice.setOnClickListener {
            if (mdmManager.lockDevice()) {
                Timber.d("Device locked")
            } else {
                Timber.w("Device Admin not active")
            }
        }
        
        buttonWipeDevice.setOnClickListener {
            showWipeConfirmationDialog()
        }
        
        buttonApplyPolicies.setOnClickListener {
            applyPolicies()
        }
    }
    
    private fun updateDeviceStatus() {
        val deviceInfo = mdmManager.getDeviceInfo()
        
        textViewDeviceAdminStatus.text = "Device Admin: ${if (deviceInfo.isDeviceAdmin) "Active" else "Inactive"}"
        textViewDeviceOwnerStatus.text = "Device Owner: ${if (deviceInfo.isDeviceOwner) "Active" else "Inactive"}"
        textViewDeviceInfo.text = "${deviceInfo.manufacturer} ${deviceInfo.deviceModel} - Android ${deviceInfo.androidVersion}"
        
        buttonActivateDeviceAdmin.isEnabled = !deviceInfo.isDeviceAdmin && !deviceInfo.isDeviceOwner
        buttonLockDevice.isEnabled = deviceInfo.isDeviceAdmin || deviceInfo.isDeviceOwner
        buttonWipeDevice.isEnabled = deviceInfo.isDeviceAdmin || deviceInfo.isDeviceOwner
    }
    
    private fun loadInstalledApps() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apps = appManager.getInstalledApps()
                CoroutineScope(Dispatchers.Main).launch {
                    appsAdapter.submitList(apps)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading installed apps")
            }
        }
    }
    
    private fun showUninstallDialog(appInfo: com.chats.capture.mdm.AppInfo) {
        AlertDialog.Builder(requireContext())
            .setTitle("Uninstall App")
            .setMessage("Are you sure you want to uninstall ${appInfo.appName}?")
            .setPositiveButton("Uninstall") { _, _ ->
                if (appManager.uninstallApp(appInfo.packageName)) {
                    Timber.d("Uninstalling ${appInfo.appName}")
                    loadInstalledApps()
                } else {
                    Timber.e("Failed to uninstall ${appInfo.appName}")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showWipeConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Factory Reset")
            .setMessage("This will erase all data on the device. Are you absolutely sure?")
            .setPositiveButton("Wipe Device") { _, _ ->
                if (mdmManager.wipeDevice()) {
                    Timber.d("Device wipe initiated")
                } else {
                    Timber.e("Failed to wipe device")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun applyPolicies() {
        val policy = SecurityPolicy(
            enforcePasswordPolicy = switchPasswordPolicy.isChecked,
            disableCamera = switchDisableCamera.isChecked,
            requireStorageEncryption = false
        )
        
        if (policyManager.applySecurityPolicy(policy)) {
            Timber.d("Policies applied")
        } else {
            Timber.e("Failed to apply policies")
        }
        
        // Handle kiosk mode separately
        if (switchKioskMode.isChecked) {
            val packageName = requireContext().packageName
            if (mdmManager.isDeviceOwner() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mdmManager.setKioskMode(packageName, true)
                Timber.d("Kiosk mode enabled")
            } else {
                Timber.w("Kiosk mode requires Device Owner")
            }
        }
    }
    
    companion object {
        const val REQUEST_DEVICE_ADMIN = 1001
    }
}
