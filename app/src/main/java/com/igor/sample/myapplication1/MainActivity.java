package com.igor.sample.myapplication1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.igor.sample.mylibrary.Util;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        setContentView(R.layout.activity_main);

        Util.putStringWithCommit(context, "hello_s", "world");
        Util.getString(context, "hello_s", "");

        Util.putBooleanWithApply(context, "hello_b", true);
        Util.getBoolean(context, "hello_b", false);

        Util.putIntWithApply(context, "hello_i", 1);
        Util.getInt(context, "hello_i", 0);

        Util.putLongWithApply(context, "hello_l", 1);
        Util.getLong(context, "hello_l", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        checkAutoAddShortcut();
    }


//    private void checkAutoAddShortcut() {
//        ShortCutUtils.addShortCut(getApplicationContext(), "title",
//                R.drawable.ic_launcher, new ComponentName(this, MainActivity.class), "one_tap_clean_shortcut");
//    }
}