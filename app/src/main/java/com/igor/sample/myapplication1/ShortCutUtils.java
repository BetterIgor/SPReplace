package com.igor.sample.myapplication1;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;

class ShortCutUtils {
    private static final String TAG = "ShortCutUtils";

    public static void addShortCut(Context cxt, String title, int iconRes, ComponentName name, String key) {
        forceAddShortCut(cxt, title, iconRes, name, key, false);
    }

    public static void forceAddShortCut(Context cxt, String title, int iconRes, ComponentName name, String key, boolean force) {

        if (Build.VERSION.SDK_INT >= 26) {

            ShortcutManager shortcutManager = (ShortcutManager) cxt.getSystemService(Context.SHORTCUT_SERVICE);

            if (shortcutManager.isRequestPinShortcutSupported()) {
                Intent shortcutInfoIntent = new Intent();
                shortcutInfoIntent.setComponent(name);
                shortcutInfoIntent.setAction(Intent.ACTION_MAIN);

                ShortcutInfo info = new ShortcutInfo.Builder(cxt, key)
                        .setIcon(Icon.createWithResource(cxt, iconRes))
                        .setShortLabel(title)
                        .setIntent(shortcutInfoIntent)
                        .build();

                //当添加快捷方式的确认弹框弹出来时，将被回调
                PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(cxt, 0, new Intent(cxt, AddShortcutReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.getIntentSender());
            }
        } else {
            Intent intent = new Intent();
            intent.setComponent(name);
            intent.setAction(Intent.ACTION_MAIN);

            // 先删除再添加
            Intent removeIntent = new Intent();
            removeIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            removeIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            removeIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");

            Intent addShortCutIntent = new Intent();
            addShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            addShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            addShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(cxt, iconRes));
            addShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

            if (!force) {
                removeIntent.putExtra("duplicate", false);
                addShortCutIntent.putExtra("duplicate", false);
            }

            // Catch the exceptions.
            try {
                cxt.sendBroadcast(removeIntent);
                cxt.sendBroadcast(addShortCutIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class AddShortcutReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive action: " + intent.getAction());
        }
    }
}
