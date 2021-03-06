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
 * m:n relation of albums ~ photos
 */
public class MyAlbumPhoto {
    private static final String TAG = MyAlbumPhoto.class.getSimpleName();

    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField
    public long albumId;

    @DatabaseField
    public long photoId;

    @DatabaseField(unique = true)
    public String relation;

    public MyAlbumPhoto() {}

    public MyAlbumPhoto(long albumId, long photoId) {
        this.albumId = albumId;
        this.photoId = photoId;
        relation = String.valueOf(albumId) + "-" + String.valueOf(photoId);
    }

    public static void update(final long albumId, final List<PhotoState> photoStates, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                updateSync(albumId, photoStates);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void update(final long albumId, final List<PhotoState> photoStates, final String nextCursor, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (!nextCursor.equalsIgnoreCase("EOF")) {
                    MyAlbum.updateNextCursorSync(new MyAlbum(albumId, nextCursor));
                }

                updateSync(albumId, photoStates);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void delete(final long albumId, final List<Long> photoIds, final DatabaseCallback<MyAlbumPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                deleteSync(albumId, photoIds);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void deleteByPhotos(final List<Long> photoIds, final DatabaseCallback<MyAlbumPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                deleteByPhotosSync(photoIds);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void getByAlbum(final long albumId, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<Long> ids = getByAlbumSync(albumId);
                if (callback != null) callback.onCompleted(0, ids);
            }
        });
    }

    public static void updateSync(final long albumId, final List<PhotoState> photoStates) {
        final RuntimeExceptionDao<MyAlbumPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbumPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    for (PhotoState ps : photoStates) {
                        try {
                            if (ps.state != null) {
                                if (ps.state.equals("active")) {
                                    dao.createOrUpdate(new MyAlbumPhoto(albumId, ps.id));
                                } else {
                                    DeleteBuilder<MyAlbumPhoto, Long> builder = dao.deleteBuilder();
                                    builder.where().eq("relation", String.valueOf(albumId) + "-" + String.valueOf(ps.id));
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

    private static void deleteSync(final long albumId, final List<Long> photoIds) {
        final RuntimeExceptionDao<MyAlbumPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbumPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    for (Long p : photoIds) {
                        try {
                            DeleteBuilder<MyAlbumPhoto, Long> builder = dao.deleteBuilder();
                            builder.where().eq("relation", String.valueOf(albumId) + "-" + String.valueOf(p));
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
        final RuntimeExceptionDao<MyAlbumPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbumPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    DeleteBuilder<MyAlbumPhoto, Long> builder = dao.deleteBuilder();
                    builder.where().in("photoId", photoIds);
                    builder.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    private static List<Long> getByAlbumSync(final long albumId) {
        final RuntimeExceptionDao<MyAlbumPhoto, Long> daoAlbumPhoto = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbumPhoto.class);
        try {
            QueryBuilder<MyAlbumPhoto, Long> albumPhotoQb = daoAlbumPhoto.queryBuilder();
            albumPhotoQb.where().eq("albumId", albumId);
            List<MyAlbumPhoto> albumPhotos = albumPhotoQb.query();
            List<Long> photoIds = new ArrayList<>();
            for (MyAlbumPhoto mp : albumPhotos) {
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
                final RuntimeExceptionDao<MyAlbumPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbumPhoto.class);
                try {
                    DeleteBuilder<MyAlbumPhoto, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }
}
