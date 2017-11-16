/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class NewPagedResponse extends BaseResponse {
    @JSONField(name = "NextCursor")
    public String nextCursor;
    @JSONField(name = "TotalCount")
    public int totalCount;

    public NewPagedResponse() {}

    public NewPagedResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    public abstract boolean hasData();
}
