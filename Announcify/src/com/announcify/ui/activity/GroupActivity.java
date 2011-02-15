package com.announcify.ui.activity;

import java.util.Locale;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Groups;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;

import com.announcify.api.background.sql.model.GroupModel;


public class GroupActivity extends ChooserActivity {

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        checkBlock.setChecked(settings.getBlockGroups());
        checkBlock.setOnClickListener(new OnClickListener() {

            public void onClick(final View v) {
                final boolean enable = !checkBlock.isChecked();
                settings.setBlockGroups(enable);
                checkBlock.setChecked(enable);
            }
        });

        model = new GroupModel(this);
        
        listAdapter.changeCursorAndColumns(null, new String[] { GroupModel.KEY_GROUP_TITLE },
                new int[] { android.R.id.text1 });

        autoAdapter.setCursorToStringConverter(new CursorToStringConverter() {

            public CharSequence convertToString(final Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex(Groups.TITLE));
            }
        });
        autoAdapter.setFilterQueryProvider(new FilterQueryProvider() {

            public Cursor runQuery(CharSequence constraint) {
                return getContentResolver().query(Groups.CONTENT_URI,
                        new String[] { BaseColumns._ID, Groups.TITLE }, "UPPER(" + Groups.TITLE + ") GLOB ?", new String[] {constraint.toString().toUpperCase(Locale.ENGLISH) + "*"}, Groups.TITLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        autoCursor = getContentResolver().query(Groups.CONTENT_URI,
                new String[] { BaseColumns._ID, Groups.TITLE }, null, null,
                Groups.TITLE);
        autoAdapter.changeCursorAndColumns(autoCursor, new String[] { Groups.TITLE }, new int[] { android.R.id.text1 });
    }
}
