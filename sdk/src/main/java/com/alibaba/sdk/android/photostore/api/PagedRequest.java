/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.aliyuncs.RpcAcsRequest;

public class PagedRequest extends BaseRequest {

    public int currentPage;
    public int sizePerPage;

    @Override
    public RpcAcsRequest build() {
        return null;
    }

    @Override
    public PagedResponse parse(String result) {
        return null;
    }

    public void roll() {
        currentPage++;
    }
}
