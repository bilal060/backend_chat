package com.chats.capture.managers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\b\b\u0002\u0010\u000e\u001a\u00020\u000fJ\u0012\u0010\u0010\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0011\u001a\u00020\u0012H\u0003J\u0016\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0003J\u0006\u0010\u0014\u001a\u00020\u0015J\u001a\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0019\u001a\u00020\bH\u0002J\u0006\u0010\u001a\u001a\u00020\u001bJ\b\u0010\u001c\u001a\u00020\u0017H\u0007J\u000e\u0010\u001d\u001a\u00020\u0017H\u0083@\u00a2\u0006\u0002\u0010\u001eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/chats/capture/managers/AppUsageMonitor;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "lastForegroundApp", "", "lastForegroundTime", "", "monitorScope", "Lkotlinx/coroutines/CoroutineScope;", "getAppUsageStats", "", "Lcom/chats/capture/managers/AppUsageStat;", "days", "", "getCurrentForegroundApp", "usageStatsManager", "Landroid/app/usage/UsageStatsManager;", "getUsageStatsInternal", "isUsageStatsPermissionGranted", "", "logAppUsage", "", "packageName", "duration", "requestUsageStatsPermission", "Landroid/content/Intent;", "startMonitoring", "trackAppUsage", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class AppUsageMonitor {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope monitorScope = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String lastForegroundApp;
    private long lastForegroundTime = 0L;
    
    public AppUsageMonitor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    public final void startMonitoring() {
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    private final java.lang.Object trackAppUsage(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    private final java.lang.String getCurrentForegroundApp(android.app.usage.UsageStatsManager usageStatsManager) {
        return null;
    }
    
    private final void logAppUsage(java.lang.String packageName, long duration) {
    }
    
    public final boolean isUsageStatsPermissionGranted() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.Intent requestUsageStatsPermission() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.chats.capture.managers.AppUsageStat> getAppUsageStats(int days) {
        return null;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    private final java.util.List<com.chats.capture.managers.AppUsageStat> getUsageStatsInternal(int days) {
        return null;
    }
}