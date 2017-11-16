/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import com.alibaba.sdk.android.photostore.model.Quota;

public class OnGetQuotaEvent extends BaseEvent {
    public Quota quota;

    public OnGetQuotaEvent(int code, String msg,  Quota quota) {
        this.code = code;
        this.msg = msg;
        this.quota = quota;
    }
}
