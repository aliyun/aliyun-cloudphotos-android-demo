/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

public abstract class PagedResponse extends BaseResponse {

    public PagedResponse() {}

    public PagedResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    public abstract boolean hasData();
}
