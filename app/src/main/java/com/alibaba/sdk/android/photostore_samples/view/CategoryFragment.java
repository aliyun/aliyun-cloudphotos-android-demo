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
import com.alibaba.sdk.android.photostore_samples.adapter.CategoryAdapter;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.event.OnGetCategoriesEvent;
import com.squareup.otto.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CategoryFragment extends BaseFragment {

    String TAG = CategoryFragment.class.getSimpleName();

    Unbinder unbinder;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    CategoryAdapter adapter;

    Handler handler = new Handler(Looper.getMainLooper());

    int itemPerRow = 3;
    int offset = 0;

    public static CategoryFragment newInstance() {
        return new CategoryFragment ();
    }

    int what = 0;
    long id = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            what = bundle.getInt("what", ContentType.PLACES.ordinal());
            id = bundle.getLong("id", 0);
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

        adapter = new CategoryAdapter(getActivity(), itemPerRow);
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
    public void onGetCategory(OnGetCategoriesEvent event) {
        loadData();
    }

    void loadData() {
    }

    void fetchData() {
    }
    
}
