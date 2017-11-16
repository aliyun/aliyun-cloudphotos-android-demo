/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

public class Moment {
    @JSONField(name = "Id")
    public long id;

    @JSONField(name = "LocationName")
    public String locationName;

    @JSONField(name = "TakenAt")
    public  long takenAt;

    @JSONField(name = "Ctime")
    public long ctime;

    @JSONField(name = "Mtime")
    public long mtime;

    @JSONField(name = "State")
    public String state;

    @JSONField(name = "PhotosCount")
    public int photoCount;
}
