/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;

public class GetVideoCoverResponse extends BaseResponse {
    @JSONField(name = "VideoCoverUrl")
    public String url;
}
