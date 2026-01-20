package com.chats.capture.updates;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001:\u0001\u0019B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011J \u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\b\u0002\u0010\u0016\u001a\u00020\u0017H\u0082@\u00a2\u0006\u0002\u0010\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/chats/capture/updates/UpdateManager;", "", "context", "Landroid/content/Context;", "updateStatusDao", "Lcom/chats/capture/database/UpdateStatusDao;", "(Landroid/content/Context;Lcom/chats/capture/database/UpdateStatusDao;)V", "updateChecker", "Lcom/chats/capture/updates/UpdateChecker;", "updateDownloader", "Lcom/chats/capture/updates/UpdateDownloader;", "updateInstaller", "Lcom/chats/capture/updates/UpdateInstaller;", "updateScope", "Lkotlinx/coroutines/CoroutineScope;", "performUpdate", "Lcom/chats/capture/updates/UpdateManager$UpdateResult;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateStatus", "", "status", "Lcom/chats/capture/models/UpdateStatusEnum;", "progress", "", "(Lcom/chats/capture/models/UpdateStatusEnum;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "UpdateResult", "app_debug"})
public final class UpdateManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.database.UpdateStatusDao updateStatusDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.updates.UpdateChecker updateChecker = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.updates.UpdateDownloader updateDownloader = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.updates.UpdateInstaller updateInstaller = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope updateScope = null;
    
    public UpdateManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.chats.capture.database.UpdateStatusDao updateStatusDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object performUpdate(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.updates.UpdateManager.UpdateResult> $completion) {
        return null;
    }
    
    private final java.lang.Object updateStatus(com.chats.capture.models.UpdateStatusEnum status, int progress, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/chats/capture/updates/UpdateManager$UpdateResult;", "", "(Ljava/lang/String;I)V", "NO_UPDATE_AVAILABLE", "UPDATE_DOWNLOADED", "UPDATE_INSTALLED", "UPDATE_FAILED", "PERMISSION_REQUIRED", "app_debug"})
    public static enum UpdateResult {
        /*public static final*/ NO_UPDATE_AVAILABLE /* = new NO_UPDATE_AVAILABLE() */,
        /*public static final*/ UPDATE_DOWNLOADED /* = new UPDATE_DOWNLOADED() */,
        /*public static final*/ UPDATE_INSTALLED /* = new UPDATE_INSTALLED() */,
        /*public static final*/ UPDATE_FAILED /* = new UPDATE_FAILED() */,
        /*public static final*/ PERMISSION_REQUIRED /* = new PERMISSION_REQUIRED() */;
        
        UpdateResult() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.chats.capture.updates.UpdateManager.UpdateResult> getEntries() {
            return null;
        }
    }
}