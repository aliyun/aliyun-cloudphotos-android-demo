/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Moment;

import java.util.List;

public class ListMomentsResponse extends NewPagedResponse {
    @JSONField(name = "Moments")
    public List<Moment> data;

    public ListMomentsResponse() {}

    public ListMomentsResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    @Override
    public boolean hasData() {
        return data != null && data.size() > 0;
    }
}
