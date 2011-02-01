
package com.announcify.ui.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Groups;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.TextView;

import com.announcify.R;
import com.announcify.api.background.sql.model.GroupModel;
import com.announcify.api.background.util.AnnouncifySettings;

public class GroupActivity extends BaseActivity {

    private CheckedTextView checkBlock;

    private ListView list;

    private AutoCompleteTextView auto;

    private SimpleCursorAdapter listAdapter;

    private SimpleCursorAdapter autoAdapter;

    private GroupModel model;

    private Cursor listCursor;

    private Cursor autoCursor;

    private AnnouncifySettings settings;

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);

        setActionBarContentView(R.layout.activity_choose);

        settings = new AnnouncifySettings(this);

        checkBlock = (CheckedTextView)findViewById(R.id.check_block_groups);
        checkBlock.setChecked(settings.getBlockGroups());
        checkBlock.setOnClickListener(new OnClickListener() {

            public void onClick(final View v) {
                final boolean enable = !checkBlock.isChecked();
                settings.setBlockGroups(enable);
                checkBlock.setChecked(enable);
            }
        });

        model = new GroupModel(this);

        listAdapter = new SimpleWhiteCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[] {
                    GroupModel.KEY_GROUP_TITLE
                }, new int[] {
                    android.R.id.text1
                });

        list = (ListView)findViewById(android.R.id.list);
        list.setAdapter(listAdapter);
        list.setBackgroundColor(Color.WHITE);
        list.setCacheColorHint(Color.TRANSPARENT);
        list.setFastScrollEnabled(true);

        registerForContextMenu(list);

        autoAdapter = new SimpleWhiteCursorAdapter(this, android.R.layout.simple_list_item_1, null,
                new String[] {
                    Groups.TITLE
                }, new int[] {
                    android.R.id.text1
                });

        auto = (AutoCompleteTextView)findViewById(R.id.auto_edit_chooser);
        auto.setSingleLine();
        auto.setAdapter(autoAdapter);
        auto.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
                model.add(autoAdapter.getItemId(position), ((TextView)view).getText().toString());

                refreshList();

                auto.setText("");
            }
        });

        autoAdapter.setCursorToStringConverter(new CursorToStringConverter() {

            public CharSequence convertToString(final Cursor cursor) {
                return cursor.getString(cursor.getColumnIndex(Groups.TITLE));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        refreshList();

        autoCursor = getContentResolver().query(Groups.CONTENT_URI, new String[] {
                BaseColumns._ID, Groups.TITLE
        }, null, null, Groups.TITLE);
        autoAdapter.changeCursor(autoCursor);
    }

    private void refreshList() {
        if (listCursor != null) {
            listCursor.close();
        }

        listCursor = model.getAll();
        listAdapter.changeCursor(listCursor);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.context_choose, menu);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item
                .getMenuInfo();

        if (item.getItemId() == R.id.menu_remove) {
            model.remove(info.id);

            refreshList();
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onStop() {
        listCursor.close();
        autoCursor.close();

        super.onStop();
    }

    private class SimpleWhiteCursorAdapter extends SimpleCursorAdapter {

        public SimpleWhiteCursorAdapter(final Context context, final int layout, final Cursor c,
                final String[] from, final int[] to) {
            super(context, layout, c, from, to);
        }

        @Override
        public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
            final View view = super.newView(context, cursor, parent);
            ((TextView)view.findViewById(android.R.id.text1)).setTextColor(Color.BLACK);
            return view;
        }
    }
}