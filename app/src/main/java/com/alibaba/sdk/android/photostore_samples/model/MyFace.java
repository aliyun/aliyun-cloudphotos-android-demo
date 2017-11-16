/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.photostore.model.Face;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@DatabaseTable(tableName = "face")
public class MyFace {

    private static final String TAG = MyFace.class.getSimpleName();

    @DatabaseField(id = true)
    public long id;

    @DatabaseField
    public String name;

    @DatabaseField
    public boolean isMe;

    @DatabaseField
    public int photosCount;

    @DatabaseField
    public long ctime;

    @DatabaseField
    public long mtime;

    @DatabaseField
    public String state;

    @DatabaseField
    public long coverPhotoId;

    @DatabaseField
    public int coverWidth;

    @DatabaseField
    public int coverHeight;

    @DatabaseField
    public int axisLeft;

    @DatabaseField
    public int axisTop;

    @DatabaseField
    public int axisRight;

    @DatabaseField
    public int axisBottom;

    @DatabaseField
    public String nextCursor;

    public MyFace() {}

    public MyFace(long id) {
        this.id = id;
        this.nextCursor = "0";
    }

    public MyFace(long id, String nextCursor) {
        this.id = id;
        this.coverPhotoId = 0;
        this.nextCursor = nextCursor;
    }

    public MyFace(Face o) {
        this.id = o.id;
        this.name = o.name;
        this.isMe = o.isMe;
        this.photosCount = o.photosCount;
        this.ctime = o.ctime;
        this.mtime = o.mtime;
        this.state = o.state;

        if (o.cover != null) {
            this.coverPhotoId = o.cover.id;
            this.coverWidth = o.cover.width;
            this.coverHeight = o.cover.height;
        } else {
            Log.w(TAG, "cover is null!!");
            this.coverPhotoId = -1;
            this.coverWidth = 0;
            this.coverHeight = 0;
        }

        if (o.axis != null) {
            this.axisLeft = o.axis.getIntValue(0);
            this.axisTop = o.axis.getIntValue(1);
            this.axisRight = o.axis.getInteger(2);
            this.axisBottom = o.axis.getInteger(3);
        }

        this.nextCursor = "0";
    }

    public static void update(final List<Face> faces, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            for (Face f : faces) {
                                try {
                                    if (!TextUtils.isEmpty(f.state) && (f.state.equals("deleted") || (f.state.equals("inactive")))) {
                                        DeleteBuilder<MyFace, Long> db = dao.deleteBuilder();
                                        db.where().eq("id", f.id);
                                        db.delete();
                                    } else {
                                        if (dao.idExists(f.id)) {
                                            UpdateBuilder<MyFace, Long> ub = dao.updateBuilder();
                                            ub.where().eq("id", f.id);
                                            ub.updateColumnValue("name", f.name);
                                            ub.updateColumnValue("isMe", f.isMe);
                                            ub.updateColumnValue("photosCount", f.photosCount);
                                            ub.updateColumnValue("mtime", f.mtime);
                                            ub.updateColumnValue("ctime", f.ctime);
                                            ub.updateColumnValue("state", f.state);
                                            if (f.cover != null) {
                                                ub.updateColumnValue("coverPhotoId", f.cover.id);
                                                ub.updateColumnValue("coverWidth", f.cover.width);
                                                ub.updateColumnValue("coverHeight", f.cover.height);
                                            }
                                            if (f.axis != null) {
                                                ub.updateColumnValue("axisLeft", f.axis.getIntValue(0));
                                                ub.updateColumnValue("axisTop", f.axis.getIntValue(1));
                                                ub.updateColumnValue("axisRight", f.axis.getIntValue(2));
                                                ub.updateColumnValue("axisBottom", f.axis.getIntValue(3));
                                            }
                                            ub.update();
                                        }
                                        dao.create(new MyFace(f));
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
                callback.onCompleted(0, null);
            }
        });
    }

