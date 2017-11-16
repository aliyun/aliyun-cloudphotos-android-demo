/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@DatabaseTable(tableName = "uploaded_photo")
public class UploadedPhoto {

    private static final String TAG = UploadedPhoto.class.getSimpleName();

    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField(unique = true)
    public String fullPath;

    @DatabaseField
    public String folder;

    @DatabaseField
    public String name;

    @DatabaseField
    public long size;

    @DatabaseField
    public long lastModified;

    @DatabaseField
    public String md5;

    public UploadedPhoto() {

    }

    public UploadedPhoto(File file) {
        if (file != null && file.isFile()) {
            this.fullPath = file.getAbsolutePath();
            this.folder = file.getParent();
            this.name = file.getName();
            this.size = file.length();
            this.lastModified = file.lastModified();
            this.md5 = "";
        }
    }

    public static void update(final List<UploadedPhoto> list, final DatabaseCallback<UploadedPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<UploadedPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(UploadedPhoto.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            for (UploadedPhoto o : list) {
                                try {
                                    dao.createOrUpdate(o);
                                } catch (Exception e) {
                                    Log.w(TAG, e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void getAll(final List<String> folderList, final DatabaseCallback<UploadedPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final RuntimeExceptionDao<UploadedPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(UploadedPhoto.class);
                    QueryBuilder<UploadedPhoto, Long> qb = dao.queryBuilder();
                    qb.where().in("folder", folderList);

                    if (callback != null) callback.onCompleted(0, qb.query());
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    if (callback != null) callback.onCompleted(1, null);
                }
            }
        });
    }

    public String toString() {
        return id + " " + fullPath + " " + folder + " " + name;
    }

    public static void clear() {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<UploadedPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(UploadedPhoto.class);
                try {
                    DeleteBuilder<UploadedPhoto, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }

}
