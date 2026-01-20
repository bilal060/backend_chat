package com.chats.capture.managers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0005\u001a\u00020\u0006J\u0006\u0010\u0007\u001a\u00020\bJ\n\u0010\t\u001a\u0004\u0018\u00010\bH\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/chats/capture/managers/BatteryOptimizationManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "isIgnoringBatteryOptimizations", "", "openBatteryOptimizationSettings", "Landroid/content/Intent;", "requestBatteryOptimizationExemption", "app_debug"})
public final class BatteryOptimizationManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    
    public BatteryOptimizationManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean isIgnoringBatteryOptimizations() {
        return false;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.M)
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent requestBatteryOptimizationExemption() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.Intent openBatteryOptimizationSettings() {
        return null;
    }
}