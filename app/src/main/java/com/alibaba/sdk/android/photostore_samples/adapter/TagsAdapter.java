/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.constants.Constants;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;
import com.alibaba.sdk.android.photostore_samples.event.OnGoToFragmentEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnStartActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyTag;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TagsAdapter extends HeaderFooterAdapter {

    private enum TYPE {
        HEADER,
        FOOTER,
        CONTENT
    }

    List<MyTag> datas;

    public TagsAdapter(Context context, int gridCount) {
        super(context, gridCount);
        datas = new ArrayList<>();
        BusProvider.getInstance().register(this);
    }

    public void setData(List<MyTag> list) {
        datas.clear();
        if (list != null) {
            datas.addAll(list);
        }
        notifyDataSetChanged();

        setLoading(false);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        return new PhotoViewHolder((ViewGroup) mLayoutInflater.inflate(R.layout.grid_face, parent, false));
    }

    @Override
    protected void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MyTag o = datas.get(position);
        final PhotoViewHolder h = (PhotoViewHolder) holder;
        h.position = position;
        h.ivName.setText(o.name);
        h.ivName.setTextColor(Color.WHITE);

        ThumbnailLoader.getInstance().loadByPhotoId(h.ivPhoto, o.coverPhotoId, Constants.PHOTO_WIDTH, Constants.PHOTO_HEIGHT);
    }

    @Override
    protected int getContentItemCount() {
        return datas.size();
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

        @BindView(R.id.iv_name)
        TextView ivName;

        int position = 0;

        public PhotoViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }

        @OnClick(R.id.iv_mask)
        void onItemClick() {
            if (isActionMode) {
                selects.set(position, true);
            } else {
                BusProvider.getInstance().post(
                        OnGoToFragmentEvent.build(FragmentType.PHOTOS, ContentType.TAG_PHOTO).id(datas.get(position).id));
            }
        }
    }
}
