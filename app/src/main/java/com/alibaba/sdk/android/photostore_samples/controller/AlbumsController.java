/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.AddAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.CreateAlbumResponse;
import com.alibaba.sdk.android.photostore.api.DeleteAlbumsResponse;
import com.alibaba.sdk.android.photostore.api.ListAlbumsResponse;
import com.alibaba.sdk.android.photostore.api.MoveAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.RemoveAlbumPhotosResponse;
import com.alibaba.sdk.android.photostore.api.RenameAlbumResponse;
import com.alibaba.sdk.android.photostore.api.SetAlbumCoverResponse;
import com.alibaba.sdk.android.photostore.model.Album;
import com.alibaba.sdk.android.photostore.model.PhotoState;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.MyApplication;
import com.alibaba.sdk.android.photostore_samples.event.OnGetAlbumPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetAlbumsEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbum;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbumPhoto;

import java.util.ArrayList;
import java.util.List;

public class AlbumsController {
    String TAG = AlbumsController.class.getSimpleName();

    private PhotoStoreClient client = PhotoStoreClient.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static AlbumsController sInstance;

    public static AlbumsController getInstance() {
        if (sInstance == null) {
            synchronized (AlbumsController.class) {
                sInstance = new AlbumsController();
            }
        }
        return sInstance;
    }

    public AlbumsController() {
        BusProvider.getInstance().register(this);
    }

    boolean fetching = false;

