/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Face;

import java.util.List;

public class ListFacesResponse extends NewPagedResponse {
    @JSONField(name = "Faces")
    public List<Face> data;

    public ListFacesResponse() {}

    public ListFacesResponse(String code, String msg, String action) {
        super(code, msg, action);
    }

    @Override
    public boolean hasData() {
        return data != null && data.size() > 0;
    }
}
