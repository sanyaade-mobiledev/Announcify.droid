
package com.announcify.plugin.message.sms.util;

import android.content.Context;

import com.announcify.api.background.util.PluginSettings;
import com.announcify.plugin.message.sms.R;

public class Settings extends PluginSettings {

    public static final String ACTION_SETTINGS = "com.announcify.plugin.message.sms.SETTINGS";

    public Settings(final Context context) {
        super(context);
    }

    @Override
    public String getEventType() {
        return context.getString(R.string.event);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public String getSettingsAction() {
        return ACTION_SETTINGS;
    }
}