package com.announcify.plugin.twitter.twidroyd.activity;

import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;

import com.announcify.api.ui.activity.PluginActivity;
import com.announcify.plugin.twitter.twidroyd.R;
import com.announcify.plugin.twitter.twidroyd.util.Settings;

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
		addPreferencesFromResource(R.xml.preferences_more_settings);
		setExtraCustomListeners();
	}
}
