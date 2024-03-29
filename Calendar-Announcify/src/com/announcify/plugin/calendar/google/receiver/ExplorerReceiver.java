
package com.announcify.plugin.calendar.google.receiver;

import android.content.Context;
import android.content.Intent;

import com.announcify.api.receiver.PluginReceiver;
import com.announcify.plugin.calendar.google.activity.SettingsActivity;
import com.announcify.plugin.calendar.google.util.Settings;

public class ExplorerReceiver extends PluginReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final Settings settings = new Settings(context);

        final Intent respondIntent = new Intent(ACTION_PLUGIN_RESPOND);
        respondIntent.putExtra(EXTRA_PLUGIN_NAME, settings.getEventType());
        respondIntent.putExtra(EXTRA_PLUGIN_ACTION, SettingsActivity.ACTION_SETTINGS);
        respondIntent.putExtra(EXTRA_PLUGIN_BROADCAST, false);
        respondIntent.putExtra(EXTRA_PLUGIN_PACKAGE, context.getPackageName());
        respondIntent.putExtra(EXTRA_PLUGIN_PRIORITY, settings.getPriority());
        context.sendBroadcast(respondIntent);
    }
}
