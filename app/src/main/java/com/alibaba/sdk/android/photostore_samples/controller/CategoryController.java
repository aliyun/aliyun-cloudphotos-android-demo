/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore_samples.BusProvider;

public class CategoryController {
    String TAG = CategoryController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static CategoryController sInstance;

    public static CategoryController getInstance() {
        if (sInstance == null) {
            synchronized (CategoryController.class) {
                sInstance = new CategoryController();
            }
        }
        return sInstance;
    }

    public CategoryController() {
        BusProvider.getInstance().register(this);
    }
}
