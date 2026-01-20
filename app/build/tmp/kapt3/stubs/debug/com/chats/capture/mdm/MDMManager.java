package com.chats.capture.mdm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\r\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000bJ\u0006\u0010\r\u001a\u00020\u000eJ\u0010\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\u0010\u001a\u00020\u0011H\u0007J\u0006\u0010\u0012\u001a\u00020\u000bJ\u0006\u0010\u0013\u001a\u00020\u000bJ\u0006\u0010\u0014\u001a\u00020\u000bJ\u0006\u0010\u0015\u001a\u00020\u0016J\u0018\u0010\u0017\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u000bH\u0007JB\u0010\u001b\u001a\u00020\u000b2\b\b\u0002\u0010\u001c\u001a\u00020\u001d2\b\b\u0002\u0010\u001e\u001a\u00020\u000b2\b\b\u0002\u0010\u001f\u001a\u00020\u000b2\b\b\u0002\u0010 \u001a\u00020\u000b2\b\b\u0002\u0010!\u001a\u00020\u000b2\b\b\u0002\u0010\"\u001a\u00020\u000bJ\u0010\u0010#\u001a\u00020\u000b2\u0006\u0010$\u001a\u00020\u000bH\u0007J\u0018\u0010%\u001a\u00020\u000b2\u0006\u0010&\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u000bH\u0007J\u0010\u0010\'\u001a\u00020\u000b2\u0006\u0010\u0018\u001a\u00020\u0019H\u0007J\u0006\u0010(\u001a\u00020\u000bJ\u0006\u0010)\u001a\u00020\u000bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/chats/capture/mdm/MDMManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "deviceAdminComponent", "Landroid/content/ComponentName;", "deviceOwnerComponent", "devicePolicyManager", "Landroid/app/admin/DevicePolicyManager;", "disableCamera", "", "disable", "getDeviceInfo", "Lcom/chats/capture/mdm/DeviceInfo;", "installAppSilently", "apkUri", "Landroid/net/Uri;", "isDeviceAdminActive", "isDeviceOwner", "lockDevice", "requestDeviceAdminActivation", "Landroid/content/Intent;", "setKioskMode", "packageName", "", "enable", "setPasswordPolicy", "minLength", "", "requireNumeric", "requireLetters", "requireLowercase", "requireUppercase", "requireSymbols", "setStorageEncryption", "require", "setUserRestriction", "restriction", "uninstallAppSilently", "unlockDevice", "wipeDevice", "app_debug"})
public final class MDMManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.app.admin.DevicePolicyManager devicePolicyManager = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.ComponentName deviceAdminComponent = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.ComponentName deviceOwnerComponent = null;
    
    public MDMManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean isDeviceAdminActive() {
        return false;
    }
    
    public final boolean isDeviceOwner() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.content.Intent requestDeviceAdminActivation() {
        return null;
    }
    
    public final boolean lockDevice() {
        return false;
    }
    
    public final boolean unlockDevice() {
        return false;
    }
    
    public final boolean wipeDevice() {
        return false;
    }
    
    public final boolean setPasswordPolicy(int minLength, boolean requireNumeric, boolean requireLetters, boolean requireLowercase, boolean requireUppercase, boolean requireSymbols) {
        return false;
    }
    
    public final boolean disableCamera(boolean disable) {
        return false;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.M)
    public final boolean setStorageEncryption(boolean require) {
        return false;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    public final boolean installAppSilently(@org.jetbrains.annotations.NotNull()
    android.net.Uri apkUri) {
        return false;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.LOLLIPOP)
    public final boolean uninstallAppSilently(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return false;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.M)
    public final boolean setKioskMode(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName, boolean enable) {
        return false;
    }
    
    @androidx.annotation.RequiresApi(value = android.os.Build.VERSION_CODES.M)
    public final boolean setUserRestriction(@org.jetbrains.annotations.NotNull()
    java.lang.String restriction, boolean enable) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.chats.capture.mdm.DeviceInfo getDeviceInfo() {
        return null;
    }
}