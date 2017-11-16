/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import com.alibaba.sdk.android.photostore.model.User;

public class OnLoginEvent extends BaseEvent {

    public User user;

    public OnLoginEvent() {}

    public OnLoginEvent(int code, String msg, User user) {
        this.code = code;
        this.msg = msg;
        this.user = user;
    }
}
