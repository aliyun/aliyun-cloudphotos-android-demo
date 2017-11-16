/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.util.Log;

import com.alibaba.sdk.android.photostore.model.Tag;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@DatabaseTable(tableName = "tag")
public class MyTag {

    private static final String TAG = MyTag.class.getSimpleName();

    @DatabaseField(id = true)
    public long id;

    @DatabaseField
    public String name;

    @DatabaseField
    public String parentTag;

    @DatabaseField
    public boolean isSubTag;

    @DatabaseField
    public long coverPhotoId;

    @DatabaseField
    public String nextCursor;

    public MyTag() {}

    public MyTag(long id) {
        this.id = id;
        this.nextCursor = "0";
    }

    public MyTag(long id, String nextCursor) {
        this.id = id;
        this.coverPhotoId = 0;
        this.nextCursor = nextCursor;
    }

    public MyTag(Tag o) {
        this.id = o.id;
        this.name = o.name;
        this.parentTag = o.parentTag;
        this.isSubTag = o.isSubTag;
        if (o.cover != null) {
            this.coverPhotoId = o.cover.id;
        }
        this.nextCursor = "0";
    }

    public static void update(final List<Tag> tags, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyTag, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTag.class);
                dao.callBatchTasks(new Callable() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            List<Long> ids = new ArrayList<>();
                            for (Tag t : tags) {
                                try {
                                    ids.add(t.id);
                                    if (dao.idExists(t.id)) {
                                        UpdateBuilder<MyTag, Long> ub = dao.updateBuilder();
                                        ub.where().eq("id", t.id);
                                        ub.updateColumnValue("name", t.name);
                                        ub.updateColumnValue("parentTag", t.parentTag);
                                        ub.updateColumnValue("isSubTag", t.isSubTag);
                                        if (t.cover != null) {
                                            ub.updateColumnValue("coverPhotoId", t.cover.id);
                                        }
                                        ub.update();
                                    }
                                    else {
                                        dao.create(new MyTag(t));
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, e.getMessage());
                                }
                            }

                            DeleteBuilder<MyTag, Long> db = dao.deleteBuilder();
                            db.where().notIn("id", ids);
                            db.delete();
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

    public static void updateNextCursorSync(final MyTag tag) {
        final RuntimeExceptionDao<MyTag, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTag.class);
        dao.callBatchTasks(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    UpdateBuilder<MyTag, Long> ub = dao.updateBuilder();
                    ub.where().eq("id", tag.id);
                    ub.updateColumnValue("nextCursor", tag.nextCursor);
                    ub.update();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    public static void getAllExceptSubTag(final DatabaseCallback<MyTag> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<MyTag> result = getAllSyncExceptSubTag();
                callback.onCompleted(0, result);
            }
        });
    }

    private static List<MyTag> getAllSyncExceptSubTag() {
        final RuntimeExceptionDao<MyTag, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTag.class);
        QueryBuilder<MyTag, Long> qb = dao.queryBuilder();
        try {
            qb.where().eq("isSubTag", false);
            List<MyTag> result = qb.query();
            return result;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return null;
        }
    }

    public static void queryExceptSubTag(final long limit, final DatabaseCallback<MyTag> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyTag, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTag.class);
                QueryBuilder<MyTag, Long> qb = dao.queryBuilder();
                if (limit > 0) {
                    qb.limit(limit);
                }
                List<MyTag> result = null;
                try {
                    qb.where().eq("isSubTag", false);
                    result = qb.query();
                    callback.onCompleted(0, result);
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    callback.onCompleted(1, null);
                }
            }
        });
    }

    public static void getById(final long id, final DatabaseCallback<MyTag> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyTag, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTag.class);
                QueryBuilder<MyTag, Long> qb = dao.queryBuilder();
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
                final RuntimeExceptionDao<MyTag, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyTag.class);
                try {
                    DeleteBuilder<MyTag, Long> db = dao.deleteBuilder();
                    db.where().gt("id", 0);
                    db.delete();
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }
}
