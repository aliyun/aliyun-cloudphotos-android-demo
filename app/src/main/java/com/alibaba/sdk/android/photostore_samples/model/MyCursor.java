/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;
import java.util.concurrent.Callable;

@DatabaseTable(tableName = "cursor")
public class MyCursor {
    private static final String TAG = MyCursor.class.getSimpleName();

    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField(unique = true)
    public String key;

    @DatabaseField
    public String cursor;

    public MyCursor() {}

    public MyCursor(String key, String cursor) {
        this.key = key;
        this.cursor = cursor;
    }

    public static void update(final String key, final String nextCursor, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                updateSync(key, nextCursor);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    private static void updateSync(final String key, final String nextCursor) {
        final RuntimeExceptionDao<MyCursor, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyCursor.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    QueryBuilder<MyCursor, Long> cursorQb = dao.queryBuilder();
                    cursorQb.where().eq("key", key);
                    List<MyCursor> cursor = cursorQb.query();
                    if (cursor != null && cursor.size() > 0) {
                        UpdateBuilder<MyCursor, Long> ub = dao.updateBuilder();
                        ub.where().eq("key", key);
                        ub.updateColumnValue("cursor", nextCursor);
                        ub.update();
                    } else {
                        dao.create(new MyCursor(key, nextCursor));
                    }
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    public static void getNextCursor(final String key, final DatabaseCallback<MyCursor> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MyCursor, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MyCursor.class);
                QueryBuilder<MyCursor, Long> qb = dao.queryBuilder();
                try {
                    qb.where().eq("key", key);
                    callback.onCompleted(0, qb.query());
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    callback.onCompleted(1, null);
                }
            }
        });
    }
}
