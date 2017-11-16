/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import com.alibaba.sdk.android.photostore_samples.constants.ContentType;

public class OnSearchEvent extends BaseEvent {

    public ContentType what;

    public OnSearchEvent(int code, String msg, ContentType what) {
        this.code = code;
        this.msg = msg;
        this.what = what;
    }
}
