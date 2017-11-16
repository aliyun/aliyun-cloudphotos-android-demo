/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.DeleteFacesResponse;
import com.alibaba.sdk.android.photostore.api.FaceSetMeResponse;
import com.alibaba.sdk.android.photostore.api.ListFacesResponse;
import com.alibaba.sdk.android.photostore.api.MergeFacesResponse;
import com.alibaba.sdk.android.photostore.api.MoveFacePhotosResponse;
import com.alibaba.sdk.android.photostore.api.RemoveFacePhotosResponse;
import com.alibaba.sdk.android.photostore.api.RenameFaceResponse;
import com.alibaba.sdk.android.photostore.api.SetFaceCoverResponse;
import com.alibaba.sdk.android.photostore.model.Face;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.MyApplication;
import com.alibaba.sdk.android.photostore_samples.event.OnGetFacesEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetFacesPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyCursor;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.alibaba.sdk.android.photostore_samples.model.MyFacePhoto;

import java.util.ArrayList;
import java.util.List;

public class FacesController {
    String TAG = FacesController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static FacesController sInstance;

    public static FacesController getInstance() {
        if (sInstance == null) {
            synchronized (FacesController.class) {
                sInstance = new FacesController();
            }
        }
        return sInstance;
    }

    public FacesController() {
        BusProvider.getInstance().register(this);
    }

    public void fetchFacesByName() {
        MyCursor.getNextCursor("facebyname+forward", new DatabaseCallback<MyCursor>() {
            @Override
            public void onCompleted(int code, List<MyCursor> data) {
                String cursor = "0";
                String state = "active";
                String direction = "forward";
                if (data != null && data.size() > 0) {
                    state = "all";
                }
                client.listFace(50, cursor, direction, state, true, new Callback<ListFacesResponse>() {
                    @Override
                    public void onSuccess(ListFacesResponse response) {
                        MyFace.update(response.data, new DatabaseCallback() {
                            @Override
                            public void onCompleted(int code, List data) {
                                if (!response.nextCursor.equalsIgnoreCase("EOF")) {
                                    MyCursor.update("facebyname+forward", response.nextCursor, new DatabaseCallback() {
                                        @Override
                                        public void onCompleted(int code, List data) {
                                            List<Long> l = new ArrayList<>();
                                            for (Face f : response.data) {
                                                l.add(f.id);
                                            }
                                            BusProvider.getInstance().post(new OnGetFacesEvent(0, "", l));
                                        }
                                    });
                                }

                                fetchFaces();
                            }
                        });
                    }

                    @Override
                    public void onFailure(int code, BaseResponse response) {
                        fetchFaces();
                    }
                });
            }
        });
    }

