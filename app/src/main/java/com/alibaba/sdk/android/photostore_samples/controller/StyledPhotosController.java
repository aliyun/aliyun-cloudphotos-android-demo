/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore_samples.BusProvider;

public class StyledPhotosController {
    String TAG = StyledPhotosController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static StyledPhotosController sInstance;

    public static StyledPhotosController getInstance() {
        if (sInstance == null) {
            synchronized (StyledPhotosController.class) {
                sInstance = new StyledPhotosController();
            }
        }
        return sInstance;
    }

    public StyledPhotosController() {
        BusProvider.getInstance().register(this);
    }
}
