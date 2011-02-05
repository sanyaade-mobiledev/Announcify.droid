package com.announcify.ui.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

import com.announcify.R;
import com.announcify.api.background.util.AnnouncifySettings;
import com.announcify.api.ui.activity.PluginActivity;
import com.announcify.background.util.AnnouncifySecurity;


public class SettingsActivity extends PluginActivity {

    private AnnouncifySecurity security;

    private boolean licensed;

    private boolean started;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getPreferenceManager().setSharedPreferencesName(
                AnnouncifySettings.PREFERENCES_NAME);
        getPreferenceManager().setSharedPreferencesMode(
                Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.preferences_main_settings);

        getPreferenceScreen().findPreference("preference_replace_chooser")
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(final Preference preference) {
                startActivity(new Intent(SettingsActivity.this,
                        ReplaceActivity.class));

                return false;
            }
        });

        getPreferenceScreen().findPreference("preference_choose_group")
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(final Preference preference) {
                startActivity(new Intent(SettingsActivity.this,
                        GroupActivity.class));

                return false;
            }
        });

        getPreferenceScreen().findPreference("preference_spam_filter")
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(final Preference preference) {
                Toast.makeText(SettingsActivity.this,
                        "Not yet implemented, sorry!",
                        Toast.LENGTH_LONG).show();

                return false;
            }
        });

        getPreferenceScreen().findPreference("preference_tts_settings")
        .setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(final Preference preference) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
                startActivity(intent);

                return false;
            }
        });        

        // ugly fix for bug #4611
        // https://code.google.com/p/android/issues/detail?id=4611
        for (int i = 1; i < 5; i++) {
            applyThemeProtection("screen" + i);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.unlicensed_dialog_title)
                .setMessage(R.string.unlicensed_dialog_body)
                .setPositiveButton(R.string.buy_button,
                        new DialogInterface.OnClickListener() {

                    public void onClick(
                            final DialogInterface dialog,
                            final int which) {
                        final Intent marketIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("http://market.android.com/details?id="
                                        + getPackageName()));
                        startActivity(marketIntent);
                    }
                })
                .setNegativeButton(R.string.quit_button,
                        new DialogInterface.OnClickListener() {

                    public void onClick(
                            final DialogInterface dialog,
                            final int which) {
                        finish();
                    }
                }).create();

            case 1:
                return ProgressDialog.show(this, "Announcify",
                        "Verifying if you really bought Pro...", true);

            case 2:
                started = true;
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        if (security != null) {
            // security.quit();
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (security != null) {
            // licensed = security.isLicensed();
            // security.quit();
        } else {
            licensed = false;
        }

        if (licensed) {
            final Editor editor = getPreferences(MODE_WORLD_READABLE).edit();
            editor.clear();
            editor.commit();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (started && !licensed) {
            showDialog(0);
        }

        super.onResume();
    }
}
