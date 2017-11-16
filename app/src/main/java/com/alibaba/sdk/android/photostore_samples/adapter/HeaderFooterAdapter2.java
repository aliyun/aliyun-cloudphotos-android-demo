/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.alibaba.sdk.android.photostore_samples.BusProvider;
import com.alibaba.sdk.android.photostore_samples.R;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;
import com.alibaba.sdk.android.photostore_samples.event.OnGoToFragmentEvent;
import com.alibaba.sdk.android.photostore_samples.model.MyFace;
import com.alibaba.sdk.android.photostore_samples.model.MyTag;
import com.alibaba.sdk.android.photostore_samples.util.ThumbnailLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class HeaderFooterAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = HeaderFooterAdapter2.class.getSimpleName();

    private enum TYPE {
        HEADER,
        FOOTER
    }

    protected boolean isActionMode = false;
    protected List<Boolean> selects;

    private List<View> headers = new ArrayList<>();
    private List<View> footers = new ArrayList<>();

    protected LayoutInflater mLayoutInflater;
    protected Context mContext;

    protected int gridCount = 1;

    protected Handler handler = new Handler(Looper.getMainLooper());

    private ProgressBar progressBar;
    private ImageView ivEmpty;

    private List<MyFace> facesPeek = new ArrayList<>();
    private List<MyTag> tagsPeek = new ArrayList<>();

    public HeaderFooterAdapter2(Context context, int gridCount) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.gridCount = gridCount;
    }

    public void addHeader(View header) {
        headers.add(header);
        notifyDataSetChanged();
    }

    public void addFooter(View footer) {
        footers.add(footer);
        notifyDataSetChanged();
    }

    public void showProgressBar(boolean show) {
        if (progressBar != null) {
            if (show) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    public void setFacesPeek(List<MyFace> list) {
        facesPeek = list;
        notifyDataSetChanged();
    }

    public void setTagsPeek(List<MyTag> list) {
        tagsPeek = list;
        notifyDataSetChanged();
    }

    protected void setLoading(boolean loading) {
        Log.d(TAG, "setLoading:" + String.valueOf(loading));
        if (progressBar != null)
            progressBar.setVisibility(View.INVISIBLE);
        if (ivEmpty != null)
            ivEmpty.setVisibility(View.INVISIBLE);
//        if (progressBar != null) {
//            if (loading) {
//                progressBar.setVisibility(View.VISIBLE);
//            } else {
//                progressBar.setVisibility(View.INVISIBLE);
//            }
//        }
//        if (ivEmpty != null) {
//            if (loading) {
//                ivEmpty.setVisibility(View.INVISIBLE);
//            } else {
//                if (hasData()) {
//                    ivEmpty.setVisibility(View.INVISIBLE);
//                } else {
//                    ivEmpty.setVisibility(View.VISIBLE);
//                }
//            }
//        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE.HEADER.ordinal()) {
            return new CollectionsViewHolder((ViewGroup) mLayoutInflater.inflate(R.layout.collections, parent, false));
        } else if (viewType == TYPE.FOOTER.ordinal()) {
            ViewGroup footerView = (ViewGroup) mLayoutInflater.inflate(R.layout.empty_footer, parent, false);
            progressBar = (ProgressBar) footerView.findViewById(R.id.pb);
            ivEmpty = (ImageView) footerView.findViewById(R.id.iv_empty);
            return new FooterViewHolder(footerView);
        } else {
            return onCreateContentViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position >= headers.size() && position < getContentItemCount() + headers.size()) {
            onBindContentViewHolder(holder, position - headers.size());
        } else if (holder instanceof CollectionsViewHolder) {
            CollectionsViewHolder h = (CollectionsViewHolder) holder;
            h.setFacesPeek(facesPeek);
            h.setTagsPeek(tagsPeek);
        }
    }

    @Override
    public int getItemCount() {
        return headers.size() + footers.size() + getContentItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < headers.size()) {
            return TYPE.HEADER.ordinal();
        } else if (position >= getContentItemCount() + headers.size()) {
            return TYPE.FOOTER.ordinal();
        } else {
            return getContentItemViewType(position - headers.size());
        }
    }

    public int getSpanSize(int position) {
        if (position < headers.size()) {
            return gridCount;
        } else if (position >= getContentItemCount() + headers.size()) {
            return gridCount;
        } else {
            return getContentSpanSize(position - headers.size());
        }
    }

    public abstract void destroy();

    protected abstract RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position);

    protected abstract int getContentItemCount();

    protected abstract int getContentItemViewType(int position);

    protected abstract int getContentSpanSize(int position);

    protected abstract void startActionMode(int position);

    protected abstract void finishActionMode();

    protected boolean hasData() {
        return getContentItemCount() > 0;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        HeaderViewHolder(View view) {
            super(view);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        FooterViewHolder(View view) {
            super(view);
        }
    }

    public class CollectionsViewHolder extends RecyclerView.ViewHolder {
        @BindViews({R.id.iv_face0, R.id.iv_face1, R.id.iv_face2, R.id.iv_face3})
        List<ImageView> ivFaces;

        @BindViews({R.id.iv_tag0, R.id.iv_tag1, R.id.iv_tag2, R.id.iv_tag3})
        List<ImageView> ivTags;

        @BindViews({R.id.iv_place0, R.id.iv_place1, R.id.iv_place2, R.id.iv_place3})
        List<ImageView> ivPlaces;

        @BindViews({R.id.iv_thing0, R.id.iv_thing1, R.id.iv_thing2, R.id.iv_thing3})
        List<ImageView> ivThings;

        @BindViews({R.id.iv_collage0, R.id.iv_collage1, R.id.iv_collage2, R.id.iv_collage3})
        List<ImageView> ivCollages;

        CollectionsViewHolder(ViewGroup viewGroup) {
            super(viewGroup);
            ButterKnife.bind(this, viewGroup);
        }

        @OnClick(R.id.ll_faces)
        public void onFacesClick() {
            BusProvider.getInstance().post(new OnGoToFragmentEvent(FragmentType.FACES));
        }

        @OnClick(R.id.ll_tags)
        public void onTagsClick() {
            BusProvider.getInstance().post(new OnGoToFragmentEvent(FragmentType.TAGS));
        }

        public void setFacesPeek(List<MyFace> list) {
            MyFace o;
            for (int i = 0; i < list.size() && i < 4; i++) {
                o = list.get(i);
                ThumbnailLoader.getInstance().loadCropByPhotoId(ivFaces.get(i), o.coverPhotoId, 1024, 1024, false,
                        o.axisLeft, o.axisTop, o.axisRight, o.axisBottom, o.coverWidth, o.coverHeight);
            }
        }

        public void setTagsPeek(List<MyTag> list) {
            if (list != null) {
                MyTag o;
                for (int i = 0; i < list.size() && i < 4; i++) {
                    o = list.get(i);
                    ThumbnailLoader.getInstance().loadByPhotoId(ivTags.get(i), o.coverPhotoId, 512, 512, false);
                }
            }
        }

    }
}
