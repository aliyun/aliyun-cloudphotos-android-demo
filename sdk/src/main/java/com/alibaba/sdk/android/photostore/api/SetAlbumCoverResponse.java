/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;

public class SetAlbumCoverResponse extends BaseResponse {
    @JSONField(name = "RequestId")
    public String requestId;
}
