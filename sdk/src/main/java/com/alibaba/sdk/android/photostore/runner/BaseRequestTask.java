/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.runner;

import android.util.Log;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseRequest;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.RpcAcsRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.http.X509TrustAll;

import java.io.IOException;

import okhttp3.Call;

abstract class BaseRequestTask<T extends BaseRequest> {

    String TAG = BaseRequestTask.class.getSimpleName();

    protected T request;
    protected Callback callback;

    protected Call call;
    protected IAcsClient client;

    public BaseRequestTask(IAcsClient client, T request, Callback callback) {
        this.client = client;
        this.request = request;
        this.callback = callback;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    protected HttpResponse performRequest(IAcsClient client, RpcAcsRequest request) throws IOException {
        HttpResponse response = null;
        X509TrustAll.ignoreSSLCertificate();
        try {
            long time = System.currentTimeMillis();
            if (PhotoStoreClient.getInstance().isLogin()) {
                response = client.doAction(request);
            }
            long escape = System.currentTimeMillis() - time;
            Log.d(TAG, request.getActionName() + " response time: " + escape + "ms");
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return response;
    }
}
