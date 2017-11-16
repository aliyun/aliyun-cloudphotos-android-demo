/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;
import com.alibaba.sdk.android.photostore_samples.event.OnCreateAlbumEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnFinishActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnGoToFragmentEvent;
import com.alibaba.sdk.android.photostore_samples.event.OnStartActionModeEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyAlbum;
import com.alibaba.sdk.android.photostore_samples.model.MyPhoto;
import com.alibaba.sdk.android.photostore_samples.util.DataRunner;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class AlbumsAdapter extends HeaderFooterAdapter {

    private static final String TAG = AlbumsAdapter.class.getSimpleName();

    private enum TYPE {
        HEADER,
        FOOTER,
        CONTENT_SPAN,
        CONTENT
    }

    private List<MyAlbum> sAlbums;
    private List<MyAlbum> albums;
    private List<MyAlbum> tAlbums;

    boolean firstLoaded = true;

    public AlbumsAdapter(Context context, int gridCount) {
        super(context, gridCount);
        albums = new ArrayList<>();
        tAlbums = new ArrayList<>();
        sAlbums = new ArrayList<>();

        BusProvider.getInstance().register(this);
    }

    @Override
    public void destroy() {
        BusProvider.getInstance().unregister(this);
    }

    public void clear() {
        sAlbums.clear();
        albums.clear();
        selects.clear();
        notifyDataSetChanged();
        setLoading(true);
    }

    /**
     * call from main thread
     * @param _albums
     */
    public void setAlbums(final List<MyAlbum> _albums) {
        DataRunner.getInstance().execute(new Runnable() {
            @Override
            public void run() {
//                Log.d(TAG, "setAlbums: " + String.valueOf(_albums.size()));
                sAlbums.clear();
                MyAlbum album = new MyAlbum(-1);
                sAlbums.add(album);
                sAlbums.addAll(_albums);
            }
        });
    }

    public void buildData(final BuildDataCallback callback) {
        DataRunner.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "buildPositionMap");

                tAlbums.clear();
                for (MyAlbum m : sAlbums) {
                    Long mid = m.id;
                    tAlbums.add(m);
                }
                Log.d(TAG, "data size: " + String.valueOf(tAlbums.size()));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        albums.clear();
                        albums.addAll(tAlbums);

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
            return new PhotoViewHolder((ViewGroup) mLayoutInflater.inflate(R.layout.grid_album, parent, false));
        }
        return null;
    }

    @Override
    protected void onBindContentViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
        } else if (holder instanceof PhotoViewHolder) {
            final PhotoViewHolder h = (PhotoViewHolder) holder;
            h.position = position;
            MyAlbum m = albums.get(position);
            if (m == null) {
                return;
            }

            boolean isVideo = false;

            if (m.id == -1) {
                h.ivMask.setVisibility(View.INVISIBLE);
                h.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                h.ivPhoto.setImageResource(R.drawable.ic_album_add);
                h.ivName.setTextColor(Color.BLACK);
                h.ivName.setText(R.string.action_add);
                h.checkBox.setVisibility(View.INVISIBLE);
                h.ivPhoto.setScaleX(1f);
                h.ivPhoto.setScaleY(1f);
            }
            else {
                h.ivMask.setVisibility(View.VISIBLE);
                h.ivName.setTextColor(Color.WHITE);
                h.ivName.setText(m.name);
                h.ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ThumbnailLoader.getInstance().loadByPhotoId(h.ivPhoto, m.coverPhotoId, 512, 512, isVideo);

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
        }
    }

    @Override
    protected int getContentItemCount() {
        return albums == null ? 0 : albums.size();
    }

    @Override
    protected int getContentItemViewType(int position) {
        return TYPE.CONTENT.ordinal();
    }

    @Override
    public int getContentSpanSize(int position) {
        return 1;
    }

    class AlbumComparator implements Comparator<MyAlbum> {

        @Override
        public int compare(MyAlbum o1, MyAlbum o2) {
            return o1.mtime < o2.mtime ? -1 : 1;
        }
    }

    public class TitleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_time)
        TextView tvTime;

        TitleViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }
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
                ivMask.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                ivPhoto.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            } else {
                checkBox.setButtonDrawable(R.drawable.btn_check_on);
                ivMask.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).start();
                ivPhoto.animate().scaleX(0.8f).scaleY(0.8f).setDuration(100).start();
            }
            selects.set(position, !selects.get(position));
        }

        @OnClick(R.id.iv_mask)
        void onItemClick() {
            if (isActionMode) {
                toggle();
            } else {
                if (position == 0) {
                    BusProvider.getInstance().post(new OnCreateAlbumEvent(0, ""));
                }
                else {
                    BusProvider.getInstance().post(
                            OnGoToFragmentEvent.build(FragmentType.ALBUM_PHOTOS, ContentType.ALBUM_PHOTO).id(albums.get(position).id));

                }
            }
        }

        @OnClick(R.id.iv_photo)
        void onAddClick() {
            if (!isActionMode && position == 0) {
                BusProvider.getInstance().post(new OnCreateAlbumEvent(0, ""));
            }
        }

        @OnClick(R.id.checkBox)
        void onCheckClick() {
            toggle();
        }

        @OnLongClick(R.id.iv_mask)
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
        for (MyAlbum a : albums) {
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

    public List<MyAlbum> getSelected() {
        List<MyAlbum> list = new ArrayList<>();
        for (int i = 1; i < selects.size(); i++) {
            if (selects.get(i)) {
                MyAlbum p = albums.get(i);
                if (p != null) {
                    list.add(new MyAlbum(albums.get(i)));
                }
            }
        }
        return list;
    }

    public List<MyAlbum> getAlbums() {
        List<MyAlbum> list = new ArrayList<>();
        int i = 1;
        for (MyAlbum a : albums) {
            if (a != null && i > 1) {
                list.add(a);
            }
            ++ i;
        }
        return list;
    }

    public interface BuildDataCallback {
        void onComplete();
    }

}

