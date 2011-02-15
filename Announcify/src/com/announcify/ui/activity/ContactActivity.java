package com.announcify.ui.activity;

import java.util.Locale;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;

import com.announcify.api.background.sql.model.ContactModel;


public class ContactActivity extends ChooserActivity {

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        checkBlock.setChecked(settings.getBlockContacts());
        checkBlock.setOnClickListener(new OnClickListener() {

            public void onClick(final View v) {
                final boolean enable = !checkBlock.isChecked();
                settings.setBlockContacts(enable);
                checkBlock.setChecked(enable);
            }
        });

        model = new ContactModel(this);

        listAdapter.changeCursorAndColumns(null, new String[] { ContactModel.KEY_CONTACT_TITLE },
                new int[] { android.R.id.text1 });

        autoAdapter.setCursorToStringConverter(new CursorToStringConverter() {

            public CharSequence convertToString(final Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex(Contacts.DISPLAY_NAME));
            }
        });
        autoAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            public Cursor runQuery(CharSequence constraint) {
                return getContentResolver().query(Contacts.CONTENT_URI,
                        new String[] { Contacts._ID, Contacts.DISPLAY_NAME }, "UPPER(" + Contacts.DISPLAY_NAME + ") GLOB ?", new String[] {constraint.toString().toUpperCase(Locale.ENGLISH) + "*"}, Contacts.DISPLAY_NAME);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        autoCursor = getContentResolver().query(Contacts.CONTENT_URI,
                new String[] { Contacts._ID, Contacts.DISPLAY_NAME }, null, null,
                Contacts.DISPLAY_NAME);
        autoAdapter.changeCursorAndColumns(autoCursor, new String[] { Contacts.DISPLAY_NAME }, new int[] { android.R.id.text1 });
    }
}