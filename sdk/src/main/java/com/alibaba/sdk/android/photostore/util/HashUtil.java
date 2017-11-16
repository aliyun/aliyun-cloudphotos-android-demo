/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    public static String getStringMd5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(s.getBytes());

            return toHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileMd5(File file) {
        if (file == null || !file.isFile()) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            FileInputStream in = new FileInputStream(file);
            byte buffer[] = new byte[1024];
            int len;
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, len);
            }
            in.close();

            return toHexString(md.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length << 1);
        for (int i = 0; i < b.length; i++) {
            sb.append(Character.forDigit((b[i] & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(b[i] & 0x0f, 16));
        }
        return sb.toString();
    }
}
