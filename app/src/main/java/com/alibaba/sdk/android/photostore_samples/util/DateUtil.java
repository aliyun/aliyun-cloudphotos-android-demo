/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
    public static String formatDate(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        String s = format1.format(calendar.getTime());
        return s;
    }

    public static long getDayStamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.get(Calendar.YEAR);
        Calendar c = Calendar.getInstance();
        c.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        return c.getTime().getTime() / 1000;
    }
}
