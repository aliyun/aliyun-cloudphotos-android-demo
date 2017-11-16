/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.aliyuncs.RpcAcsRequest;

public abstract class BaseRequest {

    String TAG = BaseRequest.class.getSimpleName();

    public static final String REGION_CN = "cn-shanghai";

    public String storeName;

    public String stsToken;

    public String libraryId;

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void setStsToken(String stsToken) {
        this.stsToken = stsToken;
    }

    public void setLibraryId(String libraryId) {
        this.libraryId = libraryId;
    }

    public abstract RpcAcsRequest build();

    public abstract BaseResponse parse(String result);

}
