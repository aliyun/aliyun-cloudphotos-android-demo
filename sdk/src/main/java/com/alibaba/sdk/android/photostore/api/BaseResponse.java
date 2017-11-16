/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;

public class BaseResponse {
    @JSONField(name = "RequestId")
    public String requestId;
    @JSONField(name = "Code")
    public String code;
    @JSONField(name = "Message")
    public String msg;
    @JSONField(name = "Action")
    public String action;

    public BaseResponse() {}

    public BaseResponse(String code, String msg, String action) {
        this.code = code;
        this.msg = msg;
        this.action = action;
    }
}
