package com.chats.capture.utils;

/**
 * Tracks app installation and first run status
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\nH\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/chats/capture/utils/InstallationTracker;", "", "()V", "KEY_FIRST_RUN", "", "KEY_INSTALL_TIME", "KEY_LAST_VERSION", "PREFS_NAME", "getCurrentVersion", "context", "Landroid/content/Context;", "getInstallTime", "", "isAppUpdated", "", "isFirstRun", "app_debug"})
public final class InstallationTracker {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "installation_tracker";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_FIRST_RUN = "first_run";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_INSTALL_TIME = "install_time";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_VERSION = "last_version";
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.InstallationTracker INSTANCE = null;
    
    private InstallationTracker() {
        super();
    }
    
    /**
     * Check if this is the first run after installation
     */
    public final boolean isFirstRun(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Get installation timestamp
     */
    public final long getInstallTime(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0L;
    }
    
    /**
     * Check if app was updated
     */
    public final boolean isAppUpdated(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    private final java.lang.String getCurrentVersion(android.content.Context context) {
        return null;
    }
}