/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.PhotoStore;

public class GetPhotoStoreResponse extends BaseResponse {
    @JSONField(name = "PhotoStore")
    public PhotoStore photoStore;
}
