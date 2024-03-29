
package com.announcify.plugin.calendar.google.util;

import android.content.Context;

import com.announcify.api.util.PluginSettings;

public class Settings extends PluginSettings {
    
    public static final String PREFERENCES_NAME = "com.announcify.plugin.calendar.google.SETTINGS";

    
    public Settings(final Context context) {
        super(context, PREFERENCES_NAME);
    }

    
    @Override
    public String getEventType() {
        return "Appointment";
    }

    @Override
    public int getPriority() {
        return 7;
    }
}
