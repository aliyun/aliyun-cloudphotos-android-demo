/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import java.util.List;

public class OnGetTagsEvent extends BaseEvent {
    public List<Long> tags;

    public OnGetTagsEvent(int code, String msg, List<Long> tags) {
        this.code = code;
        this.msg = msg;
        this.tags = tags;
    }
}
