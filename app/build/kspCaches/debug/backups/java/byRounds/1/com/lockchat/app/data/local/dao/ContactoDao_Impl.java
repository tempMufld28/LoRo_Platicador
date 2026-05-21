package com.lockchat.app.data.local.dao;

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
import com.lockchat.app.data.local.entity.ContactoEntity;
import java.lang.Class;
import java.lang.Exception;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ContactoDao_Impl implements ContactoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ContactoEntity> __insertionAdapterOfContactoEntity;

  private final EntityDeletionOrUpdateAdapter<ContactoEntity> __deletionAdapterOfContactoEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public ContactoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfContactoEntity = new EntityInsertionAdapter<ContactoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `contactos` (`nodeId`,`handle`,`addedAt`,`lastSeen`,`isOnline`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ContactoEntity entity) {
        statement.bindString(1, entity.getNodeId());
        statement.bindString(2, entity.getHandle());
        statement.bindLong(3, entity.getAddedAt());
        if (entity.getLastSeen() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getLastSeen());
        }
        final int _tmp = entity.isOnline() ? 1 : 0;
        statement.bindLong(5, _tmp);
      }
    };
    this.__deletionAdapterOfContactoEntity = new EntityDeletionOrUpdateAdapter<ContactoEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `contactos` WHERE `nodeId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ContactoEntity entity) {
        statement.bindString(1, entity.getNodeId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM contactos WHERE nodeId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ContactoEntity contacto,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfContactoEntity.insert(contacto);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ContactoEntity contacto,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfContactoEntity.handle(contacto);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final String nodeId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, nodeId);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ContactoEntity>> observeAll() {
    final String _sql = "SELECT * FROM contactos ORDER BY handle ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"contactos"}, new Callable<List<ContactoEntity>>() {
      @Override
      @NonNull
      public List<ContactoEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeId");
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "isOnline");
          final List<ContactoEntity> _result = new ArrayList<ContactoEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ContactoEntity _item;
            final String _tmpNodeId;
            _tmpNodeId = _cursor.getString(_cursorIndexOfNodeId);
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            final Long _tmpLastSeen;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmpLastSeen = null;
            } else {
              _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            }
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            _item = new ContactoEntity(_tmpNodeId,_tmpHandle,_tmpAddedAt,_tmpLastSeen,_tmpIsOnline);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object findById(final String nodeId,
      final Continuation<? super ContactoEntity> $completion) {
    final String _sql = "SELECT * FROM contactos WHERE nodeId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, nodeId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ContactoEntity>() {
      @Override
      @Nullable
      public ContactoEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNodeId = CursorUtil.getColumnIndexOrThrow(_cursor, "nodeId");
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfLastSeen = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSeen");
          final int _cursorIndexOfIsOnline = CursorUtil.getColumnIndexOrThrow(_cursor, "isOnline");
          final ContactoEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpNodeId;
            _tmpNodeId = _cursor.getString(_cursorIndexOfNodeId);
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final long _tmpAddedAt;
            _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            final Long _tmpLastSeen;
            if (_cursor.isNull(_cursorIndexOfLastSeen)) {
              _tmpLastSeen = null;
            } else {
              _tmpLastSeen = _cursor.getLong(_cursorIndexOfLastSeen);
            }
            final boolean _tmpIsOnline;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOnline);
            _tmpIsOnline = _tmp != 0;
            _result = new ContactoEntity(_tmpNodeId,_tmpHandle,_tmpAddedAt,_tmpLastSeen,_tmpIsOnline);
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM contactos";
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
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
}
