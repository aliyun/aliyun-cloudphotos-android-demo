/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.runner;

import android.util.Log;

import com.alibaba.sdk.android.photostore.Constants;
import com.alibaba.sdk.android.photostore.api.BaseRequest;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.HttpResponse;

public class RequestTask extends BaseRequestTask<BaseRequest> implements Runnable {

    String TAG = RequestTask.class.getSimpleName();

    public RequestTask(IAcsClient client, BaseRequest request, Callback callback) {
        super(client, request, callback);
    }

    @Override
    public void run() {
        try {
            HttpResponse response = performRequest(client, request.build());

            if (response != null) {
                if (response.getStatus() == 200) {
                    String body = new String(response.getContent());
                    BaseResponse resp = request.parse(body);

                    if (resp != null && resp.code.compareTo("Success") == 0) {
                        callback.onSuccess(resp);
                    } else {
                        // failed to parse or resp with error
                        callback.onFailure(response.getStatus(), resp);
                    }
                } else {
                    // http not 200
                    String body = new String(response.getContent());
                    Log.d(TAG, body);
                    BaseResponse resp = null;
                    if (response.getStatus() == 400) {
                        resp = request.parse(body);
                    }

                    callback.onFailure(response.getStatus(), resp);
                }
            } else {
                Log.w(TAG, "Failed to perform request");
                callback.onFailure(Constants.ERROR_NETWORK, null);
            }

        } catch (Exception e) {
            Log.w(TAG, "Failed to perform request");
            callback.onFailure(Constants.ERROR_NETWORK, null);
        }
    }

}
