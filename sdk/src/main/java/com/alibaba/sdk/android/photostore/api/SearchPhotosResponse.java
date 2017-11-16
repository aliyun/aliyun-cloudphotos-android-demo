/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Photo;

import java.util.List;

public class SearchPhotosResponse extends PagedResponse {
    @JSONField(name = "RequestId")
    public String requestId;
    @JSONField(name = "TotalCount")
    public int totalCount;
    @JSONField(name = "Photos")
    public List<Photo> data;

    public SearchPhotosResponse() {}

    public SearchPhotosResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    @Override
    public boolean hasData() {
        return data != null && data.size() > 0;
    }
}
