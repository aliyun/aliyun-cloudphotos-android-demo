/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Tag;

import java.util.List;

public class ListTagsResponse extends BaseResponse {
    @JSONField(name = "RequestId")
    public String requestId;
    @JSONField(name = "Tags")
    public List<Tag> data;
}
