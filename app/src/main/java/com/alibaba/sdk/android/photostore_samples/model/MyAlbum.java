/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.util.Log;

import com.alibaba.sdk.android.photostore.model.Album;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by mengzheng on 2017/7/6.
 */

public class MyAlbum {
    private static final String TAG = MyAlbum.class.getSimpleName();

    @DatabaseField(id = true)
    public long id;

    @DatabaseField
    public String name;

    @DatabaseField
    public String remark;

    @DatabaseField
    public String state;

    @DatabaseField
    public int photosCount;

    @DatabaseField
    public long coverPhotoId;

    @DatabaseField
    public boolean isCoverVideo;

    @DatabaseField
    public Long ctime;

    @DatabaseField
    public Long mtime;

    @DatabaseField
    public String nextCursor;


    public MyAlbum() {}

    public MyAlbum(long id) {
        this.id = id;
        this.coverPhotoId = 0;
        nextCursor = "0";
    }

    public MyAlbum(long id, String nextCursor) {
        this.id = id;
        this.coverPhotoId = 0;
        this.nextCursor = nextCursor;
    }

    public MyAlbum(Album o) {
        this.id = o.id;
        this.name = o.name;
        this.remark = o.remark;
        this.photosCount = o.photoCount;
        this.coverPhotoId = 0;
        if (o.cover != null) {
            this.coverPhotoId = o.cover.id;
            this.isCoverVideo = o.cover.isVideo;
        }
        this.mtime = o.mtime;
        this.ctime = o.ctime;
        this.state = o.state;
        this.nextCursor = "0";
    }

    public MyAlbum(MyAlbum o) {
        this.id = o.id;
        this.name = o.name;
        this.remark = o.remark;
        this.photosCount = o.photosCount;
        this.coverPhotoId = o.coverPhotoId;
        this.isCoverVideo = o.isCoverVideo;
        this.mtime = o.mtime;
        this.ctime = o.ctime;
        this.state = o.state;
        this.nextCursor = o.nextCursor;
    }

    public static void update(final List<Album> albums, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // get old version of data
                List<MyAlbum> list = getAllSync();

                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            for (Album m : albums) {
                                if (m.state.equals("inactive")|| m.state.equals("deleted")) {
                                    DeleteBuilder<MyAlbum, Long> db = dao.deleteBuilder();
                                    db.where().eq("id", m.id);
                                    db.delete();
                                } else {
                                    if (dao.idExists(m.id)) {
                                        UpdateBuilder<MyAlbum, Long> ub = dao.updateBuilder();
                                        ub.where().eq("id", m.id);
                                        ub.updateColumnValue("name", m.name);
                                        ub.updateColumnValue("remark", m.remark);
                                        if (m.cover != null) {
                                            ub.updateColumnValue("coverPhotoId", m.cover.id);
                                            ub.updateColumnValue("isCoverVideo", m.cover.isVideo);
                                        }
                                        ub.updateColumnValue("photosCount", m.photoCount);
                                        ub.updateColumnValue("mtime", m.mtime);
                                        ub.updateColumnValue("ctime", m.ctime);
                                        ub.updateColumnValue("state", m.state);
                                        ub.update();
                                    } else {
                                        dao.create(new MyAlbum(m));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });

                // diff
                Map<Long, MyAlbum> map = new HashMap<>();
                if (list != null) {
                    for (MyAlbum m : list) {
                        map.put(m.id, m);
                    }
                }

                List<Long> changed = new ArrayList<>();
                for (Album m : albums) {
                    if (map.containsKey(m.id)) {
                        if (m.mtime > map.get(m.id).mtime) {
                            changed.add(m.id);
                        }
                    } else {
                        changed.add(m.id);
                    }
                }

                callback.onCompleted(0, changed);
            }
        });
    }

    public static void updateName(final long albumId, final String name, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            if (dao.idExists(albumId)) {
                                UpdateBuilder<MyAlbum, Long> ub = dao.updateBuilder();
                                ub.where().eq("id", albumId);
                                ub.updateColumnValue("name", name);
                                ub.update();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });

                List<Long> changed = new ArrayList<>();
                changed.add(albumId);

                callback.onCompleted(0, changed);
            }
        });
    }

    public static void updateRemark(final long albumId, final String remark, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            if (dao.idExists(albumId)) {
                                UpdateBuilder<MyAlbum, Long> ub = dao.updateBuilder();
                                ub.where().eq("id", albumId);
                                ub.updateColumnValue("remark", remark);
                                ub.update();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });

                List<Long> changed = new ArrayList<>();
                changed.add(albumId);

                callback.onCompleted(0, changed);
            }
        });
    }

    public static void updateCover(final long albumId, final long coverPhotoId, final boolean isCoverVideo, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            if (dao.idExists(albumId)) {
                                UpdateBuilder<MyAlbum, Long> ub = dao.updateBuilder();
                                ub.where().eq("id", albumId);
                                ub.updateColumnValue("coverPhotoId", coverPhotoId);
                                ub.updateColumnValue("isCoverVideo", isCoverVideo);
                                ub.update();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.getMessage());
                        }
                        return null;
                    }
                });

                List<Long> changed = new ArrayList<>();
                changed.add(albumId);

                callback.onCompleted(0, changed);
            }
        });
    }

    public static void getAll(final DatabaseCallback<MyAlbum> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyAlbum> result = getAllSync();
                callback.onCompleted(0, result);
            }
        });
    }

    public static void getAllExceptIds(List<Long> ids, final DatabaseCallback<MyAlbum> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyAlbum> result = getAllExceptIdsSync(ids);
                callback.onCompleted(0, result);
            }
        });
    }

    public static List<MyAlbum> getAllSync() {
        final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
        QueryBuilder<MyAlbum, Long> qb = dao.queryBuilder();
        try {
            qb.where().eq("state", "active");
            qb.orderBy("ctime", false);
            List<MyAlbum> result = qb.query();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static List<MyAlbum> getAllExceptIdsSync(List<Long> ids) {
        final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
        QueryBuilder<MyAlbum, Long> qb = dao.queryBuilder();
        try {
            qb.where().eq("state", "active");
            qb.where().notIn("id", ids);
            qb.orderBy("ctime", false);
            List<MyAlbum> result = qb.query();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static void updateNextCursorSync(final MyAlbum album) {
        final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    UpdateBuilder<MyAlbum, Long> ub = dao.updateBuilder();
                    ub.where().eq("id", album.id);
                    ub.updateColumnValue("nextCursor", album.nextCursor);
                    ub.update();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    public static void getMaxUploadedAt(final DatabaseCallback<MyAlbum> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
                QueryBuilder<MyAlbum, Long> qb = dao.queryBuilder();
                try {
                    qb.orderBy("mtime", false);
                    qb.limit(1L);
                    List<MyAlbum> result = qb.query();
                    callback.onCompleted(0, result);
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    callback.onCompleted(1, null);
                }
            }
        });
    }

    public static void getById(final long id, final DatabaseCallback<MyAlbum> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
                QueryBuilder<MyAlbum, Long> qb = dao.queryBuilder();
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

    public static void clear() {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);
                try {
                    DeleteBuilder<MyAlbum, Long> db = dao.deleteBuilder();
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
                final RuntimeExceptionDao<MyAlbum, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyAlbum.class);

                DeleteBuilder<MyAlbum, Long> db = dao.deleteBuilder();
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
