/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.sdk.android.photostore.model.Quota;

public class GetQuotaResponse extends BaseResponse {
    @JSONField(name = "Quota")
    public Quota quota;
}
