/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.SearchPhotosResponse;
import com.alibaba.sdk.android.photostore.model.Photo;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.MyApplication;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnSearchEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;

import java.util.ArrayList;
import java.util.List;

public class SearchController {
    String TAG = SearchController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static SearchController sInstance;

    public List<MyPhoto> resultPhotos;

    public static SearchController getInstance() {
        if (sInstance == null) {
            synchronized (SearchController.class) {
                sInstance = new SearchController();
            }
        }
        return sInstance;
    }

    private SearchController() {
        resultPhotos = new ArrayList<>();
    }

    public void search(String keyword) {
        resultPhotos.clear();
        client.searchPhotos(keyword, 1, 500, new Callback<SearchPhotosResponse>() {
            @Override
            public void onSuccess(SearchPhotosResponse response) {
                List<MyPhoto> photos = new ArrayList<>();
                for (Photo p : response.data) {
                    photos.add(new MyPhoto(p));
                }
                resultPhotos.addAll(photos);
                BusProvider.getInstance().post(new OnSearchEvent(0, "", ContentType.SEARCH_PHOTO));
            }

            @Override
            public void onFailure(final int code, final BaseResponse response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int c = code;
                        String s = "";
                        if (response != null) {
                            s = response.msg;
                            if (response.code.compareTo("InvalidSecurityToken.Expired") == 0 && !MyApplication.tokenExpired) {
                                MyApplication.tokenExpired = true;
                                BusProvider.getInstance().post(new OnLogoutEvent(true));
                            }
                        }
                        BusProvider.getInstance().post(new OnSearchEvent(c, s, ContentType.SEARCH_PHOTO));
                    }
                });
            }
        });
    }

}
