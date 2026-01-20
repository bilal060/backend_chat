package com.chats.capture.managers;

/**
 * Manages device registration and heartbeat with the server
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u000f\u001a\u00020\bJ\u0006\u0010\u0010\u001a\u00020\u0006J\u0006\u0010\u0011\u001a\u00020\u0012J\u0012\u0010\u0013\u001a\u00020\u00142\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\bJ\u0012\u0010\u0016\u001a\u00020\u00142\b\u0010\u0015\u001a\u0004\u0018\u00010\bH\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/chats/capture/managers/DeviceRegistrationManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "HEARTBEAT_INTERVAL_MS", "", "KEY_DEVICE_ID", "", "KEY_LAST_HEARTBEAT", "KEY_REGISTERED", "prefs", "Landroid/content/SharedPreferences;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "getDeviceId", "getLastHeartbeat", "isRegistered", "", "registerDevice", "", "fcmToken", "startHeartbeat", "app_debug"})
public final class DeviceRegistrationManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_DEVICE_ID = "device_id";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_REGISTERED = "device_registered";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String KEY_LAST_HEARTBEAT = "last_heartbeat";
    private final long HEARTBEAT_INTERVAL_MS = 300000L;
    
    public DeviceRegistrationManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Get or generate device ID
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeviceId() {
        return null;
    }
    
    /**
     * Register device with server
     */
    public final void registerDevice(@org.jetbrains.annotations.Nullable()
    java.lang.String fcmToken) {
    }
    
    /**
     * Send heartbeat to server periodically
     */
    private final void startHeartbeat(java.lang.String fcmToken) {
    }
    
    /**
     * Check if device is registered
     */
    public final boolean isRegistered() {
        return false;
    }
    
    /**
     * Get last heartbeat time
     */
    public final long getLastHeartbeat() {
        return 0L;
    }
}