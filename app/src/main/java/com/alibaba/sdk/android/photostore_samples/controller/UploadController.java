/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.util.Log;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotosByMd5sResponse;
import com.alibaba.sdk.android.photostore.api.TransferDelegate;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.event.OnUploadStateChangedEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.UploadedPhoto;
import com.alibaba.sdk.android.photostore_samples.util.DataRunner;
import com.alibaba.sdk.android.photostore_samples.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UploadController {

    private static final String TAG = UploadController.class.getSimpleName();

    private static UploadController sInstance;

    private Context appContext;

    public AtomicInteger totalTaskCount = new AtomicInteger(0);
    public AtomicInteger finishedTaskCount = new AtomicInteger(0);

    public boolean uploading = false;

    Handler handler = new Handler(Looper.getMainLooper());

    public int toUpload = 0;

    public static UploadController getInstance() {
        if (sInstance == null) {
            synchronized (UploadController.class) {
                sInstance = new UploadController();
            }
        }
        return sInstance;
    }

    public UploadController() {
        BusProvider.getInstance().register(this);
    }

    public void init(Context appContext) {
        this.appContext = appContext;
    }

    public void scan() {
        scanCameraPhotos(new ScanCallback() {
            @Override
            public void onCompleted(List<File> newFiles) {
                if (newFiles != null) {
                    toUpload = newFiles.size();
                    BusProvider.getInstance().post(new OnUploadStateChangedEvent());
                }
            }
        });
    }

    public void upload(final List<File> files, final boolean bBackup) {
        if (files.size() > 0) {
            uploading = true;
        }
        finishedTaskCount.set(0);
        for (final File f : files) {
            DataRunner.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    final PhotoStoreClient client = PhotoStoreClient.getInstance();
                    String md5 = FileUtil.getFileMD5(f);
                    String ext = FileUtil.getFileExt(f);
                    if (md5 != null && md5.length() != 32) {
                        Log.d(TAG, f.getAbsolutePath());
                    }
                    List<String> md5s = new ArrayList<String>();
                    md5s.add(md5);
                    String state = "all";
                    client.getPhotosByMd5s(md5s, state, new Callback<GetPhotosByMd5sResponse>() {
                        @Override
                        public void onSuccess(GetPhotosByMd5sResponse response) {
                            boolean bUpload = true;
                            if (response.data.size() > 0) {
                                if (bBackup) {
                                    bUpload = false;
                                }
                                if (!bBackup) {
                                    String photoState = response.data.get(0).state;
                                    if (photoState.equalsIgnoreCase("active")
                                            || photoState.equalsIgnoreCase("staging")) {
                                        bUpload = false;
                                    }
                                }
                            }
                            if (bUpload) {
                                client.upload(appContext, f.getAbsolutePath(), f.length(), ext, false, md5, new TransferDelegate() {
                                    @Override
                                    public void onStart() {
                                    }

                                    @Override
                                    public void onCancel() {
                                    }

                                    @Override
                                    public void onPause() {
                                    }

                                    @Override
                                    public void onComplete() {
                                        if (bBackup) {
                                            List<UploadedPhoto> list = new ArrayList<>();
                                            list.add(new UploadedPhoto(f));
                                            UploadedPhoto.update(list, null);
                                        }

                                        if (finishedTaskCount.incrementAndGet() == totalTaskCount.get()) {
                                            toUpload = 0;
                                            stopBackup();
                                        }
                                        BusProvider.getInstance().post(new OnUploadStateChangedEvent());
                                    }

                                    @Override
                                    public void onError(int i) {

                                        if (finishedTaskCount.incrementAndGet() == totalTaskCount.get()) {
                                            toUpload = 0;
                                            stopBackup();
                                        }
                                        BusProvider.getInstance().post(new OnUploadStateChangedEvent());
                                    }

                                    @Override
                                    public void ReportSnapInfo(long i, long v) {
                                    }
                                });
                            }
                            else {
                                if (bBackup) {
                                    List<UploadedPhoto> list = new ArrayList<>();
                                    list.add(new UploadedPhoto(f));
                                    UploadedPhoto.update(list, null);
                                }

                                if (finishedTaskCount.incrementAndGet() == totalTaskCount.get()) {
                                    toUpload = 0;
                                    stopBackup();
                                }
                                BusProvider.getInstance().post(new OnUploadStateChangedEvent());
                            }
                        }

                        @Override
                        public void onFailure(int code, BaseResponse response) {
                            if (finishedTaskCount.incrementAndGet() == totalTaskCount.get()) {
                                toUpload = 0;
                                stopBackup();
                            }
                            BusProvider.getInstance().post(new OnUploadStateChangedEvent());
                        }
                    });
                }
            });
        }
        totalTaskCount.set(files.size());
        BusProvider.getInstance().post(new OnUploadStateChangedEvent());
    }

    public void startBackup() {
        if (uploading) {
            return;
        }
        scanCameraPhotos(new ScanCallback() {
            @Override
            public void onCompleted(List<File> newFiles) {
                Log.d(TAG, "new photos: " + String.valueOf(newFiles.size()));
                upload(newFiles, true);
            }
        });
    }

    public void stopBackup() {
        DataRunner.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                toUpload = totalTaskCount.get() - finishedTaskCount.get();
                uploading = false;
                BusProvider.getInstance().post(new OnUploadStateChangedEvent());
            }
        });
        if (finishedTaskCount.get() > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    PhotosController.getInstance().updatePhotos();
                }
            }, 1000);
        }
    }

    /**
     * scan DCIM/Camera folder and get not uploaded photo (compare to local db)
     * @return
     */
    private void scanCameraPhotos(final ScanCallback callback) {
        final List<File> results = new ArrayList<>();
        final HashMap<String, File> photoMap = new HashMap<>();

        List<String> folderPathList = new ArrayList<>();

        try {
            StorageManager sm = (StorageManager) appContext.getSystemService(Context.STORAGE_SERVICE); // 获取sdcard的路径：外置和内置
            String[] paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
            for (String p : paths) {
                File dcim = new File(p, "DCIM");
                if (dcim.exists()) {
                    File folder = new File(dcim, "Camera");
                    if (folder.exists())
                        scanPhotos(folder, photoMap, folderPathList);
                    else
                        scanPhotos(dcim, photoMap, folderPathList);
                }
            }
        }
        catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        Log.d(TAG, "images int dcim: " + String.valueOf(photoMap.size()));

        UploadedPhoto.getAll(folderPathList, new DatabaseCallback<UploadedPhoto>() {
            @Override
            public void onCompleted(int code, List<UploadedPhoto> data) {
                if (data == null) return;

                Log.d(TAG, "already uploaded: " + String.valueOf(data.size()));

                HashMap<String, UploadedPhoto> uploadedMap = new HashMap<>();
                for (UploadedPhoto u : data) {
                    uploadedMap.put(u.name, u);
                }

                Iterator<String> it = photoMap.keySet().iterator();

                while (it.hasNext()) {
                    String name = it.next();
                    File f = photoMap.get(name);
                    UploadedPhoto u = uploadedMap.get(name);
                    if (u == null) {
                        // uploaded
                        results.add(f);
                    } else {
                        // check meta info
                        if (u.size != f.length() || u.lastModified != f.lastModified()) {
                            results.add(f);
                        }
                    }
                }

                callback.onCompleted(results);
            }
        });
    }

    private void scanPhotos(File folder, HashMap<String, File> photoMap, List<String> folderPathList) {
        File[] files = folder.listFiles();

        if (files != null) {
            folderPathList.add(folder.getAbsolutePath());
            for (File f : files) {
                if (f.isFile()) {
                    if (FileUtil.getFileExt(f).toLowerCase().equals("jpg") || FileUtil.getFileExt(f).toLowerCase().equals("png")
                            || FileUtil.getFileExt(f).toLowerCase().equals("mp4") || FileUtil.getFileExt(f).toLowerCase().equals("jpeg")) {
                        photoMap.put(f.getName(), f);
                    }
                }
                else if (f.isDirectory()) {
                    scanPhotos(f, photoMap, folderPathList);
                }
            }
        }
    }

    interface ScanCallback {
        void onCompleted(List<File> newFiles);
    }

}
