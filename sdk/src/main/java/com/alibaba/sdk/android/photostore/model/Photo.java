/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

import com.alibaba.fastjson.annotation.JSONField;

public class Photo {
    @JSONField(name = "Id")
    public long id;

    @JSONField(name = "Title")
    public String title;

    @JSONField(name = "Remark")
    public String remark;

    @JSONField(name = "FileId")
    public String fileId;

    @JSONField(name = "Ctime")
    public long ctime;

    @JSONField(name = "Mtime")
    public long mtime;

    @JSONField(name = "TakenAt")
    public long takenAt;

    @JSONField(name = "Width")
    public int width;

    @JSONField(name = "Height")
    public int height;

    @JSONField(name = "State")
    public String state;

    @JSONField(name = "Md5")
    public String md5;

    @JSONField(name = "IsVideo")
    public boolean isVideo;

    @JSONField(name = "ShareExpireTime")
    public long shareExpireTime;

    @JSONField(name = "InactiveTime")
    public long inactiveTime;
}
