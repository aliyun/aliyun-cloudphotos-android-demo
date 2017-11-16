/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.photostore.model.Photo;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@DatabaseTable(tableName = "photo")
public class MyPhoto {
    private static final String TAG = MyPhoto.class.getSimpleName();

    @DatabaseField(id = true)
    public long id;

    @DatabaseField
    public String title;

    @DatabaseField
    public String remark;

    @DatabaseField
    public String fileId;

    @DatabaseField
    public Long ctime;

    @DatabaseField
    public Long mtime;

    @DatabaseField
    public Long takenAt;

    @DatabaseField
    public int width;

    @DatabaseField
    public int height;

    @DatabaseField
    public String state;

    @DatabaseField
    public String md5;

    @DatabaseField
    public boolean isVideo;

    @DatabaseField
    public Long shareExpireTime;

    @DatabaseField
    public Long inactiveTime;

    public MyPhoto() {}

    public MyPhoto(MyPhoto o) {
        this.id = o.id;
        this.title = o.title;
        this.remark = o.remark;
        this.fileId = o.fileId;
        this.ctime = o.ctime;
        this.mtime = o.mtime;
        this.takenAt = o.takenAt;
        this.width = o.width;
        this.height = o.height;
        this.state = o.state;
        this.md5 = o.md5;
        this.isVideo = o.isVideo;
        this.shareExpireTime = o.shareExpireTime;
        this.inactiveTime = o.inactiveTime;
    }

    public MyPhoto(long id) {
        this.id = id;
    }

    public MyPhoto(Photo o) {
        this.id = o.id;
        this.title = o.title;
        this.remark = o.remark;
        this.fileId = o.fileId;
        this.ctime = o.ctime;
        this.mtime = o.mtime;
        this.takenAt = o.takenAt;
        this.width = o.width;
        this.height = o.height;
        this.state = o.state;
        this.md5 = o.md5;
        this.isVideo = o.isVideo;
        this.shareExpireTime = o.shareExpireTime;
        this.inactiveTime = o.inactiveTime;
    }


    public static void getAll(final DatabaseCallback<MyPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyPhoto> result = getAllSync();
                callback.onCompleted(0, result);
            }
        });
    }

    public static void getById(final Long id, final DatabaseCallback<MyPhoto> callback) {
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyPhoto> result = getByIdsSync(ids);
                callback.onCompleted(0, result);
            }
        });
    }

    public static List<MyPhoto> getByIdsSync(final List<Long> ids) {
        final RuntimeExceptionDao<MyPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyPhoto.class);
        QueryBuilder<MyPhoto, Long> qb = dao.queryBuilder();
        try {
            qb.where().in("id", ids);
            qb.orderBy("ctime", false);
            return qb.query();
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
        return null;
    }

    public static List<MyPhoto> getAllSync() {
        final RuntimeExceptionDao<MyPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyPhoto.class);
        QueryBuilder<MyPhoto, Long> qb = dao.queryBuilder();
        try {
            qb.where().eq("state", "active");
            qb.orderBy("ctime", false);
            List<MyPhoto> result = qb.query();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static void updatePhotos(final List<Photo> photos, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyPhoto> list = new ArrayList<>();
                for (final Photo p : photos) {
                    list.add(new MyPhoto(p));
                }
                updateIncrementSync(list);

                callback.onCompleted(0, null);
            }
        });
    }

    private static void updateIncrementSync(final List<MyPhoto> photos) {
        final RuntimeExceptionDao<MyPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Void call() throws Exception {
                try {
                    for (MyPhoto o : photos) {
                        try {
                            if (!TextUtils.isEmpty(o.state) && (o.state.equals("deleted") || o.state.equals("inactive"))) {
                                DeleteBuilder<MyPhoto, Long> db = dao.deleteBuilder();
                                db.where().eq("id", o.id);
                                db.delete();
                            } else {
                                dao.createOrUpdate(o);
                            }
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
    }

    public static void getMaxUpdatedAt(final DatabaseCallback<MyPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyPhoto.class);

                try {
                    QueryBuilder<MyPhoto, Long> qb = dao.queryBuilder();
                    qb.orderBy("mtime", false);
                    qb.limit(1L);
                    callback.onCompleted(0, qb.query());
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    callback.onCompleted(1, null);
                }
            }
        });
    }

    public static void clear() {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyPhoto.class);
                try {
                    DeleteBuilder<MyPhoto, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }

    public static void deleteByIds(final List<Long> ids, final DatabaseCallback<Void> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyPhoto.class);

                DeleteBuilder<MyPhoto, Long> db = dao.deleteBuilder();
                try {
                    db.where().in("id", ids);
                    db.delete();
                    callback.onCompleted(0, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onCompleted(1, null);
                }
            }
        });
    }
}
