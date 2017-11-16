/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.model;

import java.util.List;

public interface DatabaseCallback<T> {
    void onCompleted(int code, List<T> data);
}
