/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

public class OnCreateAlbumEvent extends BaseEvent {

    public OnCreateAlbumEvent(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
