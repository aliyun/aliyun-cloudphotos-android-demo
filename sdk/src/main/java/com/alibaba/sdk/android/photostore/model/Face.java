/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;

public class Face {
    @JSONField(name = "Id")
    public long id;
    @JSONField(name = "Name")
    public String name;

    @JSONField(name = "IsMe")
    public boolean isMe;

    @JSONField(name = "Ctime")
    public long ctime;

    @JSONField(name = "Mtime")
    public long mtime;

    @JSONField(name = "PhotosCount")
    public int photosCount;

    @JSONField(name = "State")
    public String state;

    @JSONField(name = "Cover")
    public Photo cover;

    @JSONField(name = "Axis")
    public JSONArray axis;
}
