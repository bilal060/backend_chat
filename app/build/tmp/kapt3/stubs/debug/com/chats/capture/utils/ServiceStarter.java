package com.chats.capture.utils;

/**
 * Utility for starting services reliably
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\n"}, d2 = {"Lcom/chats/capture/utils/ServiceStarter;", "", "()V", "ensureServicesRunning", "", "context", "Landroid/content/Context;", "isNotificationServiceRunning", "", "startNotificationService", "app_debug"})
public final class ServiceStarter {
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.ServiceStarter INSTANCE = null;
    
    private ServiceStarter() {
        super();
    }
    
    /**
     * Start notification capture service
     */
    public final void startNotificationService(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Check if notification service is running
     */
    public final boolean isNotificationServiceRunning(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Ensure services are running (restart if not)
     */
    public final void ensureServicesRunning(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}