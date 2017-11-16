/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import java.util.List;

public class OnGetCategoriesEvent extends BaseEvent {
    public List<Long> categories;

    public OnGetCategoriesEvent(int code, String msg, List<Long> categories) {
        this.code = code;
        this.msg = msg;
        this.categories = categories;
    }
}
