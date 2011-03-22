package com.announcify.plugin.mail.google.activity;

import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.announcify.api.ui.activity.PluginActivity;
import com.announcify.plugin.mail.google.R;
import com.announcify.plugin.mail.google.service.MailService;
import com.announcify.plugin.mail.google.util.Settings;

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

        addPreferencesFromResource(R.xml.preferences_mail_settings);

        final Cursor message = getContentResolver().query(Uri.parse("content://gmail-ls/labels/" + "tomtasche@gmail.com"), null, null, null, null);
        if (!message.moveToFirst()) {
            Log.e("smn", "failtrain");
            return;
        }
        
        // Log.e("smn", message.getString(message.getColumnIndex("name"))); // equals ^u

        do {
        for (String s : message.getColumnNames()) {
            Log.e("smn", ":" + s);
            Log.e("smn", message.getString(message.getColumnIndex(s)));
        }
        } while (message.moveToNext());
    }

    @Override
    protected void onPause() {
        final Intent serviceIntent = new Intent(this, MailService.class);
        stopService(serviceIntent);
        startService(serviceIntent);

        super.onPause();
    }
}
