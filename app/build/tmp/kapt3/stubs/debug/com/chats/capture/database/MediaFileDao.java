package com.chats.capture.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0015\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\u0006\u0010\u000f\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J&\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\u0006\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0015J\u000e\u0010\u0016\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u001e\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u000e2\b\b\u0002\u0010\u0013\u001a\u00020\u0014H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u0016\u0010\u001a\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u001c\u0010\u001d\u001a\u00020\u00032\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\f0\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u001fJ(\u0010 \u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u00122\b\u0010!\u001a\u0004\u0018\u00010\u0005H\u00a7@\u00a2\u0006\u0002\u0010\"J0\u0010#\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010$\u001a\u00020\t2\b\u0010%\u001a\u0004\u0018\u00010\u0005H\u00a7@\u00a2\u0006\u0002\u0010&J\u0016\u0010\'\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010(\u001a\u00020\u00032\u0006\u0010\u001b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u001c\u00a8\u0006)"}, d2 = {"Lcom/chats/capture/database/MediaFileDao;", "", "deleteMediaFile", "", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteOldUploadedFiles", "beforeTimestamp", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMediaFileById", "Lcom/chats/capture/models/MediaFile;", "getMediaFilesByNotification", "", "notificationId", "getMediaFilesByStatus", "status", "Lcom/chats/capture/models/UploadStatus;", "limit", "", "(Lcom/chats/capture/models/UploadStatus;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPendingUploadCount", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPendingUploads", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertMediaFile", "mediaFile", "(Lcom/chats/capture/models/MediaFile;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertMediaFiles", "mediaFiles", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markAsUploaded", "url", "(Ljava/lang/String;Lcom/chats/capture/models/UploadStatus;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markUploadAttempt", "timestamp", "error", "(Ljava/lang/String;Lcom/chats/capture/models/UploadStatus;JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "resetStuckUploads", "updateMediaFile", "app_debug"})
@androidx.room.Dao()
public abstract interface MediaFileDao {
    
    @androidx.room.Query(value = "SELECT * FROM media_files WHERE uploadStatus = :status ORDER BY createdAt ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMediaFilesByStatus(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.UploadStatus status, int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.MediaFile>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM media_files WHERE notificationId = :notificationId")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMediaFilesByNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String notificationId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.MediaFile>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM media_files WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getMediaFileById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.models.MediaFile> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM media_files WHERE uploadStatus IN (\'PENDING\', \'FAILED\') ORDER BY createdAt ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPendingUploads(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.MediaFile>> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM media_files WHERE uploadStatus IN (\'PENDING\', \'FAILED\')")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPendingUploadCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMediaFile(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.MediaFile mediaFile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMediaFiles(@org.jetbrains.annotations.NotNull()
    java.util.List<com.chats.capture.models.MediaFile> mediaFiles, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateMediaFile(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.MediaFile mediaFile, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE media_files SET uploadStatus = :status, remoteUrl = :url WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markAsUploaded(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    com.chats.capture.models.UploadStatus status, @org.jetbrains.annotations.Nullable()
    java.lang.String url, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE media_files SET uploadStatus = :status, uploadAttempts = uploadAttempts + 1, lastUploadAttempt = :timestamp, errorMessage = :error WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markUploadAttempt(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    com.chats.capture.models.UploadStatus status, long timestamp, @org.jetbrains.annotations.Nullable()
    java.lang.String error, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE media_files SET uploadStatus = \'PENDING\' WHERE uploadStatus = \'UPLOADING\' AND lastUploadAttempt < :beforeTimestamp")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object resetStuckUploads(long beforeTimestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM media_files WHERE uploadStatus = \'SUCCESS\' AND createdAt < :beforeTimestamp")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOldUploadedFiles(long beforeTimestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM media_files WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMediaFile(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}