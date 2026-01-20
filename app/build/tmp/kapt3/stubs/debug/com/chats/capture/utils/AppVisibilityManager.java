package com.chats.capture.utils;

/**
 * Manages app visibility in launcher and Settings
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\f"}, d2 = {"Lcom/chats/capture/utils/AppVisibilityManager;", "", "()V", "getSettingsIntent", "Landroid/content/Intent;", "context", "Landroid/content/Context;", "hideFromLauncher", "", "isHiddenFromLauncher", "", "showInLauncher", "app_debug"})
public final class AppVisibilityManager {
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.AppVisibilityManager INSTANCE = null;
    
    private AppVisibilityManager() {
        super();
    }
    
    /**
     * Hide app from launcher (app drawer)
     * App will still be accessible from Settings
     */
    public final void hideFromLauncher(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Show app in launcher (for testing/debugging)
     */
    public final void showInLauncher(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Check if app is hidden from launcher
     */
    public final boolean isHiddenFromLauncher(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Get intent to open app from Settings
     */
    @org.jetbrains.annotations.NotNull()
    public final android.content.Intent getSettingsIntent(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
}