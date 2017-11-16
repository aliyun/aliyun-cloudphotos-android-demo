/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class MomentWithPhotos {
    @JSONField(name = "LastUpdateAt")
    public long updatedAt;

    @JSONField(name = "TotalCount")
    public int totalCount;

    @JSONField(name = "List")
    public List<Photo> photos;
}