    public void fetchAllAlbums(final boolean withPhotos) {
        if (fetching) {
            return;
        }
        fetching = true;
        MyAlbum.getMaxUploadedAt(new DatabaseCallback<MyAlbum>() {
            @Override
            public void onCompleted(int code, final List<MyAlbum> data) {
                long maxUpdatedAt = 0;
                String state = "active";
                if (data != null && data.size() > 0) {
                    maxUpdatedAt = data.get(0).mtime;
                    state = "all";
                }
                String cursor = String.valueOf(maxUpdatedAt);
                client.listAlbums(50, cursor, "forward", state, new Callback<ListAlbumsResponse>() {
                    @Override
                    public void onSuccess(final ListAlbumsResponse response) {
                        if (response.hasData()) {
                            MyAlbum.update(response.data, new DatabaseCallback<Long>() {
                                @Override
                                public void onCompleted(int code, List<Long> data) {
                                    if (withPhotos) {
                                        BusProvider.getInstance().post(new OnGetAlbumsEvent(0, "", data));
                                    } else {
                                        BusProvider.getInstance().post(new OnGetAlbumsEvent(0, "", null));
                                    }
                                }
                            });
                        } else {
                            fetching = false;
                            BusProvider.getInstance().post(new OnGetAlbumsEvent(0, "", null));
                        }
                    }

                    @Override
                    public void onFailure(final int code, final BaseResponse response) {
                        fetching = false;
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
                                BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
                            }
                        });
                    }
                });
            }
        });
    }

    public void createAlbum(final String name) {
        client.createAlbum(name, new Callback<CreateAlbumResponse>() {
            @Override
            public void onSuccess(CreateAlbumResponse response) {
                List<Album> list = new ArrayList<>();
                list.add(response.album);
                MyAlbum.update(list, new DatabaseCallback<Long>() {
                    @Override
                    public void onCompleted(int code, List<Long> data) {
                        BusProvider.getInstance().post(new OnGetAlbumsEvent(0, "", data));
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
                        BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
                    }
                });
            }
        });
    }

    public void renameAlbum(final long albumId, final String name) {
        client.renameAlbum(albumId, name, new Callback<RenameAlbumResponse>() {
            @Override
            public void onSuccess(RenameAlbumResponse response) {
                MyAlbum.updateName(albumId, name, new DatabaseCallback<Long>() {
                    @Override
                    public void onCompleted(int code, List<Long> data) {
                        BusProvider.getInstance().post(new OnGetAlbumsEvent(0, "", data));
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
                        BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
                    }
                });
            }
        });
    }

    public void deleteAlbums(final List<MyAlbum> albums) {
        List<Long> albumIds = new ArrayList<>();
        for (MyAlbum o : albums) {
            albumIds.add(o.id);
        }
        client.deleteAlbums(albumIds, new Callback<DeleteAlbumsResponse>() {
            @Override
            public void onSuccess(DeleteAlbumsResponse response) {
                List<Long> ids = new ArrayList<>();
                for (DeleteAlbumsResponse.CommonInfo info : response.data) {
                    ids.add(info.id);
                }
                MyAlbum.deleteByIds(ids, new DatabaseCallback<Void>() {
                    @Override
                    public void onCompleted(int code, List<Void> data) {
                        BusProvider.getInstance().post(new OnGetAlbumsEvent(0, "", null));
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
                        BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
                    }
                });
            }
        });
    }

    public void addPhotos(final long albumId, final List<Long> photoIds) {
        client.addAlbumPhotos(albumId, photoIds, new Callback<AddAlbumPhotosResponse>() {
            @Override
            public void onSuccess(AddAlbumPhotosResponse response) {
                List<PhotoState> ids = new ArrayList<>();
                for (AddAlbumPhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0) {
                        PhotoState ps = new PhotoState();
                        ps.id = info.id;
                        ps.state = "active";
                        ids.add(ps);
                    }
                }
                MyAlbumPhoto.update(albumId, ids, new DatabaseCallback<MyAlbumPhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyAlbumPhoto> data) {
                        BusProvider.getInstance().post(new OnGetAlbumPhotosEvent(0, "", null));
                    }
                });
                fetchAllAlbums(false);
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
                BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
            }
        });
    }

    public void removePhotos(final long albumId, final List<Long> list) {
        client.removeAlbumPhotos(albumId, list, new Callback<RemoveAlbumPhotosResponse>() {
            @Override
            public void onSuccess(RemoveAlbumPhotosResponse response) {
                List<Long> ids = new ArrayList<>();
                for (RemoveAlbumPhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0) {
                        ids.add(info.id);
                    }
                }
                MyAlbumPhoto.delete(albumId, ids, new DatabaseCallback<MyAlbumPhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyAlbumPhoto> data) {
                        BusProvider.getInstance().post(new OnGetAlbumPhotosEvent(0, "", null));
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
                BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
            }
        });
    }

    public void movePhotos(final long albumId, final List<Long> photoIds, final long targetId) {
        client.moveAlbumPhotos(albumId, photoIds, targetId, new Callback<MoveAlbumPhotosResponse>() {
            @Override
            public void onSuccess(MoveAlbumPhotosResponse response) {
                List<Long> ids = new ArrayList<>();
                for (MoveAlbumPhotosResponse.CommonInfo info : response.data) {
                    if (info.code.compareTo("Success") == 0) {
                        ids.add(info.id);
                    }
                }
                MyAlbumPhoto.delete(albumId, ids, new DatabaseCallback<MyAlbumPhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyAlbumPhoto> data) {
                        BusProvider.getInstance().post(new OnGetAlbumPhotosEvent(0, "", albumId));
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
                BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
            }
        });
    }

    public void setCover(final long albumId, final long photoId, final boolean isVideo) {
        client.setAlbumCover(albumId, photoId, new Callback<SetAlbumCoverResponse>() {
            @Override
            public void onSuccess(SetAlbumCoverResponse response) {
                MyAlbum.updateCover(albumId, photoId, isVideo, new DatabaseCallback<Long>() {
                    @Override
                    public void onCompleted(int code, List<Long> data) {
                        BusProvider.getInstance().post(new OnGetAlbumsEvent(0, "", null));
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
                BusProvider.getInstance().post(new OnGetAlbumsEvent(c, s, null));
            }
        });
    }
}
