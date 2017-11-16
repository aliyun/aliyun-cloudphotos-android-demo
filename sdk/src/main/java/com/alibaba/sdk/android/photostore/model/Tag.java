/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

public class Tag {
    @JSONField(name = "Id")
    public long id;

    @JSONField(name = "Name")
    public String name;

    @JSONField(name = "ParentTag")
    public String parentTag;

    @JSONField(name = "IsSubTag")
    public boolean isSubTag;

    @JSONField(name = "Cover")
    public Photo cover;
}
