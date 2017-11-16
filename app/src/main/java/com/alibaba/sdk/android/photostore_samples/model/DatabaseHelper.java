/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final int DATABASE_VERSION = 49;

    private static DatabaseHelper sInstance;

    private HashMap<Class, RuntimeExceptionDao> runtimeExceptionDaoCacheMap;
    private HashMap<Class, Dao> daoCacheMap;

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private static String databaseName;

    public static void init(Context context, String database_name) {
        databaseName = database_name;
        sInstance = new DatabaseHelper(context);
    }

    public static DatabaseHelper getInstance() {
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, databaseName, null, DATABASE_VERSION);
        daoCacheMap = new HashMap<>();
        runtimeExceptionDaoCacheMap = new HashMap<>();
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        Log.i(DatabaseHelper.class.getName(), "onCreate");
        createTable(db, connectionSource);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        Log.i(DatabaseHelper.class.getName(), "onUpgrade");
        updateTable(db, connectionSource, oldVersion, newVersion);
    }

    @Override
    public void close() {
        super.close();
        daoCacheMap.clear();
        sInstance = null;
    }

    private void createTable(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, MyPhoto.class);
            TableUtils.createTable(connectionSource, MyMoment.class);
            TableUtils.createTable(connectionSource, MyMomentPhoto.class);
            TableUtils.createTable(connectionSource, MyFace.class);
            TableUtils.createTable(connectionSource, MyFacePhoto.class);
            TableUtils.createTable(connectionSource, UploadedPhoto.class);
            TableUtils.createTable(connectionSource, MyAlbum.class);
            TableUtils.createTable(connectionSource, MyAlbumPhoto.class);
            TableUtils.createTable(connectionSource, MyTag.class);
            TableUtils.createTable(connectionSource, MyTagPhoto.class);
            TableUtils.createTable(connectionSource, MyCursor.class);
        } catch (Exception e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            e.printStackTrace();
        }
    }

    private void updateTable(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, MyPhoto.class, true);
            TableUtils.dropTable(connectionSource, MyMoment.class, true);
            TableUtils.dropTable(connectionSource, MyMomentPhoto.class, true);
            TableUtils.dropTable(connectionSource, MyFace.class, true);
            TableUtils.dropTable(connectionSource, MyFacePhoto.class, true);
            TableUtils.dropTable(connectionSource, UploadedPhoto.class, true);
            TableUtils.dropTable(connectionSource, MyAlbum.class, true);
            TableUtils.dropTable(connectionSource, MyAlbumPhoto.class, true);
            TableUtils.dropTable(connectionSource, MyTag.class, true);
            TableUtils.dropTable(connectionSource, MyTagPhoto.class, true);
            TableUtils.dropTable(connectionSource, MyCursor.class, true);
            onCreate(db, connectionSource);
        } catch (Exception e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    public <D extends Dao<T, ?>, T> D getCachedDao(Class<T> clazz) {
        Dao dao = daoCacheMap.get(clazz);
        if (dao == null) {
            try {
                dao = this.getDao(clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            daoCacheMap.put(clazz, dao);
        }
        return (D) dao;
    }

    public <D extends RuntimeExceptionDao<T, ?>, T> D getCachedRuntimeExceptionDao(Class<T> clazz) {
        RuntimeExceptionDao dao = runtimeExceptionDaoCacheMap.get(clazz);
        if (dao == null) {
            dao = this.getRuntimeExceptionDao(clazz);
            runtimeExceptionDaoCacheMap.put(clazz, dao);
        }
        return (D) dao;
    }

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public void clearAll() {
    }
}
