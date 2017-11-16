/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

public class OnGetFacesPhotosEvent extends BaseEvent {
    public Long faceId;

    public OnGetFacesPhotosEvent(int code, String msg, Long faceId) {
        this.code = code;
        this.msg = msg;
        this.faceId = faceId;
    }
}
