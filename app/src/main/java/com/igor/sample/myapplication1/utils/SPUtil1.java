package com.igor.sample.myapplication1.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class SPUtil1 {
    private static final String TAG = "SPUtil1";

    public static SharedPreferences getEditor(Context context, String name) {
        return context
                .getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void putStringWithCommit(Context context, String key, String value, String name) {
        getEditor(context, name)
                .edit()
                .putString(key, value + "_value")
                .commit();
    }

    public static String getString(Context context, String key, String value, String name) {
        String s = getEditor(context, name)
                .getString(key, value);
        Log.d(TAG, "getString res: " + s);
        return s;
    }

    public static boolean getBoolean(Context context, String key, boolean value, String name) {
        boolean b = getEditor(context, name)
                .getBoolean(key, value);
        Log.d(TAG, "getBoolean: " + b);
        return b;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void putBooleanWithApply(Context context, String key, boolean value, String name) {
        getEditor(context, name)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void putIntWithApply(Context context, String key, int value, String name) {
        getEditor(context, name)
                .edit()
                .putInt(key, value + 100)
                .apply();
    }

    public static int getInt(Context context, String key, int value, String name) {
        int b = getEditor(context, name)
                .getInt(key, value);
        Log.d(TAG, "getInt: " + b);
        return b;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void putLongWithApply(Context context, String key, long value, String name) {
        getEditor(context, name)
                .edit()
                .putLong(key, value + 100)
                .apply();
    }

    public static long getLong(Context context, String key, long value, String name) {
        long b = getEditor(context, name)
                .getLong(key, value);
        Log.d(TAG, "getLong: " + b);
        return b;
    }
}
