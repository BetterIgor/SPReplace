package com.igor.sample.myapplication1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        setContentView(R.layout.activity_main);

//        SPUtil.putStringWithCommit(context, "hello_s", "world");
//        SPUtil.getString(context, "hello_s", "");
//
//        SPUtil.putBooleanWithApply(context, "hello_b", true);
//        SPUtil.getBoolean(context, "hello_b", false);
//
//        SPUtil.putIntWithApply(context, "hello_i", 1);
//        SPUtil.getInt(context, "hello_i", 0);
//
//        SPUtil.putLongWithApply(context, "hello_l", 1);
//        SPUtil.getLong(context, "hello_l", 0);
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