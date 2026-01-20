package com.chats.capture.mdm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0006\u0010\u000b\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/chats/capture/mdm/PolicyManager;", "", "context", "Landroid/content/Context;", "mdmManager", "Lcom/chats/capture/mdm/MDMManager;", "(Landroid/content/Context;Lcom/chats/capture/mdm/MDMManager;)V", "applySecurityPolicy", "", "policy", "Lcom/chats/capture/mdm/SecurityPolicy;", "removeSecurityPolicy", "app_debug"})
public final class PolicyManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.mdm.MDMManager mdmManager = null;
    
    public PolicyManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.chats.capture.mdm.MDMManager mdmManager) {
        super();
    }
    
    public final boolean applySecurityPolicy(@org.jetbrains.annotations.NotNull()
    com.chats.capture.mdm.SecurityPolicy policy) {
        return false;
    }
    
    public final boolean removeSecurityPolicy() {
        return false;
    }
}