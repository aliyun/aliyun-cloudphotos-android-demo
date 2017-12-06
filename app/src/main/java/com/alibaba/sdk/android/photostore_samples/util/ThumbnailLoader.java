/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.GetThumbnailResponse;
import com.alibaba.sdk.android.photostore.api.GetVideoCoverResponse;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ThumbnailLoader {

    private String TAG = ThumbnailLoader.class.getSimpleName();

    private Handler handler = new Handler(Looper.getMainLooper());

    private Map<String, String> urlMap = new HashMap<>();

    private static ThumbnailLoader sInstance;

    private Context appContext;

    private String cachePath;

    public static ThumbnailLoader getInstance() {
        return sInstance;
    }

    public static void init(Context appContext) {
        sInstance = new ThumbnailLoader(appContext);
    }

    private ThumbnailLoader(Context appContext) {
        this.appContext = appContext;
        this.cachePath = appContext.getCacheDir().getPath() + File.separator;
        handler = new Handler(Looper.getMainLooper());
    }

    public void loadCropByPhotoId(final ImageView iv, final Long photoId, final int photoWidth, final int photoHeight,
                         int left, int top, int right, int bottom, int width, int height) {
        if (width != 0 && height != 0) {
            float rl = (float) left / width;
            float rt = (float) top / height;
            float rw = (float) (right - left) / width;
            float rh = (float) (bottom - top) / height;

            float margin = 0.02f;

            CropTransformation transformation = new CropTransformation(rl - margin, rt - margin, rw + 2 * margin, rh + 2 * margin);

            loadByPhotoId(iv, photoId, photoWidth, photoHeight, transformation);
        } else {
            loadByPhotoId(iv, photoId, photoWidth, photoHeight, null);
        }
    }
    public void loadByPhotoId(final ImageView iv,
                              final Long photoId,
                              final int width,
                              final int height) {
        loadByPhotoId(iv, photoId, width, height, null);
    }

    private void loadByPhotoId(final ImageView iv,
                      final Long photoId, final int width, final int height,
                      final Transformation transformation) {

        iv.setImageBitmap(null);

        String key = String.valueOf(photoId) + "_" + String.valueOf(width) + "*" + String.valueOf(height);
        iv.setTag(key);
        Picasso.with(appContext.getApplicationContext()).cancelRequest(iv);

        if (photoId == -1 || photoId == 0) {
            return;
        }

        File file = new File(cachePath, String.valueOf(photoId) + "_" + String.valueOf(width) + "*" + String.valueOf(height));
        if (file.exists()) {
            RequestCreator creator = Picasso.with(appContext.getApplicationContext()).load(file);
            if (transformation != null) {
                creator.transform(transformation);
            }
            creator.into(iv);
        }
        else {
            String url = urlMap.get(key);
            if (url != null) {
                downloadImage(iv, url, photoId, width, height, transformation);
            } else {
                PhotoStoreClient.getInstance().getThumbnail(photoId, width, height, new Callback<GetThumbnailResponse>() {
                    @Override
                    public void onSuccess(GetThumbnailResponse response) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                urlMap.put(key, response.url);
                                downloadImage(iv, response.url, photoId, width, height, transformation);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int code, BaseResponse response) {
                    }
                });
            }
        }
    }

    private void downloadImage(ImageView iv, String url, Long photoId, int width, int height, Transformation transformation) {
        Uri uri = Uri.parse(url);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                FileOutputStream ostream = null;
                try {
                    File file = new File(cachePath, String.valueOf(photoId) + "_" + String.valueOf(width) + "*" + String.valueOf(height));
                    ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();

                    // 显示
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            RequestCreator creator = Picasso.with(appContext.getApplicationContext()).load(file);
                            if (transformation != null) {
                                creator.transform(transformation);
                            }
                            creator.into(iv);
                        }
                    });

                    Log.d(TAG, "onBitmapLoaded");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        //Picasso下载
        iv.setTag(target);
        Picasso.with(appContext.getApplicationContext()).load(uri).into(target);
    }

}
