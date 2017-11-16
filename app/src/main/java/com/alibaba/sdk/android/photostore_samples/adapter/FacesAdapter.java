/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;
import com.alibaba.sdk.android.photostore_samples.event.OnFinishActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGoToFragmentEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnStartActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;
import com.alibaba.sdk.android.photostore_samples.view.MoveActivity;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FacesAdapter extends HeaderFooterAdapter {

    private enum TYPE {
        HEADER,
        FOOTER,
        CONTENT
    }

    List<MyFace> datas;

    Handler handler = new Handler(Looper.getMainLooper());

    public FacesAdapter(Context context, int gridCount) {
        super(context, gridCount);
        datas = new ArrayList<>();
        BusProvider.getInstance().register(this);
    }

    public void setData(List<MyFace> list) {
        datas.clear();
        datas.addAll(list);
        selects.clear();
        for (MyFace p : datas) {
            selects.add(false);
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
        final MyFace o = datas.get(position);
        final PhotoViewHolder h = (PhotoViewHolder) holder;

        h.position = position;
        h.ivName.setTextColor(Color.WHITE);
        h.ivName.setText(o.name);

        boolean isVideo = false;

        ThumbnailLoader.getInstance().loadCropByPhotoId(h.ivPhoto, o.coverPhotoId, 1024, 1024, isVideo,
                o.axisLeft, o.axisTop, o.axisRight, o.axisBottom, o.coverWidth, o.coverHeight);

        if (isActionMode) {
            h.checkBox.setVisibility(View.VISIBLE);
            if (selects.get(position)) {
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
        } else {
            h.checkBox.setVisibility(View.INVISIBLE);
            h.ivPhoto.setScaleX(1f);
            h.ivPhoto.setScaleY(1f);
            h.ivMask.setScaleX(1f);
            h.ivMask.setScaleY(1f);
        }
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
        isActionMode = true;
        for (MyFace a : datas) {
            selects.add(false);
        }
        BusProvider.getInstance().post(new OnStartActionModeEvent());
        notifyDataSetChanged();
    }

    @Override
    protected void finishActionMode() {
        isActionMode = false;
        selects.clear();
        notifyDataSetChanged();
    }

    @Subscribe
    public void onStartActionMode(OnStartActionModeEvent event) {
    }

    @Subscribe
    public void onFinishActoinMode(OnFinishActionModeEvent event) {
        finishActionMode();
    }

    @Override
    public void destroy() {

    }

    public void clear() {
        datas.clear();
        selects.clear();
        notifyDataSetChanged();
        setLoading(true);
    }

    public void mergeFaces() {
        MoveActivity.launch(mContext, FragmentType.FACES, -1L, getSelectedIds());
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView ivPhoto;

        @BindView(R.id.iv_mask)
        ImageView ivMask;

        @BindView(R.id.checkBox)
        CheckBox checkBox;

        @BindView(R.id.iv_name)
        TextView ivName;

        int position = 0;

        public PhotoViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }

        void toggle() {
            if (selects.get(position)) {
                checkBox.setButtonDrawable(R.drawable.btn_check_off);
                ivPhoto.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                ivMask.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            } else {
                checkBox.setButtonDrawable(R.drawable.btn_check_on);
                ivPhoto.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).start();
                ivMask.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).start();
            }
            selects.set(position, !selects.get(position));
        }

        @OnClick(R.id.iv_mask)
        void onItemClick() {
            if (isActionMode) {
                toggle();
            } else {
                BusProvider.getInstance().post(
                        OnGoToFragmentEvent.build(FragmentType.FACE_PHOTOS, ContentType.FACE_PHOTO).id(datas.get(position).id));
            }
        }

        @OnClick(R.id.checkBox)
        void onCheckClick() {
            toggle();
        }

        @OnLongClick(R.id.iv_mask)
        boolean onItemLongClick() {
            if (!isActionMode) {
                startActionMode();
                toggle();
                return true;
            }
            return false;
        }
    }

    public List<MyFace> getSelected() {
        List<MyFace> list = new ArrayList<>();
        for (int i = 0; i < selects.size(); i++) {
            if (selects.get(i)) {
                MyFace f = datas.get(i);
                if (f != null) {
                    list.add(datas.get(i));
                }
            }
        }
        return list;
    }

    public List<Long> getSelectedIds() {
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < selects.size(); i++) {
            if (selects.get(i)) {
                MyFace f = datas.get(i);
                if (f != null) {
                    list.add(datas.get(i).id);
                }
            }
        }
        return list;
    }
}
