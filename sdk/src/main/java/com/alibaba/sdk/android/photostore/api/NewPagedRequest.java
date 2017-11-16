/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.aliyuncs.RpcAcsRequest;

public class NewPagedRequest extends BaseRequest {
    public int size;
    public String cursor;
    public String direction;
    public String state;

    @Override
    public RpcAcsRequest build() {
        return null;
    }

    @Override
    public NewPagedResponse parse(String result) {
        return null;
    }
}
