/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.model;

public class User {
    public String accessKeyId = "";
    public String accessKeySecret = "";
    public String stsToken = "";
    public String kp = "";

    private static User instance;

    public static User getInstance() {
        if (instance == null) {
            synchronized (User.class) {
                instance = new User();
            }
        }
        return instance;
    }
}
