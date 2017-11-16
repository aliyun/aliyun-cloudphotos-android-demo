/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Album;


public class CreateAlbumResponse extends BaseResponse {
    @JSONField(name = "RequestId")
    public String requestId;
    @JSONField(name = "Album")
    public Album album;
}
