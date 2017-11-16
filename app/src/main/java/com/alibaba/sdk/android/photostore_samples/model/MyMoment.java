/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.util.Log;

import com.alibaba.sdk.android.photostore.model.Moment;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@DatabaseTable(tableName = "moment")
public class MyMoment {

    private static final String TAG = MyMoment.class.getSimpleName();

    @DatabaseField(id = true)
    public long id;

    @DatabaseField
    public String locationName;

    @DatabaseField
    public  long takenAt;

    @DatabaseField
    public Long ctime;

    @DatabaseField
    public Long mtime;

    @DatabaseField
    public String state;

    @DatabaseField
    public int photosCount;

    @DatabaseField
    public String nextCursor;

    public MyMoment() {}

    public MyMoment(long id) {
        this.id = id;
        this.nextCursor = "0";
    }

    public MyMoment(long id, String nextCursor) {
        this.id = id;
        this.nextCursor = nextCursor;
    }

    public MyMoment(Moment o) {
        this.id = o.id;
        this.locationName = o.locationName;
        this.photosCount = o.photoCount;
        this.takenAt = o.takenAt;
        this.mtime = o.mtime;
        this.ctime = o.ctime;
        this.state = o.state;
        this.nextCursor = "0";

    }

    public MyMoment(MyMoment o) {
        this.id = o.id;
        this.locationName = o.locationName;
        this.photosCount = o.photosCount;
        this.takenAt = o.takenAt;
        this.mtime = o.mtime;
        this.ctime = o.ctime;
        this.state = o.state;
        this.nextCursor = o.nextCursor;
    }

    public static void update(final List<Moment> moments, final DatabaseCallback<Long> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                // get old version of data
                List<MyMoment> list = getAllSync();

                final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            for (Moment m : moments) {
                                if (m.state.equals("inactive") || m.state.equals("deleted")) {
                                    DeleteBuilder<MyMoment, Long> db = dao.deleteBuilder();
                                    db.where().eq("id", m.id);
                                    db.delete();
                                } else {
                                    if (dao.idExists(m.id)) {
                                        UpdateBuilder<MyMoment, Long> ub = dao.updateBuilder();
                                        ub.where().eq("id", m.id);
                                        ub.updateColumnValue("locationName", m.locationName);
                                        ub.updateColumnValue("photosCount", m.photoCount);
                                        ub.updateColumnValue("takenAt", m.takenAt);
                                        ub.updateColumnValue("mtime", m.mtime);
                                        ub.updateColumnValue("ctime", m.ctime);
                                        ub.updateColumnValue("state", m.state);
                                        ub.update();
                                    } else {
                                        dao.create(new MyMoment(m));
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
                Map<Long, MyMoment> map = new HashMap<>();
                if (list != null) {
                    for (MyMoment m : list) {
                        map.put(m.id, m);
                    }
                }

                List<Long> changed = new ArrayList<>();
                for (Moment m : moments) {
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

    public static void updateNextCursorSync(final MyMoment moment) {
        final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    UpdateBuilder<MyMoment, Long> ub = dao.updateBuilder();
                    ub.where().eq("id", moment.id);
                    ub.updateColumnValue("nextCursor", moment.nextCursor);
                    ub.update();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    public static void invalidLocalPhotoCache(final List<Long> ids, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            for (Long l : ids) {
                                try {
                                    UpdateBuilder<MyMoment, Long> ub = dao.updateBuilder();
                                    ub.where().eq("id", l);
                                    ub.updateColumnValue("mtime", 0);
                                    ub.update();
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

    public static void getById(final long id, final DatabaseCallback<MyMoment> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
                QueryBuilder<MyMoment, Long> qb = dao.queryBuilder();
                try {
                    qb.where().eq("id", id);
                    qb.orderBy("takenAt", false);
                    if (callback != null) callback.onCompleted(0, qb.query());
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    if (callback != null) callback.onCompleted(1, null);
                }
            }
        });
    }

    public static void getByIds(final List<Long> ids, final DatabaseCallback<MyMoment> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
                QueryBuilder<MyMoment, Long> qb = dao.queryBuilder();
                try {
                    qb.where().in("id", ids);
                    qb.orderBy("takenAt", false);
                    if (callback != null) callback.onCompleted(0, qb.query());
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    if (callback != null) callback.onCompleted(1, null);
                }
            }
        });
    }

    public static void getAll(final DatabaseCallback<MyMoment> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyMoment> result = getAllSync();
                callback.onCompleted(0, result);
            }
        });
    }

    public static List<MyMoment> getAllSync() {
        final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
        QueryBuilder<MyMoment, Long> qb = dao.queryBuilder();
        try {
            qb.orderBy("takenAt", false);
            List<MyMoment> result = qb.query();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static void getMaxUploadedAt(final DatabaseCallback<MyMoment> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
                QueryBuilder<MyMoment, Long> qb = dao.queryBuilder();
                try {
                    qb.orderBy("mtime", false);
                    qb.limit(1L);
                    List<MyMoment> result = qb.query();
                    callback.onCompleted(0, result);
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
                final RuntimeExceptionDao<MyMoment, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyMoment.class);
                try {
                    DeleteBuilder<MyMoment, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }

}
