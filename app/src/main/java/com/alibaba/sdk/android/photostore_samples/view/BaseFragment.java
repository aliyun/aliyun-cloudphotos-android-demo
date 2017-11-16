/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;

import butterknife.Unbinder;

public class BaseFragment extends Fragment {

    Unbinder unbinder;

    Handler handler = new Handler(Looper.getMainLooper());

    public int what = ContentType.PHOTO.ordinal();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getSimpleName(), "onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getClass().getSimpleName(), "onResume");
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(getClass().getSimpleName(), "onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(getClass().getSimpleName(), "onDetach");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getClass().getSimpleName(), "onPause");
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(getClass().getSimpleName(), "onDestroyView");
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(getClass().getSimpleName(), "onDestroy");
        super.onDestroy();
    }
    
}
