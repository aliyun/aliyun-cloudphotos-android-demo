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
import com.alibaba.sdk.android.photostore_samples.adapter.MomentsAdapter;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.controller.MomentsController;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.controller.SearchController;
import com.alibaba.sdk.android.photostore_samples.event.OnGetMomentsEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetMomentsPhotosEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnSearchEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyMoment;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.alibaba.sdk.android.photostore_samples.model.MyMomentPhoto;
import com.alibaba.sdk.android.photostore_samples.util.DateUtil;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MomentsFragment extends BaseFragment {

    String TAG = MomentsFragment.class.getSimpleName();

    int offset = 0;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    MomentsAdapter adapter;

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

    public static MomentsFragment newInstance() {
        return new MomentsFragment();
    }

    public MomentsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        what = ContentType.MOMENT_PHOTO.ordinal();
        Bundle bundle = getArguments();
        if (bundle != null) {
            what = bundle.getInt("what", ContentType.MOMENT_PHOTO.ordinal());
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
        View rootView = inflater.inflate(R.layout.fragment_moments, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (adapter == null) {
            adapter = new MomentsAdapter(getActivity(), cols);
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
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore() {
                Log.d(TAG,"invoke onLoadMore...");
                loadMoreData();
            }
        });

        loadData();

        fetchData();

        if (what == ContentType.MOMENT_PHOTO.ordinal()) {
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
        if (what == ContentType.MOMENT_PHOTO.ordinal()) {
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
    public void onGetMomentsPhotos(OnGetMomentsPhotosEvent event) {
        if (what == ContentType.MOMENT_PHOTO.ordinal()) {
            callLoad(LOAD_DEFER_LONG);
        }
    }

    @Subscribe
    public void onGetMoments(OnGetMomentsEvent event) {
        if (event.moments == null) {
            callLoad(LOAD_DEFER_LONG);
        }
    }

    @Subscribe
    public void onSearch(final OnSearchEvent event) {
        if (what == ContentType.SEARCH_PHOTO.ordinal()) {
            callLoad(LOAD_DEFER_MEDIUM);
        }
    }

    void fetchData() {
        Log.d(TAG, "update: " + what);
        if (what == ContentType.MOMENT_PHOTO.ordinal()) {
            MomentsController.getInstance().updateMoments(true);
        }
    }

    void loadMoreData() {
        Log.d(TAG, "load more: " + what);
        if (what == ContentType.MOMENT_PHOTO.ordinal()) {
            MomentsController.getInstance().moreMoments(true);
        }
    }

    public void query(String query) {
        if (adapter != null)
            adapter.clear();
        SearchController.getInstance().search(query);
    }

    void loadData() {
        Log.d(TAG, "loadData");
        if (what == ContentType.MOMENT_PHOTO.ordinal()) {
            MyMoment.getAll(new DatabaseCallback<MyMoment>() {
                @Override
                public void onCompleted(int code, final List<MyMoment> data) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setMoments(data);
                            buildAndNotify(LOAD_DEFER_SHORT);
                        }
                    });

                    if (data == null || data.size() == 0) {
                        if (!tryOnceMore) {
                            tryOnceMore = true;
                            MomentsController.getInstance().updateMoments(true);
                        }
                    } else {
                        for (final MyMoment m : data) {
                            MyMomentPhoto.getByMoment(m.id, new DatabaseCallback<Long>() {
                                @Override
                                public void onCompleted(int code, final List<Long> data) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.setMomentPhotos(m.id, data);
                                            buildAndNotify(LOAD_DEFER_MEDIUM);
                                        }
                                    });

                                    if (m.photosCount > data.size()) {
                                        PhotosController.getInstance().updateMomentPhotos(m.id);
                                    }
                                }
                            });
                        }
                    }
                }
            });
        } else if (what == ContentType.SEARCH_PHOTO.ordinal()) {
            List<MyPhoto> data = SearchController.getInstance().resultPhotos;
            loadWithFlatPhotosList(data);
        }
    }

    void loadWithFlatPhotosList(List<MyPhoto> photos) {
        // group by date
        final HashMap<Long, List<MyPhoto>> map = new HashMap<>();
        for (MyPhoto p : photos) {
            long dt = DateUtil.getDayStamp(p.takenAt);
            if (map.containsKey(dt)) {
                List<MyPhoto> list = map.get(dt);
                list.add(p);
            } else {
                List<MyPhoto> list = new ArrayList<>();
                list.add(p);
                map.put(dt, list);
            }
        }

        final HashMap<Long, MyMoment> momentMap = new HashMap<>();
        final List<MyMoment> moments = new ArrayList<>();

        final Iterator<Long> it = map.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            long dt = it.next();
            MyMoment mm = new MyMoment(i++);
            mm.takenAt = dt*1000;
            mm.photosCount = map.get(dt).size();
            momentMap.put(dt, mm);
            moments.add(mm);
        }

        Collections.sort(moments, new Comparator<MyMoment>() {
            @Override
            public int compare(MyMoment o1, MyMoment o2) {
                return o1.takenAt < o2.takenAt ? 1 : -1;
            }
        });

        handler.post(new Runnable() {
            @Override
            public void run() {
                adapter.setMoments(moments);

                Iterator<Long> it = map.keySet().iterator();
                while (it.hasNext()) {
                    long dt = it.next();
                    List<MyPhoto> list = map.get(dt);
                    List<Long> ids = new ArrayList<Long>();
                    for (int i = 0; i < list.size(); ++i) {
                        ids.add(list.get(i).id);
                    }
                    adapter.setMomentPhotos(momentMap.get(dt).id, ids);
                }

                buildAndNotify(1);
            }
        });
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
                    adapter.buildData(new MomentsAdapter.BuildDataCallback() {
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

    public void startActionMode() {
        adapter.startActionMode();
    }
}
