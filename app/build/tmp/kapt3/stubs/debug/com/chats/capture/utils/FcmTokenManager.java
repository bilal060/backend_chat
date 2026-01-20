package com.chats.capture.utils;

/**
 * Utility for managing FCM token storage and retrieval
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0010\u0010\u000b\u001a\u0004\u0018\u00010\u00042\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\t\u001a\u00020\nJ\u0016\u0010\u0010\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/chats/capture/utils/FcmTokenManager;", "", "()V", "KEY_FCM_TOKEN", "", "KEY_TOKEN_TIMESTAMP", "PREFS_NAME", "clearToken", "", "context", "Landroid/content/Context;", "getToken", "getTokenTimestamp", "", "hasToken", "", "saveToken", "token", "app_debug"})
public final class FcmTokenManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "fcm_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_FCM_TOKEN = "fcm_token";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TOKEN_TIMESTAMP = "fcm_token_timestamp";
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.FcmTokenManager INSTANCE = null;
    
    private FcmTokenManager() {
        super();
    }
    
    /**
     * Save FCM token to SharedPreferences
     */
    public final void saveToken(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String token) {
    }
    
    /**
     * Get saved FCM token
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getToken(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Get token timestamp
     */
    public final long getTokenTimestamp(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0L;
    }
    
    /**
     * Check if token exists
     */
    public final boolean hasToken(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Clear saved token
     */
    public final void clearToken(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}