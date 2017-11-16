/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import java.util.List;

public class OnGetStyledPhotosEvent extends BaseEvent {
    public List<Long> combiners;

    public OnGetStyledPhotosEvent(int code, String msg, List<Long> combiners) {
        this.code = code;
        this.msg = msg;
        this.combiners = combiners;
    }
}
