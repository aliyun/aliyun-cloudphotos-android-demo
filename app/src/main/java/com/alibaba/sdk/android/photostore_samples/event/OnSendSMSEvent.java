/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

public class OnSendSMSEvent extends BaseEvent {

    public OnSendSMSEvent() {}

    public OnSendSMSEvent(int code, String msg) {
        this.code = 0;
        this.msg = msg;
    }
}
