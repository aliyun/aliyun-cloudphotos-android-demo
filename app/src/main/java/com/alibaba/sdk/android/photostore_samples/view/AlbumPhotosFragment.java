/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.AlbumPhotosAdapter;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.event.OnGetAlbumPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbumPhoto;
import com.squareup.otto.Subscribe;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumPhotosFragment extends BaseFragment {

    String TAG = AlbumPhotosFragment.class.getSimpleName();

    int offset = 0;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    AlbumPhotosAdapter adapter;

    long id = 0;
    String query = "";
    int cols = 0;

    MyHandler myHandler = new MyHandler(Looper.getMainLooper());

    private static final int LOAD_DEFER_LONG = 200;
    private static final int LOAD_DEFER_MEDIUM = 100;
    private static final int LOAD_DEFER_SHORT = 20;

    private static final int CALL_LOAD = 0;
    private static final int NOTIFY_DATA_CHANGED = 1;

    boolean tryOnceMore = false;

    public static AlbumPhotosFragment newInstance() {
        return new AlbumPhotosFragment();
    }

    public AlbumPhotosFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        what = ContentType.ALBUM_PHOTO.ordinal();
        Bundle bundle = getArguments();
        if (bundle != null) {
            what = bundle.getInt("what", ContentType.ALBUM_PHOTO.ordinal());
            id = bundle.getLong("id", 0);
            query = bundle.getString("query", "");
            cols = bundle.getInt("cols", 0);
        }

        setHasOptionsMenu(true);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.d(TAG, String.valueOf(metrics.density));
        Log.d(TAG, String.valueOf(metrics.widthPixels));
        if (cols == 0) {
            cols = 4;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_photos, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (adapter == null) {
            adapter = new AlbumPhotosAdapter(getActivity(), cols);
            adapter.addHeader(new View(getContext()));
            adapter.addFooter(new View(getContext()));
        }

        gridLayoutManager = new GridLayoutManager(getActivity(), cols);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        loadData();

        if (what == ContentType.ALBUM_PHOTO.ordinal()) {
            if (((MainActivity) getActivity()).positionMap.containsKey(TAG)) {
                offset = ((MainActivity) getActivity()).positionMap.get(TAG);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (what == ContentType.ALBUM_PHOTO.ordinal()) {
            ((MainActivity) getActivity()).positionMap.put(TAG, gridLayoutManager.findLastCompletelyVisibleItemPosition());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_select:
                adapter.startActionMode();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void loadData() {
        Log.d(TAG, "loadData");
        if (what == ContentType.ALBUM_PHOTO.ordinal()) {
            PhotosController.getInstance().updateAlbumPhotos(id);
            adapter.setAlbumId(id);
        }
    }

    void callLoad(int delay) {
        myHandler.removeMessages(CALL_LOAD);
        myHandler.sendMessageDelayed(myHandler.obtainMessage(CALL_LOAD), delay);
    }

    void buildAndNotify(int delay) {
        myHandler.removeMessages(NOTIFY_DATA_CHANGED);
        myHandler.sendMessageDelayed(myHandler.obtainMessage(NOTIFY_DATA_CHANGED), delay);
    }

    private class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CALL_LOAD:
                    loadData();
                    break;
                case NOTIFY_DATA_CHANGED:
                    adapter.buildData(new AlbumPhotosAdapter.BuildDataCallback() {
                        @Override
                        public void onComplete() {
                            if (offset > 0) {
                                gridLayoutManager.scrollToPosition(offset);
                                offset = 0;
                            }
                        }
                    });
                    break;
            }
        }
    }

    public List<Long> getSelected() {
        return adapter.getSelected();
    }

    public List<Long> getPhotos() {
        return adapter.getPhotos();
    }

    public long getAlbumId() {
        return id;
    }

    public void startActionMode() {
        adapter.startActionMode();
    }

    public void movePhotosToAlbum () {
        adapter.movePhotosToAlbum();
    }

    @Subscribe
    public void onGetAlbumsPhotos(OnGetAlbumPhotosEvent event) {
        MyAlbumPhoto.getByAlbum(id, new DatabaseCallback<Long>() {
            @Override
            public void onCompleted(int code, List<Long> data) {
                data.add(0, -1L);
                adapter.setCloudPhotos(data);
                buildAndNotify(LOAD_DEFER_SHORT);
            }
        });
    }
}
