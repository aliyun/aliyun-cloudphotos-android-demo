/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.runner;

import android.util.Log;

import com.alibaba.sdk.android.photostore.Constants;
import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.PagedRequest;
import com.alibaba.sdk.android.photostore.api.PagedResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.HttpResponse;

public class PagedRequestTask extends BaseRequestTask<PagedRequest> implements Runnable {

    String TAG = PagedRequestTask.class.getSimpleName();

    private static final int MAX_PAGE = 500;


    public PagedRequestTask(IAcsClient client, PagedRequest request, Callback callback) {
        super(client, request, callback);
    }

    @Override
    public void run() {
        while (true) {
            try {
                HttpResponse response = performRequest(client, request.build());
                if (response != null) {
                    if (response.getStatus() == 200) {
                        String body = new String(response.getContent());
                        PagedResponse resp = request.parse(body);

                        if (resp != null && resp.code.compareTo("Success") == 0) {
                            callback.onSuccess(resp);
                        } else {
                            // failed to parse or resp with error
                            callback.onFailure(response.getStatus(), resp);
                        }

                        boolean completed = false;
                        if (!resp.hasData() || request.currentPage > MAX_PAGE) {
                            completed = true;
                        }

                        if (completed) {
                            break;
                        } else {
                            request.roll();
                        }

                    } else {
                        // http not 200
                        String body = new String(response.getContent());
                        PagedResponse resp = null;
                        if (response.getStatus() == 400) {
                            resp = request.parse(body);
                        }
                        callback.onFailure(response.getStatus(), resp);
                        break;
                    }
                } else {
                    Log.w(TAG, "Failed to perform request");
                    callback.onFailure(Constants.ERROR_NETWORK, null);
                    break;
                }

            } catch (Exception e) {
                Log.w(TAG, "Failed to perform request");
                callback.onFailure(Constants.ERROR_NETWORK, null);
                break;
            }
        }
    }

}
