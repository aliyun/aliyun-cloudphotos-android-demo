/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class GetPhotoFacesResponse extends BaseResponse {
    @JSONField(name = "Faces")
    public List<FaceInfo> data;

    public static class FaceInfo {
        @JSONField(name = "FaceId")
        public long id;
        @JSONField(name = "Axis")
        public JSONArray axis;
    }
}
