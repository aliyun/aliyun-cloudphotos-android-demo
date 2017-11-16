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

import com.alibaba.sdk.android.photostore_samples.R;

import java.util.ArrayList;
import java.util.List;

public abstract class HeaderFooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = HeaderFooterAdapter.class.getSimpleName();

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

    boolean isReadOnly;

    public HeaderFooterAdapter(Context context, int gridCount) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.gridCount = gridCount;

        selects = new ArrayList<>();
    }

    public void addHeader(View header) {
        headers.add(header);
        notifyDataSetChanged();
    }

    public void addFooter(View footer) {
        footers.add(footer);
        notifyDataSetChanged();
    }

    protected void setLoading(boolean loading) {
        Log.d(TAG, "setLoading:" + String.valueOf(loading));
        if (progressBar != null) {
            if (loading) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
        if (ivEmpty != null) {
            if (loading) {
                ivEmpty.setVisibility(View.INVISIBLE);
            } else {
                if (hasData()) {
                    ivEmpty.setVisibility(View.INVISIBLE);
                } else {
                    ivEmpty.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE.HEADER.ordinal()) {
            return new HeaderViewHolder(mLayoutInflater.inflate(R.layout.empty_header, parent, false));
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
        } else {
            // nothing
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

    protected abstract void startActionMode();

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
}
