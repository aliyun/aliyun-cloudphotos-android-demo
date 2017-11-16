/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.runner;

import com.alibaba.sdk.android.photostore.api.BaseResponse;

public interface Callback<T extends BaseResponse> {

    void onSuccess(T response);

    /**
     *
     * @param code http code
     * @param response response
     */
    void onFailure(int code, BaseResponse response);

}
