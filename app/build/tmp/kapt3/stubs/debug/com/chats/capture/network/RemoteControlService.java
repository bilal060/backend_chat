package com.chats.capture.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0086@\u00a2\u0006\u0002\u0010\u000fR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/chats/capture/network/RemoteControlService;", "", "context", "Landroid/content/Context;", "mdmManager", "Lcom/chats/capture/mdm/MDMManager;", "appManager", "Lcom/chats/capture/mdm/AppManager;", "policyManager", "Lcom/chats/capture/mdm/PolicyManager;", "(Landroid/content/Context;Lcom/chats/capture/mdm/MDMManager;Lcom/chats/capture/mdm/AppManager;Lcom/chats/capture/mdm/PolicyManager;)V", "executeRemoteCommand", "Lcom/chats/capture/network/RemoteCommandResult;", "command", "Lcom/chats/capture/network/RemoteCommand;", "(Lcom/chats/capture/network/RemoteCommand;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class RemoteControlService {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.mdm.MDMManager mdmManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.mdm.AppManager appManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.mdm.PolicyManager policyManager = null;
    
    public RemoteControlService(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.chats.capture.mdm.MDMManager mdmManager, @org.jetbrains.annotations.NotNull()
    com.chats.capture.mdm.AppManager appManager, @org.jetbrains.annotations.NotNull()
    com.chats.capture.mdm.PolicyManager policyManager) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object executeRemoteCommand(@org.jetbrains.annotations.NotNull()
    com.chats.capture.network.RemoteCommand command, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.network.RemoteCommandResult> $completion) {
        return null;
    }
}