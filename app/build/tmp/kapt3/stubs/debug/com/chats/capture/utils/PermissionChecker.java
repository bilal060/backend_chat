package com.chats.capture.utils;

/**
 * Utility to check all required permissions status
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\r\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u000e"}, d2 = {"Lcom/chats/capture/utils/PermissionChecker;", "", "()V", "areCriticalPermissionsGranted", "", "context", "Landroid/content/Context;", "canInstallPackages", "getAllPermissionStatus", "Lcom/chats/capture/utils/PermissionStatus;", "isAccessibilityServiceEnabled", "isBatteryOptimizationIgnored", "isNotificationServiceEnabled", "isUsageStatsPermissionGranted", "app_debug"})
public final class PermissionChecker {
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.PermissionChecker INSTANCE = null;
    
    private PermissionChecker() {
        super();
    }
    
    /**
     * Check if notification listener service is enabled
     */
    public final boolean isNotificationServiceEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Check if accessibility service is enabled
     */
    public final boolean isAccessibilityServiceEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Check if battery optimization is ignored
     */
    public final boolean isBatteryOptimizationIgnored(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Check if usage stats permission is granted
     */
    public final boolean isUsageStatsPermissionGranted(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Check if install packages permission is granted
     */
    public final boolean canInstallPackages(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Get all permission statuses
     */
    @org.jetbrains.annotations.NotNull()
    public final com.chats.capture.utils.PermissionStatus getAllPermissionStatus(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Check if all critical permissions are granted
     */
    public final boolean areCriticalPermissionsGranted(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
}