/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore_samples.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.sdk.android.photostore_samples.constants.Constants;

public class PreferenceManager {

    public static SharedPreferences getSharedPref(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPref;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences sharedPref = getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPref = getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void putLong(Context context, String key, long value) {
        SharedPreferences sharedPref = getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void putFloat(Context context, String key, float value) {
        SharedPreferences sharedPref = getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences sharedPref = getSharedPref(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void clear(Context context) {
        SharedPreferences sharedPreferences = getSharedPref(context);
        sharedPreferences.edit().clear().commit();
    }
}
