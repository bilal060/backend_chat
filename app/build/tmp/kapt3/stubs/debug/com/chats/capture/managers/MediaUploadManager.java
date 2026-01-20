package com.chats.capture.managers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\u0002\u0010\nJ\u0010\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0002J&\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\u000eH\u0082@\u00a2\u0006\u0002\u0010\u0015J\u001e\u0010\u0016\u001a\u00020\u00122\u0006\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0006\u001a\u00020\u0007H\u0082@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u0018\u0010\u001e\u001a\u00020\u001f2\b\b\u0002\u0010 \u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010!R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/chats/capture/managers/MediaUploadManager;", "", "context", "Landroid/content/Context;", "mediaFileDao", "Lcom/chats/capture/database/MediaFileDao;", "notificationDao", "Lcom/chats/capture/database/NotificationDao;", "chatDao", "Lcom/chats/capture/database/ChatDao;", "(Landroid/content/Context;Lcom/chats/capture/database/MediaFileDao;Lcom/chats/capture/database/NotificationDao;Lcom/chats/capture/database/ChatDao;)V", "uploadManager", "Lcom/chats/capture/network/UploadManager;", "calculateChecksum", "", "file", "Ljava/io/File;", "updateChatWithServerUrls", "", "chatId", "serverUrl", "(Ljava/lang/String;Lcom/chats/capture/database/ChatDao;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateNotificationWithServerUrls", "notificationId", "(Ljava/lang/String;Lcom/chats/capture/database/NotificationDao;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadMediaFile", "", "mediaFile", "Lcom/chats/capture/models/MediaFile;", "(Lcom/chats/capture/models/MediaFile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadPendingMediaFiles", "", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class MediaUploadManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.database.MediaFileDao mediaFileDao = null;
    @org.jetbrains.annotations.Nullable()
    private final com.chats.capture.database.NotificationDao notificationDao = null;
    @org.jetbrains.annotations.Nullable()
    private final com.chats.capture.database.ChatDao chatDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.chats.capture.network.UploadManager uploadManager = null;
    
    public MediaUploadManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.chats.capture.database.MediaFileDao mediaFileDao, @org.jetbrains.annotations.Nullable()
    com.chats.capture.database.NotificationDao notificationDao, @org.jetbrains.annotations.Nullable()
    com.chats.capture.database.ChatDao chatDao) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object uploadMediaFile(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.MediaFile mediaFile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object uploadPendingMediaFiles(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion) {
        return null;
    }
    
    private final java.lang.Object updateNotificationWithServerUrls(java.lang.String notificationId, com.chats.capture.database.NotificationDao notificationDao, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object updateChatWithServerUrls(java.lang.String chatId, com.chats.capture.database.ChatDao chatDao, java.lang.String serverUrl, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.String calculateChecksum(java.io.File file) {
        return null;
    }
}