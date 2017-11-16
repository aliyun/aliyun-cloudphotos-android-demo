/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;
import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.GetQuotaResponse;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.event.OnGetQuotaEvent;

public class AccountController {
    String TAG = AccountController.class.getSimpleName();

    private Handler handler = new Handler(Looper.getMainLooper());

    private static AccountController sInstance;

    public static AccountController getInstance() {
        if (sInstance == null) {
            synchronized (AccountController.class) {
                sInstance = new AccountController();
            }
        }
        return sInstance;
    }

    private AccountController() {
        BusProvider.getInstance().register(this);
    }

    public void getQuota() {
        PhotoStoreClient.getInstance().getQuota(new com.alibaba.sdk.android.photostore.runner.Callback<GetQuotaResponse>() {
            @Override
            public void onSuccess(GetQuotaResponse response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(new OnGetQuotaEvent(0, response.msg, response.quota));
                    }
                });
            }

            @Override
            public void onFailure(int code, com.alibaba.sdk.android.photostore.api.BaseResponse response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(new OnGetQuotaEvent(-1, "", null));
                    }
                });
            }
        });
    }
}
