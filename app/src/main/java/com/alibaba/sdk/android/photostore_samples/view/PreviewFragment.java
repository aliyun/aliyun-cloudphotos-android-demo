/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;
import com.github.chrisbanes.photoview.PhotoView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PreviewFragment extends BaseFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    int position = 0;

    @BindView(R.id.iv_photo)
    PhotoView ivPhoto;

    public static PreviewFragment newInstance(int position) {
        PreviewFragment fragment = new PreviewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        position = bundle.getInt(ARG_SECTION_NUMBER, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preview, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Long pid = PhotosController.getInstance().getPhotoList().get(position);
        // TODO: mengzheng p.isVideo
        ThumbnailLoader.getInstance().loadByPhotoId(ivPhoto, pid, 1024, 1024, false);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
