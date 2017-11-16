/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.alibaba.sdk.android.photostore.PhotoStoreClient;
import com.alibaba.sdk.android.photostore.api.BaseResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotoFacesResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotoTagsResponse;
import com.alibaba.sdk.android.photostore.api.GetPhotosResponse;
import com.alibaba.sdk.android.photostore.model.Photo;
import com.alibaba.sdk.android.photostore.model.Tag;
import com.alibaba.sdk.android.photostore.runner.Callback;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.alibaba.sdk.android.photostore_samples.util.DateUtil;
import com.alibaba.sdk.android.photostore_samples.util.UiUtil;

import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    int position = 0;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private Handler handler = new Handler(Looper.getMainLooper());

    public static void launch(Context context, int position) {
        Intent i = new Intent(context, PreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        i.putExtras(bundle);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");


        Bundle bundle = getIntent().getExtras();
        position = bundle.getInt("position", 0);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setCurrentItem(position);
        mViewPager.setPageMargin(UiUtil.convertDpToPx(this, 16));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PhotosController.getInstance().setPhotoList(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_info:
                Long photoId = PhotosController.getInstance().getPhotoList().get(mViewPager.getCurrentItem());
                MyPhoto.getById(photoId, new DatabaseCallback<MyPhoto>() {
                    @Override
                    public void onCompleted(int code, List<MyPhoto> data) {
                        if (data != null && data.size() > 0) {
                            MyPhoto p = data.get(0);
                            showAllInfo(p.takenAt, p.title, p.id, p.shareExpireTime);
                        }
                        else {
                            List<Long> ids = new ArrayList<Long>();
                            ids.add(photoId);
                            PhotoStoreClient.getInstance().getPhotos(ids, new Callback<GetPhotosResponse>() {
                                @Override
                                public void onSuccess(GetPhotosResponse response) {
                                    if (response != null && response.data.size() > 0) {
                                        Photo p = response.data.get(0);
                                        showAllInfo(p.takenAt, p.title, p.id, p.shareExpireTime);
                                    }
                                }

                                @Override
                                public void onFailure(int code, BaseResponse response) {

                                }
                            });
                        }
                    }
                });

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showInfo(Long takenAt, String title, Long id, Long shareExpireTime, List<Tag> tags, List<GetPhotoFacesResponse.FaceInfo> faces) {
        StringBuilder sb = new StringBuilder();
        String takenAtStr = DateUtil.formatDate(takenAt);
        String expireTime = DateUtil.formatDate(shareExpireTime/1000);
        String face = "";
        String tag = "";
        if (faces != null) {
            for (int i = 0; i < faces.size(); ++i) {
                face += faces.get(i).id + " ";
            }
        }
        if (tags != null) {
            for (int i = 0; i < tags.size(); ++i) {
                tag += tags.get(i).name + " ";
            }
        }
        sb.append("title: ").append(title).append("\n")
                .append("id: ").append(id).append("\n")
                .append("taken_at: ").append(takenAtStr).append("\n")
                .append("share_expire_time: ").append(expireTime).append("\n")
                .append("faces:").append(face).append("\n")
                .append("tags:").append(tag).append("\n");

        AlertDialog dialog = new AlertDialog.Builder(PreviewActivity.this)
                .setMessage(sb.toString())
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                }).create();

        dialog.show();

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(13);
            textView.setTypeface(Typeface.MONOSPACE);
        }
    }

    private void showAllInfo(Long takenAt, String title, Long id, Long shareExpireTime) {
        PhotoStoreClient.getInstance().getPhotoTags(id, new Callback<GetPhotoTagsResponse>() {
            @Override
            public void onSuccess(GetPhotoTagsResponse response) {
                List<Tag> tags = response.data;
                PhotoStoreClient.getInstance().getPhotoFaces(id, new Callback<GetPhotoFacesResponse>() {
                    @Override
                    public void onSuccess(GetPhotoFacesResponse response) {
                        List<GetPhotoFacesResponse.FaceInfo> faces = response.data;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                showInfo(takenAt, title, id, shareExpireTime, tags, faces);
                            }
                        });
                    }

                    @Override
                    public void onFailure(int code, BaseResponse response) {
                        showInfo(takenAt, title, id, shareExpireTime, tags, null);
                    }
                });
            }

            @Override
            public void onFailure(int code, BaseResponse response) {
                showInfo(takenAt, title, id, shareExpireTime, null, null);
            }
        });
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Long> list = null;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            list = PhotosController.getInstance().getPhotoList();
        }

        @Override
        public Fragment getItem(int position) {
            return PreviewFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}
