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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.TagsAdapter;
import com.alibaba.sdk.android.photostore_samples.controller.TagsController;
import com.alibaba.sdk.android.photostore_samples.event.OnGetTagsEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyTag;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TagsFragment extends BaseFragment {

    String TAG = TagsFragment.class.getSimpleName();

    Unbinder unbinder;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    TagsAdapter adapter;

    Handler handler = new Handler(Looper.getMainLooper());

    int itemPerRow = 4;
    int offset = 0;

    public static TagsFragment newInstance() {
        return new TagsFragment();
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

        adapter = new TagsAdapter(getActivity(), itemPerRow);
        adapter.addHeader(new View(getContext()));
        adapter.addFooter(new View(getContext()));

        gridLayoutManager = new GridLayoutManager(getActivity(), itemPerRow);
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
    public void onGetTags(OnGetTagsEvent event) {
        loadData();
    }

    void loadData() {
        MyTag.queryExceptSubTag(-1, new DatabaseCallback<MyTag>() {
            @Override
            public void onCompleted(int code, final List<MyTag> data) {
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

    void fetchData() {
        TagsController.getInstance().fetchTags();
    }

}
