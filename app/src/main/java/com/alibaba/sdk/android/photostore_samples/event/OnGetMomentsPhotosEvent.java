/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import java.util.List;

public class OnGetMomentsPhotosEvent extends BaseEvent {

    public List<Long> moments;

    public OnGetMomentsPhotosEvent(int code, String msg, List<Long> moments) {
        this.code = code;
        this.msg = msg;
        this.moments = moments;
    }

}
