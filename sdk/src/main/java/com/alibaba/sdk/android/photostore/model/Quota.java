/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

public class Quota {
    @JSONField(name = "UsedQutoa")
    public long usedQutoa;

    @JSONField(name = "TotalQuota")
    public long totalQuota;

    @JSONField(name = "FacesCount")
    public int facesCount;

    @JSONField(name = "PhotosCount")
    public int photosCount;

    @JSONField(name = "VideosCount")
    public int videosCount;
}
