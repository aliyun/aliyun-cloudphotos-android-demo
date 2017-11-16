/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.photostore_samples.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AssistantAdapter extends HeaderFooterAdapter2 {

    private static final String TAG = HeaderFooterAdapter.class.getSimpleName();

    private enum TYPE {
        HEADER,
        FOOTER,
        CONTENT
    }

    public AssistantAdapter(Context context, int gridCount) {
        super(context, gridCount);
    }

    public void setData() {
        setLoading(false);
    }

    @Override
    public void destroy() {

    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE.CONTENT.ordinal()) {
            return new AssistantViewHolder(mLayoutInflater.inflate(R.layout.grid_assistant, parent, false));
        }
        return null;
    }

    @Override
    protected void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AssistantViewHolder) {
        }
    }

    @Override
    protected int getContentItemCount() {
        return 0;
    }

    @Override
    protected int getContentItemViewType(int position) {
        return TYPE.CONTENT.ordinal();
    }

    @Override
    protected int getContentSpanSize(int position) {
        return 1;
    }

    @Override
    protected void startActionMode(int position) {

    }

    @Override
    protected void finishActionMode() {

    }

    public class AssistantViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView ivPhoto;

        @BindView(R.id.tv_title)
        TextView tvTitle;

        @BindView(R.id.tv_subtitle)
        TextView tvSubtitle;

        public int position = 0;

        public AssistantViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.iv_photo)
        public void onClick() {
        }
    }

}
