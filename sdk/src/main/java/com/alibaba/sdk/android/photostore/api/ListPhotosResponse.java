/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Photo;

import java.util.List;

public class ListPhotosResponse extends NewPagedResponse {

    @JSONField(name = "Photos")
    public List<Photo> data;

    public ListPhotosResponse() {}

    public ListPhotosResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    @Override
    public boolean hasData() {
        return data != null && data.size() > 0;
    }
}
