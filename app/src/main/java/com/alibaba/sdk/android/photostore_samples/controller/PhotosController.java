/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.DeletePhotosResponse;
import com.alibaba.sdk.android.photostore.api.InactivatePhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListFacePhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListMomentPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ListTagPhotosResponse;
import com.alibaba.sdk.android.photostore.api.ReactivatePhotosResponse;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.MyApplication;
import com.alibaba.sdk.android.photostore_samples.event.OnGetAlbumPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetFacesPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetInactivePhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetMomentsEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetMomentsPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetTagsPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnRemoveInactivePhotosEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbum;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbumPhoto;
import com.alibaba.sdk.android.photostore_samples.model.MyCursor;
import com.alibaba.sdk.android.photostore_samples.model.MyMoment;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.alibaba.sdk.android.photostore_samples.model.MyFacePhoto;
import com.alibaba.sdk.android.photostore_samples.model.MyMomentPhoto;
import com.alibaba.sdk.android.photostore_samples.model.MyTag;
import com.alibaba.sdk.android.photostore_samples.model.MyTagPhoto;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class PhotosController {

    String TAG = PhotosController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static PhotosController sInstance;

    private List<Long> photos = null;

    public static PhotosController getInstance() {
        if (sInstance == null) {
            synchronized (PhotosController.class) {
                sInstance = new PhotosController();
            }
        }
        return sInstance;
    }

    public PhotosController() {
        BusProvider.getInstance().register(this);
    }

    private void updateMomentsPhotos(List<Long> momentIds) {
        for (Long id : momentIds) {
            updateMomentPhotos(id);
        }
    }

    public void updatePhotos() {
        MyPhoto.getMaxUpdatedAt(new DatabaseCallback<MyPhoto>() {
            @Override
            public void onCompleted(int code, List<MyPhoto> data) {
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
                String cursor = String.valueOf(maxUpdatedAt);
                final String dir = direction;
                client.listPhoto(100, cursor, direction, state, new Callback<ListPhotosResponse>() {
                    @Override
                    public void onSuccess(ListPhotosResponse response) {
                        Log.d(TAG, response.code);
                        if (response.hasData()) {
                            MyPhoto.updatePhotos(response.data, new DatabaseCallback() {
                                @Override
                                public void onCompleted(int code, List data) {
                                    if (dir.equalsIgnoreCase("backward")) {
                                        MyCursor.update("photo+backward", response.nextCursor, new DatabaseCallback() {
                                            @Override
                                            public void onCompleted(int code, List data) {
                                                BusProvider.getInstance().post(new OnGetPhotosEvent(0, ""));
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            BusProvider.getInstance().post(new OnGetPhotosEvent(0, ""));
                        }
                    }

                    @Override
                    public void onFailure(final int code, final BaseResponse response) {
                        Log.d(TAG, String.valueOf(code));
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

                                BusProvider.getInstance().post(new OnGetPhotosEvent(c, s));
                            }
                        });
                    }
                });
            }
        });
    }

    public void morePhotos() {
        MyCursor.getNextCursor("photo+backward", new DatabaseCallback<MyCursor>() {
            @Override
            public void onCompleted(int code, List<MyCursor> data) {
                if (data != null && data.size() > 0) {
                    String cursor = data.get(0).cursor;
                    if (cursor.equalsIgnoreCase("EOF")) {
                        BusProvider.getInstance().post(new OnGetPhotosEvent(0, ""));
                    }
                    else {
                        client.listPhoto(100, cursor, "backward", "active", new Callback<ListPhotosResponse>() {
                            @Override
                            public void onSuccess(ListPhotosResponse response) {
                                Log.d(TAG, response.code);
                                if (response.hasData()) {
                                    MyPhoto.updatePhotos(response.data, new DatabaseCallback() {
                                        @Override
                                        public void onCompleted(int code, List data) {
                                            MyCursor.update("photo+backward", response.nextCursor, new DatabaseCallback() {
                                                @Override
                                                public void onCompleted(int code, List data) {
                                                    BusProvider.getInstance().post(new OnGetPhotosEvent(0, ""));
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    BusProvider.getInstance().post(new OnGetPhotosEvent(0, ""));
                                }
                            }

                            @Override
                            public void onFailure(final int code, final BaseResponse response) {
                                Log.d(TAG, String.valueOf(code));
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

                                        BusProvider.getInstance().post(new OnGetPhotosEvent(c, s));
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

    public void updateInactivePhotos() {
        long now = System.currentTimeMillis();
        String cursor = String.valueOf(now);
        client.listPhoto(100, cursor, "backward", "inactive", new Callback<ListPhotosResponse>() {
            @Override
            public void onSuccess(ListPhotosResponse response) {
                if (response.data.size() > 0) {
                    BusProvider.getInstance().post(new OnGetInactivePhotosEvent(0, "", response.data));
                }
            }

            @Override
            public void onFailure(int code, BaseResponse response) {
                Log.d(TAG, String.valueOf(code));
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
                    }
                });
            }
        });
    }

    public void updateMomentPhotos(final long momentId) {
        MyMoment.getById(momentId, new DatabaseCallback<MyMoment>() {
            @Override
            public void onCompleted(int code, List<MyMoment> data) {
                String cursor = "0";
                String state = "active";
                if (data != null && data.size() > 0) {
                    cursor = data.get(0).nextCursor;
                    state = "all";
                }

                client.listMomentPhotos(momentId, 100, cursor, "forward", state, new Callback<ListMomentPhotosResponse>() {
                    @Override
                    public void onSuccess(ListMomentPhotosResponse response) {
                        if (response.hasData()) {
                            MyMomentPhoto.update(momentId, response.data, response.nextCursor, new DatabaseCallback() {
                                @Override
                                public void onCompleted(int code, List data) {
                                    List<Long> l = new ArrayList<>();
                                    l.add(momentId);
                                    BusProvider.getInstance().post(new OnGetMomentsPhotosEvent(0, "", l));
                                }
                            });
                        } else {
                            BusProvider.getInstance().post(new OnGetMomentsPhotosEvent(0, "", null));
                        }
                    }

                    @Override
                    public void onFailure(int code, BaseResponse response) {
                        Log.d(TAG, String.valueOf(code));
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

                                BusProvider.getInstance().post(new OnGetPhotosEvent(c, s));
                            }
                        });
                    }
                });
            }
        });
    }

    public void updateAlbumPhotos(final long albumId) {
        MyAlbum.getById(albumId, new DatabaseCallback<MyAlbum>() {
            @Override
            public void onCompleted(int code, List<MyAlbum> data) {
                // TODO:mengzheng
                String cursor = String.valueOf(0);
                String state = "active";
                String direction = "forward";
                if (data != null && data.size() > 0) {
                    cursor = data.get(0).nextCursor;
                    state = "all";
                }

                client.listAlbumPhotos(albumId, 100, cursor, direction, state, new Callback<ListAlbumPhotosResponse>() {
                    @Override
                    public void onSuccess(ListAlbumPhotosResponse response) {
                        if (response.hasData()) {
                            MyAlbumPhoto.update(albumId, response.data, response.nextCursor, new DatabaseCallback() {
                                @Override
                                public void onCompleted(int code, List data) {
                                    BusProvider.getInstance().post(new OnGetAlbumPhotosEvent(0, "", albumId));
                                }
                            });
                        } else {
                            BusProvider.getInstance().post(new OnGetAlbumPhotosEvent(0, "", -1L));
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
                                BusProvider.getInstance().post(new OnGetAlbumPhotosEvent(c, s, -1L));
                            }
                        });
                    }
                });
            }
        });
    }

    public void updateFacePhotos(final long faceId) {
        MyFace.getById(faceId, new DatabaseCallback<MyFace>() {
            @Override
            public void onCompleted(int code, List<MyFace> data) {
                String cursor = "0";
                String state = "active";
                String direction = "forward";
                if (data != null && data.size() > 0) {
                    cursor = data.get(0).nextCursor;
                    state = "all";
                }

                client.listFacePhotos(faceId, 100, cursor, direction, state, new Callback<ListFacePhotosResponse>() {
                    @Override
                    public void onSuccess(ListFacePhotosResponse response) {
                        if (response.hasData()) {
                            MyFacePhoto.update(faceId, response.data, response.nextCursor, new DatabaseCallback() {
                                @Override
                                public void onCompleted(int code, List data) {
                                    BusProvider.getInstance().post(new OnGetFacesPhotosEvent(0, "", faceId));
                                }
                            });
                        } else {
                            BusProvider.getInstance().post(new OnGetFacesPhotosEvent(0, "", -1L));
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
                                BusProvider.getInstance().post(new OnGetFacesPhotosEvent(c, s, -1L));
                            }
                        });
                    }
                });
            }
        });
    }

    public void updateTagPhotos(final long tagId) {
        MyTag.getById(tagId, new DatabaseCallback<MyTag>() {
            @Override
            public void onCompleted(int code, List<MyTag> data) {
                String cursor = "0";
                String state = "active";
                if (data != null && data.size() > 0) {
                    cursor = data.get(0).nextCursor;
                    state = "all";
                }

                client.listTagPhotos(tagId, 100, cursor, state, new Callback<ListTagPhotosResponse>() {
                    @Override
                    public void onSuccess(ListTagPhotosResponse response) {
                        if (response.hasData()) {
                            MyTagPhoto.update(tagId, response.data, response.nextCursor, new DatabaseCallback() {
                                @Override
                                public void onCompleted(int code, List data) {
                                    BusProvider.getInstance().post(new OnGetTagsPhotosEvent(0, "", tagId));
                                }
                            });
                        } else {
                            BusProvider.getInstance().post(new OnGetTagsPhotosEvent(0, "", -1L));
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
                                BusProvider.getInstance().post(new OnGetTagsPhotosEvent(c, s, -1L));
                            }
                        });
                    }
                });
            }
        });
    }

    public void inactiveCloudPhotos(final List<Long> list) {
        client.inactivePhotos(list, new Callback<InactivatePhotosResponse>() {
            @Override
            public void onSuccess(InactivatePhotosResponse response) {
                List<Long> ids = new ArrayList<>();
                for ( InactivatePhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0)
                        ids.add(info.id);
                }
                MyPhoto.deleteByIds(ids, new DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(int code, List<Void> data) {
                        BusProvider.getInstance().post(new OnGetPhotosEvent(0, ""));
                    }
                });
                MyMomentPhoto.deleteByPhotos(ids, new DatabaseCallback<MyMomentPhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyMomentPhoto> data) {

                    }
                });
                MyAlbumPhoto.deleteByPhotos(ids, new DatabaseCallback<MyAlbumPhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyAlbumPhoto> data) {

                    }
                });
                MyFacePhoto.deleteByPhotos(ids, new DatabaseCallback<MyFacePhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyFacePhoto> data) {

                    }
                });
                MyTagPhoto.deleteByPhotos(ids, new DatabaseCallback<MyTagPhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyTagPhoto> data) {

                    }
                });
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

                        BusProvider.getInstance().post(new OnGetPhotosEvent(c, s));
                    }
                });
            }
        });
    }

    public void deleteCloudPhotos(final List<MyPhoto> list) {
        List<Long> params = new ArrayList<>();
        for (MyPhoto o : list) {
            params.add(o.id);
        }

        client.deletePhotos(params, new Callback<DeletePhotosResponse>() {
            @Override
            public void onSuccess(DeletePhotosResponse response) {
                List<Long> ids = new ArrayList<>();
                for ( DeletePhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0)
                        ids.add(info.id);
                }
                BusProvider.getInstance().post(new OnRemoveInactivePhotosEvent(0, "", ids));
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
                    }
                });
            }
        });
    }

    public void reactiveCloudPhotos(final List<MyPhoto> list) {
        List<Long> params = new ArrayList<>();
        for (MyPhoto o : list) {
            params.add(o.id);
        }

        client.reactivePhotos(params, new Callback<ReactivatePhotosResponse>() {
            @Override
            public void onSuccess(ReactivatePhotosResponse response) {
                List<Long> ids = new ArrayList<>();
                for ( ReactivatePhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0)
                        ids.add(info.id);
                }
                BusProvider.getInstance().post(new OnRemoveInactivePhotosEvent(0, "", ids));
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
                    }
                });
            }
        });
    }

    @Subscribe
    public void onGetMoments(OnGetMomentsEvent event) {
        if (event.moments != null) {
            updateMomentsPhotos(event.moments);
        }
    }

    public List<Long> getPhotoList() {
        return photos;
    }

    public void setPhotoList(List<Long> list) {
        photos = list;
    }

}
