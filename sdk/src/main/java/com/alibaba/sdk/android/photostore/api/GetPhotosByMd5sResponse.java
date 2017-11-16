/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Photo;

import java.util.List;

public class GetPhotosByMd5sResponse extends BaseResponse {
    @JSONField(name = "Photos")
    public List<Photo> data;
}
