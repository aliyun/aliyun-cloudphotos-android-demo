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
 * m:n relation of tags ~ photos
 */
@DatabaseTable(tableName = "tag_photo")
public class MyTagPhoto {
    private static final String TAG = MyTagPhoto.class.getSimpleName();

    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField
    public long tagId;

    @DatabaseField
    public long photoId;

    @DatabaseField(unique = true)
    public String relation;

    public MyTagPhoto() {}

    public MyTagPhoto(long tagId, long photoId) {
        this.tagId = tagId;
        this.photoId = photoId;
        relation = String.valueOf(tagId) + "-" + String.valueOf(photoId);
    }

    public static void update(final long tagId, final List<PhotoState> photoStates, final String nextCursor, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (!nextCursor.equalsIgnoreCase("EOF")) {
                    MyTag.updateNextCursorSync(new MyTag(tagId, nextCursor));
                }

                updateSync(tagId, photoStates);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void deleteByPhotos(final List<Long> photoIds, final DatabaseCallback<MyTagPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                deleteByPhotosSync(photoIds);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    public static void getByTag(final long tagId, final DatabaseCallback<MyPhoto> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<Long> ids = getByTagSync(tagId);
                List<MyPhoto> photos = MyPhoto.getByIdsSync(ids);
                if (callback != null)
                    callback.onCompleted(0, photos);
            }
        });
    }

    private static void updateSync(final long tagId, final List<PhotoState> photoStates) {
        final RuntimeExceptionDao<MyTagPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTagPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    for (PhotoState ps : photoStates) {
                        try {
                            if (ps.state != null) {
                                if (ps.state.equals("active")) {
                                    dao.createOrUpdate(new MyTagPhoto(tagId, ps.id));
                                }
                                else {
                                    DeleteBuilder<MyTagPhoto, Long> builder = dao.deleteBuilder();
                                    builder.where().eq("relation", String.valueOf(tagId) + "-" + String.valueOf(ps.id));
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

    private static void deleteByPhotosSync(final List<Long> photoIds) {
        final RuntimeExceptionDao<MyTagPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTagPhoto.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    DeleteBuilder<MyTagPhoto, Long> builder = dao.deleteBuilder();
                    builder.where().in("photo_id", photoIds);
                    builder.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    private static List<Long> getByTagSync(final long tagId) {
        final RuntimeExceptionDao<MyTagPhoto, Long> daoTagPhoto = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTagPhoto.class);
        try {
            QueryBuilder<MyTagPhoto, Long> tagPhotoQb = daoTagPhoto.queryBuilder();
            tagPhotoQb.where().eq("tagId", tagId);
            List<MyTagPhoto> tagPhotos = tagPhotoQb.query();
            List<Long> photoIds = new ArrayList<>();
            for (MyTagPhoto tp : tagPhotos) {
                photoIds.add(tp.photoId);
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
                final RuntimeExceptionDao<MyTagPhoto, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTagPhoto.class);
                try {
                    DeleteBuilder<MyTagPhoto, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }

}
