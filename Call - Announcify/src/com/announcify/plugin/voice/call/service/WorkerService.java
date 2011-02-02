
package com.announcify.plugin.voice.call.service;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.TelephonyManager;

import com.announcify.api.AnnouncifyIntent;
import com.announcify.api.background.contact.Contact;
import com.announcify.api.background.contact.Filter;
import com.announcify.api.background.service.PluginService;
import com.announcify.api.background.text.Formatter;
import com.announcify.api.background.util.AnnouncifySettings;
import com.announcify.api.background.util.PluginSettings;
import com.announcify.plugin.voice.call.util.Settings;

public class WorkerService extends PluginService {

    public final static String ACTION_START_RINGTONE = "com.announcify.plugin.voice.call.ACTION_START_RINGTONE";

    public final static String ACTION_STOP_RINGTONE = "com.announcify.plugin.voice.call.ACTION_STOP_RINGTONE";

    public WorkerService() {
        super("Announcify - Call", ACTION_START_RINGTONE, ACTION_STOP_RINGTONE);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (settings == null) {
            settings = new Settings(this);
        }

        if (ACTION_ANNOUNCE.equals(intent.getAction())) {
            final String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (number == null && "".equals(number)) {
                return;
            }
            final Contact contact = new Contact(this,
                    new com.announcify.api.background.contact.lookup.Number(this), number);

            if (!Filter.announcableContact(this, contact)) {
                return;
            }

            final Formatter formatter = new Formatter(this, contact, settings);

            final AnnouncifyIntent announcify = new AnnouncifyIntent(this, settings);
            announcify.setStartBroadcast(ACTION_START_RINGTONE);
            announcify.setStopBroadcast(ACTION_STOP_RINGTONE);
            announcify.announce(formatter.format(null));
        } else if (ACTION_START_RINGTONE.equals(intent.getAction())) {
            final String s = getSharedPreferences(Settings.PREFERENCES_NAME,
                    Context.MODE_WORLD_READABLE).getString(PluginSettings.KEY_RINGTONE, "");
            if (s == null || "".equals(s)) {
                return;
            }
            final RingtoneManager manager = new RingtoneManager(this);
            manager.setType(RingtoneManager.TYPE_RINGTONE);
            ringtone = manager.getRingtone(manager.getRingtonePosition(Uri.parse(s)));
            if (ringtone == null) {
                return;
            }
            ringtone.setStreamType(new AnnouncifySettings(this).getStream());
            ringtone.play();
        } else if (ACTION_STOP_RINGTONE.equals(intent.getAction())) {
            if (ringtone != null) {
                ringtone.stop();
                ringtone = null;
            }
        } else {
            super.onHandleIntent(intent);
        }
    }
}
