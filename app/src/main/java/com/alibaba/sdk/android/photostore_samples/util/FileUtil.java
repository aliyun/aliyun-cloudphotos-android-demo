/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileUtil {

    public static String getFileName(File f) {
        String[] ss = f.getName().split("\\.");
        String name = "";
        String ext = "";
        if (ss.length >= 2) {
            ext = ss[ss.length - 1];
            for (int i = 0; i < ss.length - 1; i++) {
                name += ss[i];
            }
        } else {
            name = f.getName();
        }
        return name;
    }

    public static String getFileExt(File f) {
        String[] ss = f.getName().split("\\.");
        String name = "";
        String ext = "";
        if (ss.length >= 2) {
            ext = ss[ss.length - 1];
            for (int i = 0; i < ss.length - 1; i++) {
                name += ss[i];
            }
        } else {
            name = f.getName();
        }
        return ext;
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
