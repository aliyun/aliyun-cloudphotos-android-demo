/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Album;

import java.util.List;

public class ListAlbumsResponse extends NewPagedResponse {
    @JSONField(name = "Albums")
    public List<Album> data;

    public ListAlbumsResponse() {}

    public ListAlbumsResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    @Override
    public boolean hasData() {
        return data != null && data.size() > 0;
    }
}
