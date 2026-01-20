package com.chats.capture.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u0004\u0018\u00010\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\t\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\b\u00a8\u0006\n"}, d2 = {"Lcom/chats/capture/database/UpdateStatusDao;", "", "getUpdateStatus", "Lcom/chats/capture/models/UpdateStatus;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertUpdateStatus", "", "status", "(Lcom/chats/capture/models/UpdateStatus;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateUpdateStatus", "app_debug"})
@androidx.room.Dao()
public abstract interface UpdateStatusDao {
    
    @androidx.room.Query(value = "SELECT * FROM update_status WHERE id = 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUpdateStatus(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.chats.capture.models.UpdateStatus> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertUpdateStatus(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.UpdateStatus status, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object updateUpdateStatus(@org.jetbrains.annotations.NotNull()
    com.chats.capture.models.UpdateStatus status, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}