/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import java.util.List;

public class OnGetFacesEvent extends BaseEvent {
    public List<Long> faces;

    public OnGetFacesEvent(int code, String msg, List<Long> faces) {
        this.code = code;
        this.msg = msg;
        this.faces = faces;
    }
}
