/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import java.util.List;

public class OnGetAlbumsEvent extends BaseEvent {
    public List<Long> albums;

    public OnGetAlbumsEvent(int code, String msg, List<Long> albums) {
        this.code = code;
        this.msg = msg;
        this.albums = albums;
    }
}
