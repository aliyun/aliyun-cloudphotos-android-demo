/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.controller.PhotosController;
import com.alibaba.sdk.android.photostore_samples.event.OnFinishActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnStartActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyMoment;
import com.alibaba.sdk.android.photostore_samples.util.DataRunner;
import com.alibaba.sdk.android.photostore_samples.util.DateUtil;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;
import com.alibaba.sdk.android.photostore_samples.view.PreviewActivity;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MomentsAdapter extends HeaderFooterAdapter {

    private static final String TAG = MomentsAdapter.class.getSimpleName();

    private enum TYPE {
        HEADER,
        FOOTER,
        CONTENT_SPAN,
        CONTENT
    }

    private List<MyMoment> sMoments;
    private HashMap<Long, List<Long>> sPhotoMap;

    List<String> urls;
    private List<TYPE> types;
    private List<MyMoment> moments;
    private List<Long> photos;

    private List<TYPE> tTypes;
    private List<MyMoment> tMoments;
    private List<Long> tPhotos;

    boolean firstLoaded = true;

    public MomentsAdapter(Context context, int gridCount) {
        super(context, gridCount);
        types = new ArrayList<>();
        moments = new ArrayList<>();
        photos = new ArrayList<>();
        urls = new ArrayList<>();

        tTypes = new ArrayList<>();
        tMoments = new ArrayList<>();
        tPhotos = new ArrayList<>();

        sMoments = new ArrayList<>();
        sPhotoMap = new HashMap<>();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void destroy() {
        BusProvider.getInstance().unregister(this);
    }

    public void clear() {
        sMoments.clear();
        sPhotoMap.clear();
        types.clear();
        moments.clear();
        photos.clear();
        urls.clear();
        selects.clear();
        notifyDataSetChanged();
        setLoading(true);
    }

    /**
     * call from main thread
     * @param _moments
     */
    public void setMoments(final List<MyMoment> _moments) {
        DataRunner.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "setMoments: " + String.valueOf(_moments.size()));
                sMoments.clear();
                sMoments.addAll(_moments);
            }
        });
    }

    /**
     * call from main thread
     * @param momentId
     * @param photos
     */
    public void setMomentPhotos(final long momentId, final List<Long> photos) {
        DataRunner.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                sPhotoMap.remove(momentId);
                sPhotoMap.put(momentId, photos);
            }
        });
    }

    public void buildData(final BuildDataCallback callback) {
        DataRunner.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "buildPositionMap");

                tTypes.clear();
                tMoments.clear();
                tPhotos.clear();
                for (MyMoment m : sMoments) {
                    Long mid = m.id;
                    tTypes.add(TYPE.CONTENT_SPAN);
                    tMoments.add(m);
                    tPhotos.add(null);
                    List<Long> ps = sPhotoMap.get(mid);
                    if (ps != null && ps.size() > 0) {
                        if (ps.size() != m.photosCount) {
                            Log.w(TAG, "shit, moment photosCount is not equal to db photosCount: "
                                    + String.valueOf(m.id) + "/" + String.valueOf(m.photosCount) + "/" + String.valueOf(ps.size()));
                        }
                        for (Long p : ps) {
                            tTypes.add(TYPE.CONTENT);
                            tMoments.add(null);
                            tPhotos.add(p);
                        }
                    } else {
                        // place some dummy data to be placeholder
                        for (int i = 0; i < m.photosCount; i++) {
                            tTypes.add(TYPE.CONTENT);
                            tMoments.add(null);
                            tPhotos.add(-1L);
                        }
                    }
                }
                Log.d(TAG, "data size: " + String.valueOf(tMoments.size()));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        types.clear();
                        types.addAll(tTypes);
                        moments.clear();
                        moments.addAll(tMoments);
                        photos.clear();
                        photos.addAll(tPhotos);
                        urls.clear();
                        for (int i = 0; i < photos.size(); i++) {
                            urls.add(null);
                        }

                        if (firstLoaded) {
                            if (!hasData()) {
                                // do nothing, still loading
                            } else {
                                setLoading(false);
                            }
                            firstLoaded = false;
                        } else {
                            setLoading(false);
                        }

                        notifyDataSetChanged();

                        if (callback != null) {
                            callback.onComplete();
                        }
                    }
                });

            }
        });

    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE.CONTENT_SPAN.ordinal()) {
            return new TitleViewHolder((ViewGroup) mLayoutInflater.inflate(R.layout.grid_title, parent, false));
        } else if (viewType == TYPE.CONTENT.ordinal()) {
            return new PhotoViewHolder((ViewGroup) mLayoutInflater.inflate(R.layout.grid_image, parent, false));
        }
        return null;
    }

    @Override
    protected void onBindContentViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            MyMoment m = moments.get(position);
            if (m == null) {
                return;
            }
            TitleViewHolder h = (TitleViewHolder) holder;
            h.tvTime.setText(DateUtil.formatDate(m.takenAt));
            h.tvPoi.setText(m.locationName);
        } else if (holder instanceof PhotoViewHolder) {
            final Long id = photos.get(position);

            final PhotoViewHolder h = (PhotoViewHolder) holder;
            h.position = position;

            // TODO:mengzheng isVideo
            ThumbnailLoader.getInstance().loadByPhotoId(h.ivPhoto, id, 512, 512, false);

            if (isActionMode) {
                h.checkBox.setVisibility(View.VISIBLE);
                if (selects.get(position)) {
                    h.checkBox.setButtonDrawable(R.drawable.btn_check_on);
                    h.ivPhoto.setScaleX(0.8f);
                    h.ivPhoto.setScaleY(0.8f);
                } else {
                    h.checkBox.setButtonDrawable(R.drawable.btn_check_off);
                    h.ivPhoto.setScaleX(1f);
                    h.ivPhoto.setScaleY(1f);
                }
            } else {
                h.checkBox.setVisibility(View.INVISIBLE);
                h.ivPhoto.setScaleX(1f);
                h.ivPhoto.setScaleY(1f);
            }
        }
    }

    @Override
    protected int getContentItemCount() {
        return types == null ? 0 : types.size();
    }

    @Override
    protected int getContentItemViewType(int position) {
        TYPE type = types.get(position);
        return type == null ? 0 : type.ordinal();
    }

    @Override
    public int getContentSpanSize(int position) {
        TYPE type = types.get(position);
        if (type == null) {
            return 1;
        }
        switch (type) {
            case CONTENT_SPAN:
                return gridCount;
            case CONTENT:
                return 1;
            default:
                return 1;
        }
    }

    public class TitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_time)
        TextView tvTime;
        @BindView(R.id.tv_poi)
        TextView tvPoi;

        TitleViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_photo)
        ImageView ivPhoto;

        @BindView(R.id.checkBox)
        CheckBox checkBox;

        int position = 0;

        public PhotoViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }

        void toggle() {
            if (selects.get(position)) {
                checkBox.setButtonDrawable(R.drawable.btn_check_off);
                ivPhoto.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            } else {
                checkBox.setButtonDrawable(R.drawable.btn_check_on);
                ivPhoto.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).start();
            }
            selects.set(position, !selects.get(position));
        }

        @OnClick(R.id.iv_photo)
        void onItemClick() {
            if (isActionMode) {
                toggle();
            } else {
                List<Long> list = getPhotos();
                Long myP = photos.get(position);

                int pos = 0;
                for (int i = 0; i < list.size(); i++) {
                    Long p = list.get(i);
                    if (p == myP) {
                        pos = i;
                        break;
                    }
                }
                PhotosController.getInstance().setPhotoList(getPhotos());
                PreviewActivity.launch(mContext, pos);
            }
        }

        @OnClick(R.id.checkBox)
        void onCheckClick() {
            toggle();
        }

        @OnLongClick(R.id.iv_photo)
        boolean onItemLongClick() {
            if (isReadOnly) {
                return false;
            }
            if (!isActionMode) {
                startActionMode();
                toggle();
                return true;
            }
            return false;
        }
    }

    @Override
    public void startActionMode() {
        isActionMode = true;
        for (TYPE t : types) {
            selects.add(false);
        }
        BusProvider.getInstance().post(new OnStartActionModeEvent());
        notifyDataSetChanged();
    }

    @Override
    public void finishActionMode() {
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

    public List<Long> getSelected() {
        List<Long> list = new ArrayList<>();
        for (int i = 0; i < selects.size(); i++) {
            if (selects.get(i)) {
                Long p = photos.get(i);
                if (p != null) {
                    list.add(photos.get(i));
                }
            }
        }
        return list;
    }

    public List<Long> getPhotos() {
        List<Long> list = new ArrayList<>();
        for (Long p : photos) {
            if (p != null) {
                list.add(p);
            }
        }
        return list;
    }

    public interface BuildDataCallback {
        void onComplete();
    }

}

