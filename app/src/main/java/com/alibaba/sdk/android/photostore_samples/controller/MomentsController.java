/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.ListMomentsResponse;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.MyApplication;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.event.OnGetMomentsEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyCursor;
import com.alibaba.sdk.android.photostore_samples.model.MyMoment;
import java.util.List;

public class MomentsController {

    String TAG = MomentsController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static MomentsController sInstance;

    public static MomentsController getInstance() {
        if (sInstance == null) {
            synchronized (MomentsController.class) {
                sInstance = new MomentsController();
            }
        }
        return sInstance;
    }

    public MomentsController() {
        BusProvider.getInstance().register(this);
    }

    public void updateMoments(final boolean withPhotos) {
        MyMoment.getMaxUploadedAt(new DatabaseCallback<MyMoment>() {
            @Override
            public void onCompleted(int code, final List<MyMoment> data) {
                long maxUpdatedAt = System.currentTimeMillis();
                String state = "active";
                String direction = "forward";
                if (data != null && data.size() > 0) {
                    maxUpdatedAt = data.get(0).mtime;
                    state = "all";
                }
                else {
                    direction = "backward";
                }
                final String dir = direction;
                String cursor = String.valueOf(maxUpdatedAt);
                client.listMoment(50, cursor, direction, state, new Callback<ListMomentsResponse>() {
                    @Override
                    public void onSuccess(ListMomentsResponse response) {
                        Log.d(TAG, response.msg);
                        if (response.hasData()) {
                            MyMoment.update(response.data, new DatabaseCallback<Long>() {
                                @Override
                                public void onCompleted(int code, List<Long> data) {
                                    if (dir.equalsIgnoreCase("backward")) {
                                        MyCursor.update("moment+backward", response.nextCursor, new DatabaseCallback() {
                                            @Override
                                            public void onCompleted(int code, List l) {
                                                if (withPhotos) {
                                                    BusProvider.getInstance().post(new OnGetMomentsEvent(0, "", data));
                                                } else {
                                                    BusProvider.getInstance().post(new OnGetMomentsEvent(0, "", null));
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            BusProvider.getInstance().post(new OnGetMomentsEvent(0, "", null));
                        }
                    }

                    @Override
                    public void onFailure(int code, BaseResponse response) {
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

                                BusProvider.getInstance().post(new OnGetMomentsEvent(c, s, null));
                            }
                        });
                    }
                });
            }
        });

    }

    public void moreMoments(final boolean withPhotos) {
        MyCursor.getNextCursor("moment+backward", new DatabaseCallback<MyCursor>() {
            @Override
            public void onCompleted(int code, List<MyCursor> data) {
                if (data != null && data.size() > 0) {
                    String cursor = data.get(0).cursor;
                    if (!cursor.equalsIgnoreCase("EOF")) {
                        client.listMoment(50, cursor, "backward", "active", new Callback<ListMomentsResponse>() {
                            @Override
                            public void onSuccess(ListMomentsResponse response) {
                                Log.d(TAG, response.msg);
                                if (response.hasData()) {
                                    MyMoment.update(response.data, new DatabaseCallback<Long>() {
                                        @Override
                                        public void onCompleted(int code, List<Long> data) {
                                            MyCursor.update("photo+backward", response.nextCursor, new DatabaseCallback() {
                                                @Override
                                                public void onCompleted(int code, List l) {
                                                    if (withPhotos) {
                                                        BusProvider.getInstance().post(new OnGetMomentsEvent(0, "", data));
                                                    } else {
                                                        BusProvider.getInstance().post(new OnGetMomentsEvent(0, "", null));
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    BusProvider.getInstance().post(new OnGetMomentsEvent(0, "", null));
                                }
                            }

                            @Override
                            public void onFailure(int code, BaseResponse response) {
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

                                        BusProvider.getInstance().post(new OnGetMomentsEvent(c, s, null));
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

}

