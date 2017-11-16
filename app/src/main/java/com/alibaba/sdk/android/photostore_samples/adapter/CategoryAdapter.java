/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.event.OnStartActionModeEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CategoryAdapter extends HeaderFooterAdapter {

    private enum TYPE {
        HEADER,
        FOOTER,
        CONTENT
    }

    public CategoryAdapter(Context context, int gridCount) {
        super(context, gridCount);

        BusProvider.getInstance().register(this);
    }

    public void setData() {
        setLoading(false);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder((ViewGroup) mLayoutInflater.inflate(R.layout.grid_category, parent, false));
    }

    @Override
    protected void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
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
    protected void startActionMode() {

    }

    @Override
    protected void finishActionMode() {

    }

    @Override
    public void destroy() {

    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView ivPhoto;
        @BindView(R.id.tv_title)
        TextView tvTitle;

        int position = 0;

        public PhotoViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }

        @OnClick(R.id.iv_photo)
        void onItemClick() {
            if (isActionMode) {
                selects.set(position, true);
            } else {
            }
        }
    }
}
