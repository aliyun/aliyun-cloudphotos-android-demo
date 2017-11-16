/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.ListTagsResponse;
import com.alibaba.sdk.android.photostore.model.Tag;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.MyApplication;
import com.alibaba.sdk.android.photostore_samples.event.OnGetFacesEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetTagsEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyTag;

import java.util.ArrayList;
import java.util.List;

public class TagsController {
    String TAG = TagsController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static TagsController sInstance;

    public static TagsController getInstance() {
        if (sInstance == null) {
            synchronized (TagsController.class) {
                sInstance = new TagsController();
            }
        }
        return sInstance;
    }

    public TagsController() {
        BusProvider.getInstance().register(this);
    }

    public void fetchTags() {
        client.listTag(new Callback<ListTagsResponse>() {
            @Override
            public void onSuccess(ListTagsResponse response) {
                MyTag.update(response.data, new DatabaseCallback() {
                    @Override
                    public void onCompleted(int code, List data) {
                        List<Long> l = new ArrayList<>();
                        for (Tag f : response.data) {
                            l.add(f.id);
                        }
                        BusProvider.getInstance().post(new OnGetTagsEvent(0, "", l));
                    }
                });
            }

            @Override
            public void onFailure(int code, BaseResponse response) {
                int c = code;
                String s = "";
                if (response != null) {
                    s = response.msg;
                    if (response.code.compareTo("InvalidSecurityToken.Expired") == 0 && !MyApplication.tokenExpired) {
                        MyApplication.tokenExpired = true;
                        BusProvider.getInstance().post(new OnLogoutEvent(true));
                    }
                }
                BusProvider.getInstance().post(new OnGetFacesEvent(c, s, null));
            }
        });
    }
}
