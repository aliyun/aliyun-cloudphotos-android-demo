/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alibaba.sdk.android.photostore.model.Photo;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.InactivePhotosAdapter;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.event.OnGetInactivePhotosEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class InactivePhotosActivity extends AppCompatActivity {

    private InactivePhotosAdapter adapter;

    Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    Handler handler = new Handler(Looper.getMainLooper());

    int cols = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inactive_photos);

        unbinder = ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");

        adapter = new InactivePhotosAdapter(this, cols);
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
        BusProvider.getInstance().register(this);
        asyncInactivePhotoList();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void launch(Context context) {
        Intent i = new Intent(context, InactivePhotosActivity.class);
        Bundle bundle = new Bundle();
        i.putExtras(bundle);
        context.startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_reactive_photo:
                List<MyPhoto> select_reactive = adapter.getSelected();
                if (select_reactive != null) {
                    PhotosController.getInstance().reactiveCloudPhotos(select_reactive);
                }
                setResult(RESULT_OK, null);
                finish();
                return true;
            case R.id.menu_delete_photo:
                List<MyPhoto> select_delete = adapter.getSelected();
                if (select_delete != null) {
                    PhotosController.getInstance().deleteCloudPhotos(select_delete);
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
        getMenuInflater().inflate(R.menu.menu_inactive_photos, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void asyncInactivePhotoList() {
        PhotosController.getInstance().updateInactivePhotos();
    }

    @Subscribe
    public void onGetInactivePhotos(OnGetInactivePhotosEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<MyPhoto> list = new ArrayList<>();
                if (event != null) {
                    for (Photo p : event.photos) {
                        MyPhoto photo = new MyPhoto(p);
                        list.add(photo);
                    }
                    adapter.setData(list);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}
