/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

public class OnGetCategoriesPhotosEvent extends BaseEvent {
    public Long categoryId;

    public OnGetCategoriesPhotosEvent(int code, String msg, Long faceId) {
        this.code = code;
        this.msg = msg;
        this.categoryId = categoryId;
    }
}
