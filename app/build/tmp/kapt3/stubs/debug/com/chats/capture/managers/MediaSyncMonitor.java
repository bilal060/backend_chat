package com.chats.capture.managers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\n\u001a\u00020\u000bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\f\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\tJ\u000e\u0010\r\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/chats/capture/managers/MediaSyncMonitor;", "", "context", "Landroid/content/Context;", "mediaFileDao", "Lcom/chats/capture/database/MediaFileDao;", "(Landroid/content/Context;Lcom/chats/capture/database/MediaFileDao;)V", "checkOrphanedFiles", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSyncStatus", "Lcom/chats/capture/managers/SyncStatus;", "performHealthCheck", "retryPermanentlyFailedUploads", "app_debug"})
public final class MediaSyncMonitor {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.database.MediaFileDao mediaFileDao = null;
    
    public MediaSyncMonitor(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.chats.capture.database.MediaFileDao mediaFileDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object performHealthCheck(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object checkOrphanedFiles(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object retryPermanentlyFailedUploads(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getSyncStatus(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.managers.SyncStatus> $completion) {
        return null;
    }
}