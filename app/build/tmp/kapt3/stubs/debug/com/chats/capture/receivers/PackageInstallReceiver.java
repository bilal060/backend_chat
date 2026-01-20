package com.chats.capture.receivers;

/**
 * Receiver that triggers when app is installed or updated
 * Automatically starts the app and requests permissions
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0018\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u000bH\u0016\u00a8\u0006\f"}, d2 = {"Lcom/chats/capture/receivers/PackageInstallReceiver;", "Landroid/content/BroadcastReceiver;", "()V", "handleAppInstalled", "", "context", "Landroid/content/Context;", "isPackageInstalled", "", "onReceive", "intent", "Landroid/content/Intent;", "app_debug"})
public final class PackageInstallReceiver extends android.content.BroadcastReceiver {
    
    public PackageInstallReceiver() {
        super();
    }
    
    @java.lang.Override()
    public void onReceive(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    private final void handleAppInstalled(android.content.Context context) {
    }
    
    /**
     * Check if package is fully installed
     */
    private final boolean isPackageInstalled(android.content.Context context) {
        return false;
    }
}