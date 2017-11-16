/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import android.widget.EditText;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.AlbumsAdapter;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.controller.AlbumsController;
import com.alibaba.sdk.android.photostore_samples.controller.SearchController;
import com.alibaba.sdk.android.photostore_samples.event.OnCreateAlbumEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetAlbumsEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbum;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumsFragment extends BaseFragment {

    String TAG = AlbumsFragment.class.getSimpleName();

    int offset = 0;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    AlbumsAdapter adapter;

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

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    public AlbumsFragment() {

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
            cols = 2;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_albums, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (adapter == null) {
            adapter = new AlbumsAdapter(getActivity(), cols);
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

        fetchData();

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
        SearchController.getInstance().resultPhotos.clear();
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

    @Subscribe
    public void onGetAlbums(final OnGetAlbumsEvent event) {
        if (what == ContentType.ALBUM_PHOTO.ordinal()) {
            callLoad(LOAD_DEFER_LONG);
        }
    }

    @Subscribe
    public void onCreateAlbumEvent(OnCreateAlbumEvent event) {
        final EditText editText = new EditText(getContext());
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(getContext());
        inputDialog.setTitle(getString(R.string.action_create_album)).setView(editText);
        inputDialog.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlbumsController.getInstance().createAlbum(editText.getText().toString());
                    }
                }).setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    void fetchData() {
        Log.d(TAG, "update: " + what);
        AlbumsController.getInstance().fetchAllAlbums(false);
    }

    public void query(String query) {
        adapter.clear();
        SearchController.getInstance().search(query);
    }

    void loadData() {
        Log.d(TAG, "loadData");
        if (what == ContentType.ALBUM_PHOTO.ordinal()) {
            MyAlbum.getAll(new DatabaseCallback<MyAlbum>() {
                @Override
                public void onCompleted(int code, final List<MyAlbum> data) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setAlbums(data);
                            buildAndNotify(LOAD_DEFER_SHORT);
                        }
                    });

                    if (data == null || data.size() == 0) {
                        if (!tryOnceMore) {
                            tryOnceMore = true;
                            AlbumsController.getInstance().fetchAllAlbums(true);
                            buildAndNotify(LOAD_DEFER_MEDIUM);
                        }
                    }
                }
            });
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
                    adapter.buildData(new AlbumsAdapter.BuildDataCallback() {
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

    public List<MyAlbum> getSelected() {
        return adapter.getSelected();
    }

    public void startActionMode() {
        adapter.startActionMode();
    }
}
