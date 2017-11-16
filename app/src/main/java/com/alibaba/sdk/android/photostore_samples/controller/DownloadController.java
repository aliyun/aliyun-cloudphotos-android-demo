/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.controller;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.GetDownloadResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotosResponse;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.alibaba.sdk.android.photostore_samples.util.DataRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadController {
    private static final String TAG = DownloadController.class.getSimpleName();

    private static DownloadController sInstance;

    private DownloadChangeObserver downloadObserver;
    DownloadManager downloadManager;
    private long lastDownloadId = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    public static DownloadController getInstance() {
        if (sInstance == null) {
            synchronized (DownloadController.class) {
                sInstance = new DownloadController();
            }
        }
        return sInstance;
    }

    public void download(Context context, final List<Long> photos, String downloadPath) {
        for (Long pid:photos) {
            DataRunner.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    final PhotoStoreClient client = PhotoStoreClient.getInstance();
                    client.getDownload(pid, new Callback<GetDownloadResponse>() {
                        @Override
                        public void onSuccess(GetDownloadResponse response) {
                            final String downloadUrl = response.url;
                            MyPhoto.getById(pid, new DatabaseCallback<MyPhoto>() {
                                @Override
                                public void onCompleted(int code, List<MyPhoto> data) {
                                    if (data != null && data.size() > 0) {
                                        String title = data.get(0).title;
                                        downloadImg(title, context, downloadPath, downloadUrl);
                                    }
                                    else {
                                        List<Long> ids = new ArrayList<Long>();
                                        ids.add(pid);
                                        client.getPhotos(ids, new Callback<GetPhotosResponse>() {
                                            @Override
                                            public void onSuccess(GetPhotosResponse response) {
                                                if (response.data.size() > 0) {
                                                    String title = response.data.get(0).title;
                                                    downloadImg(title, context, downloadPath, downloadUrl);
                                                }
                                            }

                                            @Override
                                            public void onFailure(int code, BaseResponse response) {

                                            }
                                        });
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(int code, BaseResponse response) {

                        }
                    });
                }
            });
        }
    }

    private void downloadImg(String title, Context context, String downloadPath, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        File saveFile = new File(Environment.getExternalStorageDirectory() + "/Download", title);
        request.setDestinationUri(Uri.fromFile(saveFile));
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        lastDownloadId = downloadManager.enqueue(request);
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadObserver = new DownloadChangeObserver(null);
        context.getContentResolver().registerContentObserver(Uri.parse("content://downloads/"), true, downloadObserver);
    }


    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onChange(boolean selfChange) {
            queryDownloadStatus();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //这里可以取得下载的id，这样就可以知道哪个文件下载完成了。适用与多个下载任务的监听
            Log.v(TAG, "" + intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
            queryDownloadStatus();
        }
    };

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(lastDownloadId);
        Cursor c = downloadManager.query(query);
        if(c != null && c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

            int reasonIdx = c.getColumnIndex(DownloadManager.COLUMN_REASON);
            int titleIdx = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
            int fileSizeIdx =
                    c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            int bytesDLIdx =
                    c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            String title = c.getString(titleIdx);
            int fileSize = c.getInt(fileSizeIdx);
            int bytesDL = c.getInt(bytesDLIdx);

            // Translate the pause reason to friendly text.
            int reason = c.getInt(reasonIdx);
            StringBuilder sb = new StringBuilder();
            sb.append(title).append("\n");
            sb.append("Downloaded ").append(bytesDL).append(" / " ).append(fileSize);

            // Display the status
            Log.d(TAG, sb.toString());
            switch(status) {
                case DownloadManager.STATUS_PAUSED:
                    Log.v(TAG, "STATUS_PAUSED");
                case DownloadManager.STATUS_PENDING:
                    Log.v(TAG, "STATUS_PENDING");
                case DownloadManager.STATUS_RUNNING:
                    // 正在下载，不做任何事情
                    Log.v(TAG, "STATUS_RUNNING");
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    // 完成
                    Log.v(TAG, "下载完成");
                    downloadManager.remove(lastDownloadId);
                    break;
                case DownloadManager.STATUS_FAILED:
                    // 清除已下载的内容，重新下载
                    Log.v(TAG, "STATUS_FAILED");
                    downloadManager.remove(lastDownloadId);
                    break;
            }
        }
    }
}
