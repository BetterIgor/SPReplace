package com.igor.sample.myapplication1.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

public class SPUtil {
    private static final String TAG = "SPUtil";

    private static SharedPreferences getEditor(Context context) {
        return context
                .getSharedPreferences("b_sp_files", Context.MODE_PRIVATE);
    }

    public static void putStringWithCommit(Context context, String key, String value) {
        getEditor(context)
                .edit()
                .putString(key, value)
                .commit();
    }

    public static String getString(Context context, String key, String value) {
        String s = getEditor(context)
                .getString(key, value);
        Log.d(TAG, "getString: " + s);
        return s;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void putBooleanWithApply(Context context, String key, boolean value) {
        getEditor(context)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static boolean getBoolean(Context context, String key, boolean value) {
        boolean b = getEditor(context)
                .getBoolean(key, value);
        return b;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void putIntWithApply(Context context, String key, int value) {
        getEditor(context)
                .edit()
                .putInt(key, value)
                .apply();
    }

    public static int getInt(Context context, String key, int value) {
        int b = getEditor(context)
                .getInt(key, value);
        return b;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void putLongWithApply(Context context, String key, long value) {
        getEditor(context)
                .edit()
                .putLong(key, value)
                .apply();
    }

    public static long getLong(Context context, String key, long value) {
        long b = getEditor(context)
                .getLong(key, value);
        return b;
    }

}
