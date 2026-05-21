package com.lockchat.app.data.local;

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
import com.lockchat.app.data.local.dao.ContactoDao;
import com.lockchat.app.data.local.dao.ContactoDao_Impl;
import com.lockchat.app.data.local.dao.MensajeDao;
import com.lockchat.app.data.local.dao.MensajeDao_Impl;
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
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ContactoDao _contactoDao;

  private volatile MensajeDao _mensajeDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `contactos` (`nodeId` TEXT NOT NULL, `handle` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, `lastSeen` INTEGER, `isOnline` INTEGER NOT NULL, PRIMARY KEY(`nodeId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `mensajes` (`msgId` TEXT NOT NULL, `contactNodeId` TEXT NOT NULL, `direction` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `leido` INTEGER NOT NULL, `status` TEXT NOT NULL, PRIMARY KEY(`msgId`), FOREIGN KEY(`contactNodeId`) REFERENCES `contactos`(`nodeId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensajes_contactNodeId` ON `mensajes` (`contactNodeId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_mensajes_timestamp` ON `mensajes` (`timestamp`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '235bf93f042f6486e8885649fbea0498')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `contactos`");
        db.execSQL("DROP TABLE IF EXISTS `mensajes`");
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
        db.execSQL("PRAGMA foreign_keys = ON");
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
        final HashMap<String, TableInfo.Column> _columnsContactos = new HashMap<String, TableInfo.Column>(5);
        _columnsContactos.put("nodeId", new TableInfo.Column("nodeId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContactos.put("handle", new TableInfo.Column("handle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContactos.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContactos.put("lastSeen", new TableInfo.Column("lastSeen", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContactos.put("isOnline", new TableInfo.Column("isOnline", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysContactos = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesContactos = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoContactos = new TableInfo("contactos", _columnsContactos, _foreignKeysContactos, _indicesContactos);
        final TableInfo _existingContactos = TableInfo.read(db, "contactos");
        if (!_infoContactos.equals(_existingContactos)) {
          return new RoomOpenHelper.ValidationResult(false, "contactos(com.lockchat.app.data.local.entity.ContactoEntity).\n"
                  + " Expected:\n" + _infoContactos + "\n"
                  + " Found:\n" + _existingContactos);
        }
        final HashMap<String, TableInfo.Column> _columnsMensajes = new HashMap<String, TableInfo.Column>(7);
        _columnsMensajes.put("msgId", new TableInfo.Column("msgId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMensajes.put("contactNodeId", new TableInfo.Column("contactNodeId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMensajes.put("direction", new TableInfo.Column("direction", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMensajes.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMensajes.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMensajes.put("leido", new TableInfo.Column("leido", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMensajes.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMensajes = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMensajes.add(new TableInfo.ForeignKey("contactos", "CASCADE", "NO ACTION", Arrays.asList("contactNodeId"), Arrays.asList("nodeId")));
        final HashSet<TableInfo.Index> _indicesMensajes = new HashSet<TableInfo.Index>(2);
        _indicesMensajes.add(new TableInfo.Index("index_mensajes_contactNodeId", false, Arrays.asList("contactNodeId"), Arrays.asList("ASC")));
        _indicesMensajes.add(new TableInfo.Index("index_mensajes_timestamp", false, Arrays.asList("timestamp"), Arrays.asList("ASC")));
        final TableInfo _infoMensajes = new TableInfo("mensajes", _columnsMensajes, _foreignKeysMensajes, _indicesMensajes);
        final TableInfo _existingMensajes = TableInfo.read(db, "mensajes");
        if (!_infoMensajes.equals(_existingMensajes)) {
          return new RoomOpenHelper.ValidationResult(false, "mensajes(com.lockchat.app.data.local.entity.MensajeEntity).\n"
                  + " Expected:\n" + _infoMensajes + "\n"
                  + " Found:\n" + _existingMensajes);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "235bf93f042f6486e8885649fbea0498", "7d037e4dd7cf8770ba03d94eab1be97e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "contactos","mensajes");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `contactos`");
      _db.execSQL("DELETE FROM `mensajes`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
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
    _typeConvertersMap.put(ContactoDao.class, ContactoDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MensajeDao.class, MensajeDao_Impl.getRequiredConverters());
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
  public ContactoDao contactoDao() {
    if (_contactoDao != null) {
      return _contactoDao;
    } else {
      synchronized(this) {
        if(_contactoDao == null) {
          _contactoDao = new ContactoDao_Impl(this);
        }
        return _contactoDao;
      }
    }
  }

  @Override
  public MensajeDao mensajeDao() {
    if (_mensajeDao != null) {
      return _mensajeDao;
    } else {
      synchronized(this) {
        if(_mensajeDao == null) {
          _mensajeDao = new MensajeDao_Impl(this);
        }
        return _mensajeDao;
      }
    }
  }
}
