/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.util.Log;

import com.alibaba.sdk.android.photostore.model.PhotoState;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * m:n relation of moments ~ photos
 */
public class MyMomentPhoto {
    private static final String TAG = MyMomentPhoto.class.getSimpleName();

    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField
    public long momentId;

    @DatabaseField
    public long photoId;

    @DatabaseField(unique = true)
    public String relation;

    public MyMomentPhoto() {}

    public MyMomentPhoto(long momentId, long photoId) {
        this.momentId = momentId;
        this.photoId = photoId;
        relation =  String.valueOf(momentId) + "-" + String.valueOf(photoId);
    }

    public static void update(final long momentId, final List<PhotoState> photoStates, final String nextCursor, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (!nextCursor.equalsIgnoreCase("EOF")) {
                    MyMoment.updateNextCursorSync(new MyMoment(momentId, nextCursor));
                }

                updateSync(momentId, photoStates);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void delete(final long momentId, final List<Long> photoIds, final DatabaseCallback<MyMomentPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                deleteSync(momentId, photoIds);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void getByMoment(final long momentId, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<Long> ids = getByMomentSync(momentId);
                if (callback != null) callback.onCompleted(0, ids);
            }
        });
    }

    public static void deleteByPhotos(final List<Long> photoIds, final DatabaseCallback<MyMomentPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                deleteByPhotosSync(photoIds);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    private static void updateSync(final long momentId, final List<PhotoState> photoStates) {
        final RuntimeExceptionDao<MyMomentPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMomentPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    for (PhotoState ps : photoStates) {
                        try {
                            if (ps.state != null) {
                                if (ps.state.equals("active")) {
                                    dao.createOrUpdate(new MyMomentPhoto(momentId, ps.id));
                                } else {
                                    DeleteBuilder<MyMomentPhoto, Long> builder = dao.deleteBuilder();
                                    builder.where().eq("photoId", ps.id);
                                    builder.delete();
                                }
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

    private static void deleteSync(final long momentId, final List<Long> photoIds) {
        final RuntimeExceptionDao<MyMomentPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMomentPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    for (Long p : photoIds) {
                        try {
                            DeleteBuilder<MyMomentPhoto, Long> builder = dao.deleteBuilder();
                            builder.where().eq("relation", String.valueOf(momentId) + "-" + String.valueOf(p));
                            builder.delete();
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

    private static void deleteByPhotosSync(final List<Long> photoIds) {
        final RuntimeExceptionDao<MyMomentPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMomentPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    DeleteBuilder<MyMomentPhoto, Long> builder = dao.deleteBuilder();
                    builder.where().in("photoId", photoIds);
                    builder.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    private static List<Long> getByMomentSync(final long momentId) {
        final RuntimeExceptionDao<MyMomentPhoto, Long> daoMomentPhoto = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMomentPhoto.class);
        try {
            QueryBuilder<MyMomentPhoto, Long> momentPhotoQb = daoMomentPhoto.queryBuilder();
            momentPhotoQb.where().eq("momentId", momentId);
            List<MyMomentPhoto> momentPhotos = momentPhotoQb.query();
            List<Long> photoIds = new ArrayList<>();
            for (MyMomentPhoto mp : momentPhotos) {
                photoIds.add(mp.photoId);
            }

            return photoIds;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static void clear() {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyMomentPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMomentPhoto.class);
                try {
                    DeleteBuilder<MyMomentPhoto, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }
}
