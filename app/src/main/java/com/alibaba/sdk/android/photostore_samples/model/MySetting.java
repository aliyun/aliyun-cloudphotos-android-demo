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

@DatabaseTable(tableName = "setting")
public class MySetting {
    private static final String TAG = MySetting.class.getSimpleName();

    @DatabaseField(generatedId = true)
    public long id;

    @DatabaseField(unique = true)
    public String key;

    @DatabaseField
    public String value;

    public MySetting() {}

    public MySetting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static void update(final String key, final String value, final DatabaseCallback callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                updateSync(key, value);
                if (callback != null) callback.onCompleted(0, null);
            }
        });
    }

    private static void updateSync(final String key, final String value) {
        final RuntimeExceptionDao<MySetting, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MySetting.class);
        dao.callBatchTasks(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    QueryBuilder<MySetting, Long> settingQb = dao.queryBuilder();
                    settingQb.where().eq("key", key);
                    List<MySetting> settings = settingQb.query();
                    if (settings != null && settings.size() > 0) {
                        UpdateBuilder<MySetting, Long> ub = dao.updateBuilder();
                        ub.where().eq("key", key);
                        ub.updateColumnValue("value", value);
                        ub.update();
                    } else {
                        dao.create(new MySetting(key, value));
                    }
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
                return null;
            }
        });
    }

    public static void getValue(final String key, final DatabaseCallback<MySetting> callback) {
        DatabaseHelper.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final RuntimeExceptionDao<MySetting, Long> dao = DatabaseHelper.getInstance().getCachedRuntimeExceptionDao(MySetting.class);
                QueryBuilder<MySetting, Long> qb = dao.queryBuilder();
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
