/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.constants.Constants;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbum;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MoveAdapter extends HeaderFooterAdapter {

    private enum TYPE {
        HEADER,
        FOOTER,
        CONTENT
    }

    private List<MyAlbum> albums = new ArrayList<>();
    private List<MyFace> faces = new ArrayList<>();
    private int selectId = -1;
    private int type = FragmentType.ALBUMS.ordinal();

    public MoveAdapter(Context context, int gridCount) {
        super(context, gridCount);
        isActionMode = true;
    }

    public void setAlbums(List<MyAlbum> datas) {
        type = FragmentType.ALBUMS.ordinal();
        albums.clear();
        albums.addAll(datas);
        selects.clear();
        for (MyAlbum p : albums) {
            selects.add(false);
        }
        selectId = -1;
    }

    public void setFaces(List<MyFace> datas) {
        type = FragmentType.FACES.ordinal();
        faces.clear();
        faces.addAll(datas);
        selects.clear();
        for (MyFace p : faces) {
            selects.add(false);
        }
        selectId = -1;
    }

    @Override
    public void destroy() {
        albums.clear();
        faces.clear();
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE.CONTENT.ordinal()) {
            return new AlbumViewHolder((ViewGroup) mLayoutInflater.inflate(R.layout.grid_face, parent, false));
        }
        return null;
    }

    @Override
    protected void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AlbumViewHolder) {
            final AlbumViewHolder h = (AlbumViewHolder) holder;
            h.position = position;
            h.ivName.setTextColor(Color.WHITE);
            h.checkBox.setVisibility(View.VISIBLE);

            if (type == FragmentType.ALBUMS.ordinal()) {
                final MyAlbum album = albums.get(position);
                h.ivName.setText(album.name);

                ThumbnailLoader.getInstance().loadByPhotoId(h.ivPhoto, album.coverPhotoId, Constants.PHOTO_WIDTH, Constants.PHOTO_HEIGHT);
            }
            else if (type == FragmentType.FACES.ordinal()) {
                final MyFace face = faces.get(position);
                h.ivName.setText(face.name);

                ThumbnailLoader.getInstance().loadCropByPhotoId(h.ivPhoto, face.coverPhotoId, Constants.BIG_PHOTO_WIDTH, Constants.BIG_PHOTO_HEIGHT,
                        face.axisLeft, face.axisTop, face.axisRight, face.axisBottom, face.coverWidth, face.coverHeight);
            }

            if (selectId == position) {
                h.checkBox.setButtonDrawable(R.drawable.btn_check_on);
                h.ivPhoto.setScaleX(0.8f);
                h.ivPhoto.setScaleY(0.8f);
                h.ivMask.setScaleX(0.8f);
                h.ivMask.setScaleY(0.8f);
            } else {
                h.checkBox.setButtonDrawable(R.drawable.btn_check_off);
                h.ivPhoto.setScaleX(1f);
                h.ivPhoto.setScaleY(1f);
                h.ivMask.setScaleX(1f);
                h.ivMask.setScaleY(1f);
            }
        }
    }

    @Override
    protected int getContentItemCount() {
        if (type == FragmentType.ALBUMS.ordinal()) {
            return albums == null ? 0 : albums.size();
        }
        else if (type == FragmentType.FACES.ordinal()) {
            return faces == null ? 0 : faces.size();
        }

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

    public class AlbumViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView ivPhoto;

        @BindView(R.id.iv_name)
        TextView ivName;

        @BindView(R.id.iv_mask)
        ImageView ivMask;

        @BindView(R.id.checkBox)
        CheckBox checkBox;

        int position = 0;

        public AlbumViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }

        @OnClick(R.id.iv_mask)
        void onItemClick() {
            selectId = position;
            notifyDataSetChanged();
        }
    }

    public long getSelected() {
        if (selectId == -1) {
            return -1;
        }

        if (type == FragmentType.ALBUMS.ordinal()) {
            MyAlbum album = albums.get(selectId);
            return album.id;
        }
        else if (type == FragmentType.FACES.ordinal()) {
            MyFace face = faces.get(selectId);
            return face.id;
        }

        return -1;
    }

}
