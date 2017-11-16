/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import java.util.List;

public class OnRemoveInactivePhotosEvent extends BaseEvent {
    public List<Long> photos;

    public OnRemoveInactivePhotosEvent(int code, String msg, List<Long> photos) {
        this.code = code;
        this.msg = msg;
        this.photos = photos;
    }
}
