/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.PhotoState;

import java.util.List;

public class ListAlbumPhotosResponse extends NewPagedResponse {
    @JSONField(name = "Results")
    public List<PhotoState> data;

    public ListAlbumPhotosResponse() {}

    public ListAlbumPhotosResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    @Override
    public boolean hasData() {
        return data != null && data.size() > 0;
    }
}
