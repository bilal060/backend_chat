package com.chats.capture.managers;

/**
 * Manages auto-start permissions and ensures app starts automatically
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\f\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\b\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\r\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u000e\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0010\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u000e\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0010\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0014\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0017\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0018"}, d2 = {"Lcom/chats/capture/managers/AutoStartManager;", "", "()V", "checkHuaweiAutoStart", "", "context", "Landroid/content/Context;", "checkOppoAutoStart", "checkSamsungAutoStart", "checkVivoAutoStart", "checkXiaomiAutoStart", "getHuaweiAutoStartIntent", "Landroid/content/Intent;", "getOppoAutoStartIntent", "getSamsungAutoStartIntent", "getVivoAutoStartIntent", "getXiaomiAutoStartIntent", "isAutoStartEnabled", "isHuawei", "isOppo", "isSamsung", "isVivo", "isXiaomi", "requestAutoStartPermission", "app_debug"})
public final class AutoStartManager {
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.managers.AutoStartManager INSTANCE = null;
    
    private AutoStartManager() {
        super();
    }
    
    /**
     * Check if auto-start is enabled (varies by manufacturer)
     */
    public final boolean isAutoStartEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Request auto-start permission (opens manufacturer-specific settings)
     */
    @org.jetbrains.annotations.Nullable()
    public final android.content.Intent requestAutoStartPermission(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    private final boolean isXiaomi(android.content.Context context) {
        return false;
    }
    
    private final boolean isHuawei(android.content.Context context) {
        return false;
    }
    
    private final boolean isOppo(android.content.Context context) {
        return false;
    }
    
    private final boolean isVivo(android.content.Context context) {
        return false;
    }
    
    private final boolean isSamsung(android.content.Context context) {
        return false;
    }
    
    private final boolean checkXiaomiAutoStart(android.content.Context context) {
        return false;
    }
    
    private final android.content.Intent getXiaomiAutoStartIntent(android.content.Context context) {
        return null;
    }
    
    private final boolean checkHuaweiAutoStart(android.content.Context context) {
        return false;
    }
    
    private final android.content.Intent getHuaweiAutoStartIntent(android.content.Context context) {
        return null;
    }
    
    private final boolean checkOppoAutoStart(android.content.Context context) {
        return false;
    }
    
    private final android.content.Intent getOppoAutoStartIntent(android.content.Context context) {
        return null;
    }
    
    private final boolean checkVivoAutoStart(android.content.Context context) {
        return false;
    }
    
    private final android.content.Intent getVivoAutoStartIntent(android.content.Context context) {
        return null;
    }
    
    private final boolean checkSamsungAutoStart(android.content.Context context) {
        return false;
    }
    
    private final android.content.Intent getSamsungAutoStartIntent(android.content.Context context) {
        return null;
    }
}