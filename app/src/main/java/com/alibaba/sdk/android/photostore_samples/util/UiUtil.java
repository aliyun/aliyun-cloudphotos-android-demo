/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class UiUtil {

    public static int convertDpToPx(Context context, int dp) {
        return Math.round(dp * (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    public static int convertPxToDp(Context context, int px) {
        return Math.round(px / (context.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
