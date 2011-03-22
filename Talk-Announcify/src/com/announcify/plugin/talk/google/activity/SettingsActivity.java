package com.announcify.plugin.talk.google.activity;

import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.announcify.api.R;
import com.announcify.api.ui.activity.PluginActivity;
import com.announcify.plugin.talk.google.service.TalkService;
import com.announcify.plugin.talk.google.util.Settings;

public class SettingsActivity extends PluginActivity {

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        parseRingtone(requestCode, resultCode, data,
                RingtoneManager.TYPE_NOTIFICATION);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, new Settings(this),
                R.xml.preferences_settings);
        
        final Cursor message = getContentResolver().query(Uri.withAppendedPath(Uri.parse("content://com.google.android.providers.talk/"), "messages"), null, "err_code = 0", null, "date DESC");
        if (!message.moveToFirst()) {
            return;
        }
        
        Log.e("smn", message.getInt(message.getColumnIndex("type")) + ":");
        Log.e("smn", message.getString(message.getColumnIndex("body")) + ";");
        Log.e("smn", message.getString(message.getColumnIndex("nickname")) + ".");
    }

    @Override
    protected void onPause() {
        final Intent serviceIntent = new Intent(this, TalkService.class);
        stopService(serviceIntent);
        startService(serviceIntent);

        super.onPause();
    }
}
