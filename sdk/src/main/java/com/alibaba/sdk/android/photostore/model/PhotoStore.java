/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

public class PhotoStore {
    @JSONField(name = "Id")
    public long id;

    @JSONField(name = "Name")
    public String name;

    @JSONField(name = "Remark")
    public String remark;

    @JSONField(name = "AutoCleanEnabled")
    public boolean autoCleanEnabled;

    @JSONField(name = "AutoCleanDays")
    public int autoCleanDays;

    @JSONField(name = "DefaultQuota")
    public long defaultQuota;

    @JSONField(name = "Ctime")
    public long ctime;

    @JSONField(name = "Mtime")
    public long mtime;
}
