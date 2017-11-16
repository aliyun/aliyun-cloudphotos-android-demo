/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

public class OnGetAlbumPhotosEvent extends BaseEvent {
    public Long albumId;

    public OnGetAlbumPhotosEvent(int code, String msg, Long albumId) {
        this.code = code;
        this.msg = msg;
        this.albumId = albumId;
    }
}
