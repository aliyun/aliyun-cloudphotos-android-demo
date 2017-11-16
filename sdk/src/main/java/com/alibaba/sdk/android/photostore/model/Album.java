/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

public class Album {
    @JSONField(name = "Id")
    public long id;

    @JSONField(name = "Name")
    public String name;

    @JSONField(name = "State")
    public String state;

    @JSONField(name = "PhotosCount")
    public int photoCount;

    @JSONField(name = "Cover")
    public Photo cover;

    @JSONField(name = "Ctime")
    public long ctime;

    @JSONField(name = "Mtime")
    public long mtime;

    @JSONField(name = "Remark")
    public String remark;
}
