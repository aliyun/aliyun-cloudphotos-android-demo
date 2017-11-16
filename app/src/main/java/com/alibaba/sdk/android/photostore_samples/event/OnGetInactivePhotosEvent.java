/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import com.alibaba.sdk.android.photostore.model.Photo;

import java.util.List;

public class OnGetInactivePhotosEvent extends BaseEvent {
    public List<Photo> photos;

    public OnGetInactivePhotosEvent(int code, String msg, List<Photo> photos) {
        this.code = code;
        this.msg = msg;
        this.photos = photos;
    }
}