    public void fetchFaces() {
        MyCursor.getNextCursor("face+forward", new DatabaseCallback<MyCursor>() {
            @Override
            public void onCompleted(int code, List<MyCursor> data) {
                String direction = "forward";
                String state = "active";
                String cursor = String.valueOf(System.currentTimeMillis());
                if (data != null && data.size() > 0) {
                    state = "all";
                    cursor = data.get(0).cursor;
                } else {
                    direction = "backward";
                }

                final String dir = direction;
                final String currentCusor = cursor;
                client.listFace(50, cursor, direction, state, false, new Callback<ListFacesResponse>() {
                    @Override
                    public void onSuccess(ListFacesResponse response) {
                        if (response.hasData()) {
                            MyFace.update(response.data, new DatabaseCallback() {
                                @Override
                                public void onCompleted(int code, List data) {
                                    if (dir.equalsIgnoreCase("backward")) {
                                        MyCursor.update("face+forward", currentCusor, null);
                                        MyCursor.update("face+backward", response.nextCursor, new DatabaseCallback() {
                                            @Override
                                            public void onCompleted(int code, List data) {
                                                List<Long> l = new ArrayList<>();
                                                for (Face f : response.data) {
                                                    l.add(f.id);
                                                }
                                                BusProvider.getInstance().post(new OnGetFacesEvent(0, "", l));
                                            }
                                        });
                                    }
                                    else if (!response.nextCursor.equalsIgnoreCase("EOF")) {
                                        MyCursor.update("face+forward", response.nextCursor, new DatabaseCallback() {
                                            @Override
                                            public void onCompleted(int code, List data) {
                                                List<Long> l = new ArrayList<>();
                                                for (Face f : response.data) {
                                                    l.add(f.id);
                                                }
                                                BusProvider.getInstance().post(new OnGetFacesEvent(0, "", l));
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
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
                                BusProvider.getInstance().post(new OnGetFacesEvent(c, s, null));
                            }
                        });
                    }
                });
            }
        });
    }

    public void moreFaces() {
        MyCursor.getNextCursor("face+backward", new DatabaseCallback<MyCursor>() {
            @Override
            public void onCompleted(int code, List<MyCursor> data) {
                if (data != null && data.size() > 0) {
                    String cursor = data.get(0).cursor;
                    if (cursor.equalsIgnoreCase("EOF")) {
                        BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
                    } else {
                        client.listFace(50, cursor, "backward", "active", false, new Callback<ListFacesResponse>() {
                            @Override
                            public void onSuccess(ListFacesResponse response) {
                                if (response.hasData()) {
                                    MyFace.update(response.data, new DatabaseCallback() {
                                        @Override
                                        public void onCompleted(int code, List data) {
                                            MyCursor.update("face+backward", response.nextCursor, new DatabaseCallback() {
                                                @Override
                                                public void onCompleted(int code, List data) {
                                                    List<Long> l = new ArrayList<>();
                                                    for (Face f : response.data) {
                                                        l.add(f.id);
                                                    }
                                                    BusProvider.getInstance().post(new OnGetFacesEvent(0, "", l));
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
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
                                        BusProvider.getInstance().post(new OnGetFacesEvent(c, s, null));
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

    public void setMe(final long faceId) {
        client.faceSetMe(faceId, new Callback<FaceSetMeResponse>() {
            @Override
            public void onSuccess(FaceSetMeResponse response) {
                MyFace.changeMe(faceId, new DatabaseCallback() {
                    @Override
                    public void onCompleted(int code, List data) {
                        BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
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
                        BusProvider.getInstance().post(new OnGetFacesEvent(c, s, null));
                    }
                });
            }
        });
    }

    public void rename(final long id, final String name) {
        client.renameFace(id, name, new Callback<RenameFaceResponse>() {
            @Override
            public void onSuccess(RenameFaceResponse response) {
                MyFace.updateName(id, name, new DatabaseCallback() {
                    @Override
                    public void onCompleted(int code, List data) {
                        BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
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

    public void deleteFaces(final List<MyFace> faces) {
        List<Long> faceIds = new ArrayList<>();
        for (MyFace f : faces) {
            faceIds.add(f.id);
        }
        client.deleteFaces(faceIds, new Callback<DeleteFacesResponse>() {
            @Override
            public void onSuccess(DeleteFacesResponse response) {
                MyFace.deleteByIds(faceIds, new DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(int code, List<Void> data) {
                        BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
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

    public void mergeFaces(final Long targetFaceId, final List<Long> faceIds) {
        client.mergeFaces(targetFaceId, faceIds, new Callback<MergeFacesResponse>() {
            @Override
            public void onSuccess(MergeFacesResponse response) {
                MyFace.deleteByIds(faceIds, new DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(int code, List<Void> data) {
                        BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
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
            }
        });
    }

    public void removePhotos(final long faceId, final List<MyPhoto> list) {
        List<Long> photoIds = new ArrayList<>();
        for (MyPhoto o : list) {
            photoIds.add(o.id);
        }

        client.removeFacePhotos(faceId, photoIds, new Callback<RemoveFacePhotosResponse>() {
            @Override
            public void onSuccess(RemoveFacePhotosResponse response) {
                List<Long> ids = new ArrayList<>();
                for (RemoveFacePhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0) {
                        ids.add(info.id);
                    }
                }
                MyFacePhoto.delete(faceId, ids, new DatabaseCallback<MyFacePhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyFacePhoto> data) {
                        BusProvider.getInstance().post(new OnGetFacesPhotosEvent(0, "", faceId));
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

    public void movePhotos(final long faceId, final List<Long> photoIds, final long targetId) {
        client.moveFacePhotos(faceId, photoIds, targetId, new Callback<MoveFacePhotosResponse>() {
            @Override
            public void onSuccess(MoveFacePhotosResponse response) {
                List<Long> ids = new ArrayList<>();
                for (MoveFacePhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0) {
                        ids.add(info.id);
                    }
                }
                MyFacePhoto.delete(faceId, ids, new DatabaseCallback<MyFacePhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyFacePhoto> data) {
                        BusProvider.getInstance().post(new OnGetFacesPhotosEvent(0, "", faceId));
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

    public void setCover(final long faceId, final long photoId) {
        client.setFaceCover(faceId, photoId, new Callback<SetFaceCoverResponse>() {
            @Override
            public void onSuccess(SetFaceCoverResponse response) {
                MyFace.updateCover(faceId, photoId, new DatabaseCallback<Long>() {
                    @Override
                    public void onCompleted(int code, List<Long> data) {
                        BusProvider.getInstance().post(new OnGetFacesEvent(0, "", null));
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
