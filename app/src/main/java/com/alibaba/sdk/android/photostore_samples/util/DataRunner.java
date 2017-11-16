/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All data write should be run on this single threaded data runner
 * to prevent data processing on main thread or concurrent modification
 */
public class DataRunner {

    private static DataRunner sInstance;

    private ExecutorService executorService;

    public static DataRunner getInstance() {
        if (sInstance == null) {
            synchronized (DataRunner.class) {
                sInstance = new DataRunner();
            }
        }
        return sInstance;
    }

    public DataRunner() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

}
