/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotoStoreResponse;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.constants.Constants;
import com.alibaba.sdk.android.photostore_samples.controller.AccountController;
import com.alibaba.sdk.android.photostore_samples.controller.CategoryController;
import com.alibaba.sdk.android.photostore_samples.controller.StyledPhotosController;
import com.alibaba.sdk.android.photostore_samples.controller.FacesController;
import com.alibaba.sdk.android.photostore_samples.controller.MomentsController;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.controller.UploadController;
import com.alibaba.sdk.android.photostore_samples.event.OnLoginEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnLogoutEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseHelper;
import com.alibaba.sdk.android.photostore_samples.util.PreferenceManager;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyApplication extends Application {

    static String TAG = MyApplication.class.getSimpleName();

    public static boolean isPresLogin = false;
    public static String event = "";
    public static int autoCleanDays;
    public static boolean autoCleanEnabled;
    public static boolean tokenExpired;
    public static int shareExpireDays = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        init();
    }

    public void init() {
        SharedPreferences sharedPreferences = PreferenceManager.getSharedPref(this);
        String uid = sharedPreferences.getString(Constants.PREF_UID, "");
        if (uid != null && !uid.equals("")) {
            String database_name = uid + ".db";
            DatabaseHelper.init(this.getApplicationContext(), database_name);
        }
        else {
            DatabaseHelper.init(this.getApplicationContext(), "photo.db");
        }

        final Context context = this;

        MomentsController.getInstance();
        PhotosController.getInstance();
        FacesController.getInstance();
        CategoryController.getInstance();
        StyledPhotosController.getInstance();
        UploadController.getInstance().init(context);

        ThumbnailLoader.init(context);
        String currentEnv = sharedPreferences.getString(Constants.PREF_ENV, "");
        PhotoStoreClient.getInstance().setEnv(currentEnv);

        BusProvider.getInstance().register(this);
    }

    public static boolean copyAssetFolder(Context context, String fromAssetPath, String toPath) {
        try {
            String[] files = context.getAssets().list(fromAssetPath);
            File toFile = new File(toPath);
            if (toFile.exists() && toFile.isDirectory()) {
                return false;
            } else {
                toFile.mkdir();
            }
            boolean res = true;
            for (String file : files) {
                if (file.contains(".")) {
                    res &= copyAsset(context, fromAssetPath + "/" + file,
                            toPath + "/" + file);
                } else {
                    res &= copyAssetFolder(context, fromAssetPath + "/" + file,
                            toPath + "/" + file);
                }
            }
            return res;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(Context context, String fromAssetPath, String toPath) {
        File toFile = new File(toPath);
        boolean rt = false;
        if (!toFile.exists()) {
            try {
                toFile.createNewFile();
                InputStream is = context.getAssets().open(fromAssetPath);
                rt = copyToFile(is, toFile);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rt;
    }

    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Subscribe
    public void onLogin(OnLoginEvent event) {
        if (event.user != null) {
            String uid = event.user.kp;
            if (uid != null && !uid.equals("")) {
                tokenExpired = false;
                String database_name = uid + ".db";
                DatabaseHelper.init(this.getApplicationContext(), database_name);
                PhotoStoreClient.getInstance().getPhotoStore(new Callback<GetPhotoStoreResponse>() {
                    @Override
                    public void onSuccess(GetPhotoStoreResponse response) {
                        autoCleanDays = response.photoStore.autoCleanDays;
                        autoCleanEnabled = response.photoStore.autoCleanEnabled;
                    }

                    @Override
                    public void onFailure(int code, BaseResponse response) {

                    }
                });
            }
            else {
                DatabaseHelper.init(this.getApplicationContext(), "photo.db");
            }
        }
    }

    @Subscribe
    public void onLogout(OnLogoutEvent event) {
        // clear all data
        if (event.isInvalid) {
            Log.d(TAG, "InvalidSecurityToken.Expired");
            Toast.makeText(this, R.string.login_invalid, Toast.LENGTH_SHORT).show();
        }
        UploadController.getInstance().stopBackup();
        DatabaseHelper.getInstance().clearAll();
        PreferenceManager.clear(this);
        PhotoStoreClient.getInstance().cancelAll();
    }

}

