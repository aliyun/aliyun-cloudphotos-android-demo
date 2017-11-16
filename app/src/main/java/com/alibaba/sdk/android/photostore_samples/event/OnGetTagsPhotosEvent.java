/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

public class OnGetTagsPhotosEvent extends BaseEvent {
    public Long tagId;

    public OnGetTagsPhotosEvent(int code, String msg, Long tagId) {
        this.code = code;
        this.msg = msg;
        this.tagId = tagId;
    }
}
