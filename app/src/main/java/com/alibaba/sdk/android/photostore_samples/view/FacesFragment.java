/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.FacesAdapter;
import com.alibaba.sdk.android.photostore_samples.controller.FacesController;
import com.alibaba.sdk.android.photostore_samples.event.OnGetFacesEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FacesFragment extends BaseFragment {

    String TAG = FacesFragment.class.getSimpleName();

    Unbinder unbinder;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    FacesAdapter adapter;

    Handler handler = new Handler(Looper.getMainLooper());

    int itemPerRow = 4;
    int offset = 0;

    public static FacesFragment newInstance() {
        return new FacesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            adapter = new FacesAdapter(getActivity(), itemPerRow);
            adapter.addHeader(new View(getContext()));
            adapter.addFooter(new View(getContext()));
        }

        gridLayoutManager = new GridLayoutManager(getActivity(), itemPerRow);
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

        if (((MainActivity) getActivity()).positionMap.containsKey(TAG)) {
            offset = ((MainActivity) getActivity()).positionMap.get(TAG);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.destroy();
        ((MainActivity) getActivity()).positionMap.put(TAG, gridLayoutManager.findLastCompletelyVisibleItemPosition());
    }

    @Subscribe
    public void onGetFaces(OnGetFacesEvent event) {
        loadData();
    }

    void loadData() {
        MyFace.query(-1, new DatabaseCallback<MyFace>() {
            @Override
            public void onCompleted(int code, final List<MyFace> data) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setData(data);
                        if (offset > 0) {
                            gridLayoutManager.scrollToPosition(offset);
                            offset = 0;
                        }
                    }
                });
            }
        });
    }

    public void mergeFaces () {
        adapter.mergeFaces();
    }

    void fetchData() {
        FacesController.getInstance().fetchFacesByName();
    }

    void loadMoreData() {
        Log.d(TAG, "load more");
        FacesController.getInstance().moreFaces();
    }

    public List<MyFace> getSelected() {
        return adapter.getSelected();
    }

}
