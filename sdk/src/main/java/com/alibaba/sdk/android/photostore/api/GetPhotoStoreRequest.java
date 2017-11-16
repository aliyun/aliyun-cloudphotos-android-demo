/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.RpcAcsRequest;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;

public class GetPhotoStoreRequest extends BaseRequest {
    @Override
    public RpcAcsRequest build() {
        com.aliyuncs.cloudphoto.model.v20170711.GetPhotoStoreRequest request = new com.aliyuncs.cloudphoto.model.v20170711.GetPhotoStoreRequest();
        request.setProtocol(ProtocolType.HTTPS); //指定访问协议
        request.setAcceptFormat(FormatType.JSON); //指定api返回格式
        request.setMethod(MethodType.POST); //指定请求方法
        request.setRegionId(REGION_CN);//指定要访问的Region,仅对当前请求生效，不改变client的默认设置。
        if (!stsToken.isEmpty())
            request.setSecurityToken(stsToken);
        request.setStoreName(storeName);
        request.setActionName("GetPhotoStore");
        return request;
    }

    @Override
    public BaseResponse parse(String result) {
        return JSON.parseObject(result, GetPhotoStoreResponse.class);
    }
}
