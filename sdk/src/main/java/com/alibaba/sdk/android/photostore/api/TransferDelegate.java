/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

public interface TransferDelegate {
    void onStart();

    void onCancel();

    void onPause();

    void onComplete();

    void onError(int var1);

    void ReportSnapInfo(long var1, long var2);
}

