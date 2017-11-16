/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.event;

import com.alibaba.sdk.android.photostore_samples.constants.ContentType;
import com.alibaba.sdk.android.photostore_samples.constants.FragmentType;

public class OnGoToFragmentEvent {
    public FragmentType whatFragment;
    public ContentType whatContent;
    public long id;
    public String query;
    public int cols;

    public OnGoToFragmentEvent(FragmentType f) {
        this(f, null);
    }

    public OnGoToFragmentEvent(FragmentType f, ContentType c) {
        this.whatFragment = f;
        this.whatContent = c;
    }

    public static OnGoToFragmentEvent build(FragmentType f, ContentType c) {
        return new OnGoToFragmentEvent(f, c);
    }

    public OnGoToFragmentEvent query(String query) {
        this.query = query;
        return this;
    }

    public OnGoToFragmentEvent id(long id) {
        this.id = id;
        return this;
    }

    public OnGoToFragmentEvent cols(int cols) {
        this.cols = cols;
        return this;
    }
}
