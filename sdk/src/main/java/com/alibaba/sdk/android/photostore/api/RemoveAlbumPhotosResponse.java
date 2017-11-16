/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class RemoveAlbumPhotosResponse extends BaseResponse {
    @JSONField(name = "RequestId")
    public String requestId;
    @JSONField(name = "Results")
    public List<RemoveAlbumPhotosResponse.CommonInfo> data;

    public static class CommonInfo {
        @JSONField(name = "Id")
        public long id;
        @JSONField(name = "Code")
        public String code;
        @JSONField(name = "Message")
        public String msg;
    }
}
