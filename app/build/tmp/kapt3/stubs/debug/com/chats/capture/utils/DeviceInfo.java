package com.chats.capture.utils;

/**
 * Utility for generating and managing device identification
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\u0007\u001a\u00020\u0004J\u0010\u0010\b\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0010\u0010\t\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\n\u001a\u00020\u0004\u00a8\u0006\u000b"}, d2 = {"Lcom/chats/capture/utils/DeviceInfo;", "", "()V", "getDeviceId", "", "context", "Landroid/content/Context;", "getDeviceModel", "getDeviceName", "getImei", "getOsVersion", "app_debug"})
public final class DeviceInfo {
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.DeviceInfo INSTANCE = null;
    
    private DeviceInfo() {
        super();
    }
    
    /**
     * Generate unique device ID
     * Uses Android ID + package name hash for uniqueness
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeviceId(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Get device model name
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDeviceModel() {
        return null;
    }
    
    /**
     * Get Android OS version
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOsVersion() {
        return null;
    }
    
    /**
     * Get device IMEI (if available and permission granted)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getImei(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Get device name (if set by user)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDeviceName(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
}