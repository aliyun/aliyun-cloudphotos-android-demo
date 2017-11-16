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
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * m:n relation of faces ~ photos
 */
@DatabaseTable(tableName = "face_photo")
public class MyFacePhoto {
    private static final String TAG = MyFacePhoto.class.getSimpleName();

    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField
    public long faceId;

    @DatabaseField
    public long photoId;

    @DatabaseField(unique = true)
    public String relation;

    public MyFacePhoto() {}

    public MyFacePhoto(long faceId, long photoId) {
        this.faceId = faceId;
        this.photoId = photoId;
        relation = String.valueOf(faceId) + "-" + String.valueOf(photoId);
    }

    public static void update(final long faceId, final List<PhotoState> photoStates, final String nextCursor, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (!nextCursor.equalsIgnoreCase("EOF")) {
                    MyFace.updateNextCursorSync(new MyFace(faceId, nextCursor));
                }

                updateSync(faceId, photoStates);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void delete(final long faceId, final List<Long> photoIds, final DatabaseCallback<MyFacePhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                deleteSync(faceId, photoIds);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void getByFace(final long faceId, final DatabaseCallback<MyPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<Long> photoIds = getByFaceSync(faceId);
                List<MyPhoto> photos = MyPhoto.getByIdsSync(photoIds);
                if (callback != null) callback.onCompleted(0, photos);
            }
        });
    }

    public static void deleteByPhotos(final List<Long> photoIds, final DatabaseCallback<MyFacePhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                deleteByPhotosSync(photoIds);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    private static void updateSync(final long faceId, final List<PhotoState> photoStates) {
        final RuntimeExceptionDao<MyFacePhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFacePhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    for (PhotoState ps : photoStates) {
                        try {
                            if (ps.state != null) {
                                if (ps.state.equals("active")) {
                                    dao.createOrUpdate(new MyFacePhoto(faceId, ps.id));
                                } else {
                                    DeleteBuilder<MyFacePhoto, Long> builder = dao.deleteBuilder();
                                    builder.where().eq("relation", String.valueOf(faceId) + "-" + String.valueOf(ps.id));
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

    private static void deleteSync(final long faceId, final List<Long> photoIds) {
        final RuntimeExceptionDao<MyFacePhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFacePhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    for (Long p : photoIds) {
                        try {
                            DeleteBuilder<MyFacePhoto, Long> builder = dao.deleteBuilder();
                            builder.where().eq("relation", String.valueOf(faceId) + "-" + String.valueOf(p));
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
        final RuntimeExceptionDao<MyFacePhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFacePhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    DeleteBuilder<MyFacePhoto, Long> builder = dao.deleteBuilder();
                    builder.where().in("photo_id", photoIds);
                    builder.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    private static List<Long> getByFaceSync(final long faceId) {
        final RuntimeExceptionDao<MyFacePhoto, Long> daoFacePhoto = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFacePhoto.class);
        try {
            QueryBuilder<MyFacePhoto, Long> facePhotoQb = daoFacePhoto.queryBuilder();
            facePhotoQb.where().eq("faceId", faceId);
            List<MyFacePhoto> facePhotos = facePhotoQb.query();
            List<Long> photoIds = new ArrayList<>();
            for (MyFacePhoto fp : facePhotos) {
                photoIds.add(fp.photoId);
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
                final RuntimeExceptionDao<MyFacePhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFacePhoto.class);
                try {
                    DeleteBuilder<MyFacePhoto, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }

}
