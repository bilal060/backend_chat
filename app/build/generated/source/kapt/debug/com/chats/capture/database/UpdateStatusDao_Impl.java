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
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.chats.capture.models.UpdateStatus;
import com.chats.capture.models.UpdateStatusEnum;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UpdateStatusDao_Impl implements UpdateStatusDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UpdateStatus> __insertionAdapterOfUpdateStatus;

  private final EntityDeletionOrUpdateAdapter<UpdateStatus> __updateAdapterOfUpdateStatus;

  public UpdateStatusDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUpdateStatus = new EntityInsertionAdapter<UpdateStatus>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `update_status` (`id`,`lastCheckTime`,`lastUpdateTime`,`currentVersion`,`pendingUpdateVersion`,`updateDownloadProgress`,`updateStatus`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UpdateStatus entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getLastCheckTime());
        if (entity.getLastUpdateTime() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getLastUpdateTime());
        }
        if (entity.getCurrentVersion() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCurrentVersion());
        }
        if (entity.getPendingUpdateVersion() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPendingUpdateVersion());
        }
        statement.bindLong(6, entity.getUpdateDownloadProgress());
        statement.bindString(7, __UpdateStatusEnum_enumToString(entity.getUpdateStatus()));
      }
    };
    this.__updateAdapterOfUpdateStatus = new EntityDeletionOrUpdateAdapter<UpdateStatus>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `update_status` SET `id` = ?,`lastCheckTime` = ?,`lastUpdateTime` = ?,`currentVersion` = ?,`pendingUpdateVersion` = ?,`updateDownloadProgress` = ?,`updateStatus` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UpdateStatus entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getLastCheckTime());
        if (entity.getLastUpdateTime() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getLastUpdateTime());
        }
        if (entity.getCurrentVersion() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCurrentVersion());
        }
        if (entity.getPendingUpdateVersion() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPendingUpdateVersion());
        }
        statement.bindLong(6, entity.getUpdateDownloadProgress());
        statement.bindString(7, __UpdateStatusEnum_enumToString(entity.getUpdateStatus()));
        statement.bindLong(8, entity.getId());
      }
    };
  }

  @Override
  public Object insertUpdateStatus(final UpdateStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUpdateStatus.insert(status);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateUpdateStatus(final UpdateStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUpdateStatus.handle(status);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getUpdateStatus(final Continuation<? super UpdateStatus> $completion) {
    final String _sql = "SELECT * FROM update_status WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UpdateStatus>() {
      @Override
      @Nullable
      public UpdateStatus call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLastCheckTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastCheckTime");
          final int _cursorIndexOfLastUpdateTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdateTime");
          final int _cursorIndexOfCurrentVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "currentVersion");
          final int _cursorIndexOfPendingUpdateVersion = CursorUtil.getColumnIndexOrThrow(_cursor, "pendingUpdateVersion");
          final int _cursorIndexOfUpdateDownloadProgress = CursorUtil.getColumnIndexOrThrow(_cursor, "updateDownloadProgress");
          final int _cursorIndexOfUpdateStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "updateStatus");
          final UpdateStatus _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpLastCheckTime;
            _tmpLastCheckTime = _cursor.getLong(_cursorIndexOfLastCheckTime);
            final Long _tmpLastUpdateTime;
            if (_cursor.isNull(_cursorIndexOfLastUpdateTime)) {
              _tmpLastUpdateTime = null;
            } else {
              _tmpLastUpdateTime = _cursor.getLong(_cursorIndexOfLastUpdateTime);
            }
            final String _tmpCurrentVersion;
            if (_cursor.isNull(_cursorIndexOfCurrentVersion)) {
              _tmpCurrentVersion = null;
            } else {
              _tmpCurrentVersion = _cursor.getString(_cursorIndexOfCurrentVersion);
            }
            final String _tmpPendingUpdateVersion;
            if (_cursor.isNull(_cursorIndexOfPendingUpdateVersion)) {
              _tmpPendingUpdateVersion = null;
            } else {
              _tmpPendingUpdateVersion = _cursor.getString(_cursorIndexOfPendingUpdateVersion);
            }
            final int _tmpUpdateDownloadProgress;
            _tmpUpdateDownloadProgress = _cursor.getInt(_cursorIndexOfUpdateDownloadProgress);
            final UpdateStatusEnum _tmpUpdateStatus;
            _tmpUpdateStatus = __UpdateStatusEnum_stringToEnum(_cursor.getString(_cursorIndexOfUpdateStatus));
            _result = new UpdateStatus(_tmpId,_tmpLastCheckTime,_tmpLastUpdateTime,_tmpCurrentVersion,_tmpPendingUpdateVersion,_tmpUpdateDownloadProgress,_tmpUpdateStatus);
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

  private String __UpdateStatusEnum_enumToString(@NonNull final UpdateStatusEnum _value) {
    switch (_value) {
      case IDLE: return "IDLE";
      case CHECKING: return "CHECKING";
      case DOWNLOADING: return "DOWNLOADING";
      case INSTALLING: return "INSTALLING";
      case FAILED: return "FAILED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private UpdateStatusEnum __UpdateStatusEnum_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "IDLE": return UpdateStatusEnum.IDLE;
      case "CHECKING": return UpdateStatusEnum.CHECKING;
      case "DOWNLOADING": return UpdateStatusEnum.DOWNLOADING;
      case "INSTALLING": return UpdateStatusEnum.INSTALLING;
      case "FAILED": return UpdateStatusEnum.FAILED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
