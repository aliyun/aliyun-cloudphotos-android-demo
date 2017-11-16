/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.CloudPhotosAdapter;
import com.alibaba.sdk.android.photostore_samples.controller.AlbumsController;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CloudPhotosActivity extends AppCompatActivity {

    private CloudPhotosAdapter adapter;

    Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    int cols = 4;
    long albumId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_photos);

        unbinder = ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");

        Bundle bundle = getIntent().getExtras();
        albumId = bundle.getLong("albumId", 0);

        adapter = new CloudPhotosAdapter(this, cols);
        adapter.addHeader(new View(this));

        gridLayoutManager = new GridLayoutManager(this, cols);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        asyncUploadPhotoList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void launch(Context context, Long albumId) {
        Intent i = new Intent(context, CloudPhotosActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("albumId", albumId);
        i.putExtras(bundle);
        context.startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_cloud_photo_upload:
                List<Long> selected = adapter.getSelected();
                if (selected != null) {
                    AlbumsController.getInstance().addPhotos(albumId, selected);
                }
                setResult(RESULT_OK, null);
                finish();
                return true;
            case R.id.menu_cloud_photo_select_all:
                adapter.selectAll(true);
                return true;
            case R.id.menu_cloud_photo_select_none:
                adapter.selectAll(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cloud_photos, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void asyncUploadPhotoList() {
        MyPhoto.getAll(new DatabaseCallback<MyPhoto>() {
            @Override
            public void onCompleted(int code, List<MyPhoto> data) {
                adapter.setData(data);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
