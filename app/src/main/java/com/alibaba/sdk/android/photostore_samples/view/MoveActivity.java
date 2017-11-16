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

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.MoveAdapter;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;
import com.alibaba.sdk.android.photostore_samples.controller.AlbumsController;
import com.alibaba.sdk.android.photostore_samples.controller.FacesController;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbum;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MoveActivity extends AppCompatActivity {

    private MoveAdapter adapter;

    Unbinder unbinder;

    Handler handler = new Handler(Looper.getMainLooper());

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    int cols = 4;
    long sourceId = 0;
    List<Long> ids = new ArrayList<>();
    int whatFragment = FragmentType.ALBUM_PHOTOS.ordinal();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_albums);

        unbinder = ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");

        Bundle bundle = getIntent().getExtras();
        sourceId = bundle.getLong("sourceId", 0);
        ids.clear();

        for (long item : bundle.getLongArray("ids")) {
            ids.add(item);
        }

        whatFragment = bundle.getInt("what", FragmentType.ALBUM_PHOTOS.ordinal());
        if (whatFragment == FragmentType.ALBUM_PHOTOS.ordinal()) {
            setTitle(getText(R.string.title_activity_move_albums));
        }
        else {
            setTitle(getText(R.string.title_activity_move_faces));
        }


        adapter = new MoveAdapter(this, cols);
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
        if (whatFragment == FragmentType.ALBUM_PHOTOS.ordinal())
            asyncMoveAlbumList();
        else
            asyncMoveFaceList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static void launch(Context context, FragmentType what, Long sourceId, List<Long> ids) {
        Intent i = new Intent(context, MoveActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("what", what.ordinal());
        bundle.putLong("sourceId", sourceId);
        long[] array = new long[ids.size()];
        int idx = 0;
        for (Long id : ids) {
            array[idx] = id;
            ++ idx;
        }

        bundle.putLongArray("ids", array);
        i.putExtras(bundle);
        context.startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_move_ok:
                long selectId = adapter.getSelected();

                if (selectId != -1) {
                    if (whatFragment == FragmentType.ALBUM_PHOTOS.ordinal()) {
                        AlbumsController.getInstance().movePhotos(sourceId, ids, selectId);
                    }
                    else if (whatFragment == FragmentType.FACES.ordinal()) {
                        FacesController.getInstance().mergeFaces(selectId, ids);
                    }
                }
                setResult(RESULT_OK, null);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_move, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void asyncMoveAlbumList() {
        List<Long> ids = new ArrayList<>();
        ids.add(sourceId);
        MyAlbum.getAllExceptIds(ids, new DatabaseCallback<MyAlbum>() {
            @Override
            public void onCompleted(int code, List<MyAlbum> data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setAlbums(data);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void asyncMoveFaceList() {
        if (whatFragment == FragmentType.FACES.ordinal()) {
            MyFace.getAllExceptIds(ids, new DatabaseCallback<MyFace>() {
                @Override
                public void onCompleted(int code, List<MyFace> data) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setFaces(data);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
        else if (whatFragment == FragmentType.FACE_PHOTOS.ordinal()) {
            List<Long> faceIds = new ArrayList<>();
            faceIds.add(sourceId);
            MyFace.getAllExceptIds(faceIds, new DatabaseCallback<MyFace>() {
                @Override
                public void onCompleted(int code, List<MyFace> data) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setFaces(data);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }
}
