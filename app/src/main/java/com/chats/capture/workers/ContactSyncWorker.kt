package com.chats.capture.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chats.capture.CaptureApplication
import com.chats.capture.database.ContactDao
import com.chats.capture.managers.ContactCaptureManager
import com.chats.capture.network.ApiClient
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Worker to sync all contacts daily
 */
class ContactSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("ContactSyncWorker started - capturing and syncing all contacts")
            
            // Check network availability
            if (!ApiClient.isNetworkAvailable(applicationContext)) {
                Timber.w("Network not available, skipping contact sync")
                return Result.retry()
            }
            
            // Capture all contacts from device
            val contactCaptureManager = ContactCaptureManager(applicationContext)
            contactCaptureManager.captureAllContacts()
            
            // Wait a bit for contacts to be captured
            delay(2000)
            
            // Sync all contacts to server
            val database = (applicationContext as CaptureApplication).database
            syncContacts(database.contactDao())
            
            Timber.d("ContactSyncWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "ContactSyncWorker failed")
            Result.retry()
        }
    }
    
    private suspend fun syncContacts(contactDao: ContactDao) {
        try {
            // Get all unsynced contacts (or all contacts if daily sync)
            val unsyncedContacts = contactDao.getUnsyncedContacts(limit = 500)
            
            if (unsyncedContacts.isEmpty()) {
                Timber.d("No unsynced contacts")
                return
            }
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadContactsBatch(unsyncedContacts)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Mark as synced
                val syncTime = System.currentTimeMillis()
                unsyncedContacts.forEach { contact ->
                    contactDao.markAsSynced(contact.id, syncTime)
                }
                Timber.d("Synced ${unsyncedContacts.size} contacts")
            } else {
                // Mark sync attempt
                val errorMessage = response.body()?.message ?: "Sync failed"
                unsyncedContacts.forEach { contact ->
                    contactDao.markSyncAttempt(
                        contact.id,
                        System.currentTimeMillis(),
                        errorMessage
                    )
                }
                Timber.w("Failed to sync contacts: $errorMessage")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error syncing contacts")
        }
    }
}
