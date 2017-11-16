/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

public class PhotoState {
    @JSONField(name = "PhotoId")
    public long id;

    @JSONField(name = "State")
    public String state;
}
