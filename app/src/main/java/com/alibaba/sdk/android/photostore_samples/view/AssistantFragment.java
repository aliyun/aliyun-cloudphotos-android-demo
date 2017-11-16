/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.adapter.AssistantAdapter;
import com.alibaba.sdk.android.photostore_samples.controller.FacesController;
import com.alibaba.sdk.android.photostore_samples.controller.TagsController;
import com.alibaba.sdk.android.photostore_samples.event.OnGetFacesEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGetTagsEvent;
import com.alibaba.sdk.android.photostore_samples.model.DatabaseCallback;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.alibaba.sdk.android.photostore_samples.model.MyTag;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AssistantFragment extends BaseFragment {

    String TAG = AssistantFragment.class.getSimpleName();

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    GridLayoutManager gridLayoutManager;

    AssistantAdapter adapter;

    int itemPerRow = 2;
    int offset = 0;

    private static final int CALL_LOAD_0 = 0;
    private static final int CALL_LOAD_1 = 1;
    private static final int CALL_LOAD_2 = 2;

    private static final int LOAD_DEFER_MEDIUM = 100;
    private static final int LOAD_DEFER_SHORT = 20;

    MyHandler myHandler = new MyHandler(Looper.myLooper());

    public static AssistantFragment newInstance() {
        return new AssistantFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_assistant, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (adapter == null) {
            adapter = new AssistantAdapter(getActivity(), itemPerRow);
            adapter.addHeader(new View(getActivity()));
            adapter.addFooter(new View(getActivity()));
        }

        gridLayoutManager = new GridLayoutManager(getActivity(), itemPerRow);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });

        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(getContext(), R.dimen.grid_offset);
        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        if (((MainActivity) getActivity()).positionMap.containsKey(TAG)) {
            offset = ((MainActivity) getActivity()).positionMap.get(TAG);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        loadData(0);
        loadData(1);

        fetchData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) getActivity()).positionMap.put(TAG, gridLayoutManager.findLastCompletelyVisibleItemPosition());
    }

    @Subscribe
    public void onGetFaces(OnGetFacesEvent event) {
        myHandler.removeMessages(CALL_LOAD_0);
        myHandler.sendMessageDelayed(myHandler.obtainMessage(CALL_LOAD_0), LOAD_DEFER_MEDIUM);
    }

    @Subscribe
    public void onGetTags(OnGetTagsEvent event) {
        myHandler.removeMessages(CALL_LOAD_1);
        myHandler.sendMessageDelayed(myHandler.obtainMessage(CALL_LOAD_1), LOAD_DEFER_MEDIUM);
    }

    void loadData(int what) {
        Log.d(TAG, "loadData");
        // load memory
        switch (what) {
            case 0:
                MyFace.query(4, new DatabaseCallback<MyFace>() {
                    @Override
                    public void onCompleted(int code, final List<MyFace> data) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.setFacesPeek(data);
                            }
                        });
                    }
                });
                break;
            case 1:
                MyTag.queryExceptSubTag(4, new DatabaseCallback<MyTag>() {
                    @Override
                    public void onCompleted(int code, List<MyTag> data) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (data != null)
                                    adapter.setTagsPeek(data);
                            }
                        });
                    }
                });
                break;
        }
        adapter.setData();
    }

    void fetchData() {
        FacesController.getInstance().fetchFacesByName();
        TagsController.getInstance().fetchTags();
    }

    private class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            if (position < 1) {
                outRect.set(0, 0, 0, 0);
            } else if (position % 2 == 0) {
                outRect.set(mItemOffset / 2, mItemOffset / 2, mItemOffset, mItemOffset / 2);
            } else {
                outRect.set(mItemOffset, mItemOffset / 2, mItemOffset / 2, mItemOffset / 2);
            }
        }
    }

    private class MyHandler extends Handler {

        MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CALL_LOAD_0:
                    loadData(0);
                    break;
                case CALL_LOAD_1:
                    loadData(1);
                    break;
            }
        }
    }
}