    public static void updateName(final long faceId, final String name, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                QueryBuilder<MyFace, Long> qb = dao.queryBuilder();
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            if (dao.idExists(faceId)) {
                                UpdateBuilder<MyFace, Long> ub = dao.updateBuilder();
                                ub.where().eq("id", faceId);
                                ub.updateColumnValue("name", name);
                                ub.update();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });
            }
        });
    }

    public static void updateCover(final long faceId, final long coverPhotoId, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            if (dao.idExists(faceId)) {
                                UpdateBuilder<MyFace, Long> ub = dao.updateBuilder();
                                ub.where().eq("id", faceId);
                                ub.updateColumnValue("coverPhotoId", coverPhotoId);
                                ub.update();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });

                List<Long> changed = new ArrayList<>();
                changed.add(faceId);

                callback.onCompleted(0, changed);
            }
        });
    }

    public static void changeMe(final long faceId, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                QueryBuilder<MyFace, Long> qb = dao.queryBuilder();
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            long oldFaceId = -1;
                            qb.where().eq("isMe", true);
                            for (MyFace face : qb.query()) {
                                oldFaceId = face.id;
                            }
                            if (dao.idExists(oldFaceId)) {
                                UpdateBuilder<MyFace, Long> ub = dao.updateBuilder();
                                ub.where().eq("id", oldFaceId);
                                ub.updateColumnValue("isMe", false);
                                ub.update();
                            }
                            if (dao.idExists(faceId)) {
                                UpdateBuilder<MyFace, Long> ub = dao.updateBuilder();
                                ub.where().eq("id", faceId);
                                ub.updateColumnValue("isMe", true);
                                ub.update();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });
            }
        });
    }

    public static void updateNextCursorSync(final MyFace face) {
        final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    UpdateBuilder<MyFace, Long> ub = dao.updateBuilder();
                    ub.where().eq("id", face.id);
                    ub.updateColumnValue("nextCursor", face.nextCursor);
                    ub.update();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    public static void getAll(final DatabaseCallback<MyFace> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyFace> result = getAllSync();
                callback.onCompleted(0, result);
            }
        });
    }

    public static void getAllExceptIds(List<Long> ids, final DatabaseCallback<MyFace> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyFace> result = getAllExceptIdsSync(ids);
                callback.onCompleted(0, result);
            }
        });
    }

    public static List<MyFace> getAllSync() {
        final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
        QueryBuilder<MyFace, Long> qb = dao.queryBuilder();
        try {
            qb.orderBy("name", false);
            List<MyFace> result = qb.query();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static List<MyFace> getAllExceptIdsSync(List<Long> ids) {
        final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
        QueryBuilder<MyFace, Long> qb = dao.queryBuilder();
        try {
            qb.orderBy("name", false);
            qb.where().notIn("id", ids);
            List<MyFace> result = qb.query();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static void query(final long limit, final DatabaseCallback<MyFace> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                QueryBuilder<MyFace, Long> qb = dao.queryBuilder();
                qb.orderBy("name", false);
                if (limit > 0) {
                    qb.limit(limit);
                }
                List<MyFace> result = null;
                try {
                    result = qb.query();
                    callback.onCompleted(0, result);
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    callback.onCompleted(1, null);
                }
            }
        });
    }

    public static void getById(final long id, final DatabaseCallback<MyFace> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                QueryBuilder<MyFace, Long> qb = dao.queryBuilder();
                try {
                    qb.where().eq("id", id);
                    callback.onCompleted(0, qb.query());
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    callback.onCompleted(1, null);
                }
            }
        });

    }

    public static void getMaxUploadedAt(final DatabaseCallback<MyFace> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                QueryBuilder<MyFace, Long> qb = dao.queryBuilder();
                try {
                    qb.orderBy("mtime", false);
                    qb.limit(1L);
                    List<MyFace> result = qb.query();
                    callback.onCompleted(0, result);
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    callback.onCompleted(1, null);
                }
            }
        });
    }

    public static void deleteByIds(final List<Long> ids, final DatabaseCallback<Void> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);

                DeleteBuilder<MyFace, Long> db = dao.deleteBuilder();
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

    public static void clear() {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyFace, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyFace.class);
                try {
                    DeleteBuilder<MyFace, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }

}
