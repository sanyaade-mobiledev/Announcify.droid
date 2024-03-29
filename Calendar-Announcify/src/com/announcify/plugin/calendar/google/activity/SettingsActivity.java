
package com.announcify.plugin.calendar.google.activity;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.text.format.Time;

import com.announcify.api.activity.PluginActivity;
import com.announcify.plugin.calendar.google.R;
import com.announcify.plugin.calendar.google.util.Settings;

public class SettingsActivity extends PluginActivity {
    public static final String ACTION_SETTINGS = "com.announcify.plugin.calendar.google.SETTINGS";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Settings.PREFERENCES_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preferences_settings);

        // Cursor cursor =
        // getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"),
        // null, null, null, null);
        // cursor.moveToFirst();
        //
        // for (String s : cursor.getColumnNames()) {
        // try {
        // Log.e("smn", s);
        // Log.e("smn", cursor.getString(cursor.getColumnIndex(s)));
        // } catch (Exception e) {}
        // }

        // Cursor cursor =
        // getContentResolver().query(Uri.parse("content://com.android.calendar/reminders"),
        // null, null, null, null);
        // cursor.moveToFirst();
        //
        // for (String s : cursor.getColumnNames()) {
        // try {
        // Log.e("smn", s);
        // Log.e("smn", cursor.getString(cursor.getColumnIndex(s)));
        // } catch (Exception e) {}
        // }
        
        // https://thinkandroid.wordpress.com/2010/06/20/sync-ing-with-androids-calendar/
        // content://com.android.calendar/events

        new Time().setToNow();

        new Time().set(Long.valueOf("1267920000000"));
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        parseRingtone(requestCode, resultCode, data, RingtoneManager.TYPE_NOTIFICATION);

        super.onActivityResult(requestCode, resultCode, data);
    }
}
