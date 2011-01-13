
package com.announcify.activity;

import greendroid.app.GDListActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.item.Item;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.announcify.R;
import com.announcify.activity.widget.PluginItem;
import com.announcify.api.receiver.PluginReceiver;
import com.announcify.api.sql.model.PluginModel;

public class AnnouncifyActivity extends GDListActivity {
    private PluginModel model;

    private PluginExplorer explorer;

    private SectionedItemAdapter adapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        setTitle("Announcify...");

        super.onCreate(savedInstanceState);

        final ImageButton button = (ImageButton)findViewById(R.id.gd_action_bar_home_item);
        button.setImageDrawable(getResources().getDrawable(R.drawable.launcher_icon));

        getListView().setBackgroundColor(Color.WHITE);
        getListView().setCacheColorHint(Color.TRANSPARENT);
        getListView().setFastScrollEnabled(true);

        // if (Build.VERSION.SDK_INT >= 9) {
        // getListView().setOverScrollMode(ListView.OVER_SCROLL_ALWAYS);
        // getListView().setOverscrollHeader(getResources().getDrawable(R.drawable.launcher_icon));
        // getListView().setOverscrollFooter(getResources().getDrawable(R.drawable.launcher_icon));
        // }

        addActionBarItem(Type.SortBySize);
        // addActionBarItem(Type.Refresh);
        addActionBarItem(Type.Add);
        addActionBarItem(Type.Share);

        model = new PluginModel(this);

        explorer = new PluginExplorer();
        registerReceiver(explorer, new IntentFilter(PluginReceiver.ACTION_PLUGIN_RESPOND));
        sendBroadcast(new Intent(PluginReceiver.ACTION_PLUGIN_CONTACT),
                PluginReceiver.PERMISSION_IM_A_PLUGIN);

        final List<Item> items = new ArrayList<Item>();
        buildItems(items);

        adapter = new SectionedItemAdapter(this, items);
        setListAdapter(adapter);

        registerForContextMenu(getListView());
    }

    public void buildItems(final List<Item> items) {
        final Cursor cursor = model.getAll();

        while (cursor.moveToNext()) {
            items.add(new PluginItem(this, cursor.getInt(cursor.getColumnIndex(BaseColumns._ID))));
        }

        cursor.close();
    }

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        ((PluginItem)getListView().getItemAtPosition(position)).fireAction();
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        final PluginItem item = (PluginItem)adapter.getItem(info.position);
        menu.setHeaderTitle(item.getName());

        menu.add("Uninstall");
        menu.add("Deactivate / Activate");
        menu.add("Report this Plugin");
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Toast.makeText(this, "Uninstall", Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(this, "Not yet implemented!", Toast.LENGTH_LONG).show();
                break;
        }

        return true;
    }

    @Override
    public boolean onHandleActionBarItemClick(final ActionBarItem item, final int position) {
        switch (position) {
            case 0:
                adapter.toggleSort();
                break;

                // case 1:
                // final List<Item> items = new ArrayList<Item>();
                // buildItems(items);
                //
                // adapter = new SectionedItemAdapter(this, items);
                // setListAdapter(adapter);
                //
                // ((LoaderActionBarItem)item).setLoading(false);
                // break;

            case 1:
                // TODO: open market with new plugins

            case 2:
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent
                .putExtra(Intent.EXTRA_TEXT,
                        "I'm love with my phone, now that it's talking to me!\n@Announcify - http://announcify.com");
                shareIntent.setType("text/plain");
                shareIntent.addCategory(Intent.CATEGORY_DEFAULT);
                startActivity(shareIntent);

            default:
                return super.onHandleActionBarItemClick(item, position);
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO: if we didn't receive every plugin that's in the db: increase
        // rip timer!
        // or register for package installed broadcasts and check packages

        unregisterReceiver(explorer);

        model.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        model = new PluginModel(this);

        registerReceiver(explorer, new IntentFilter(PluginReceiver.ACTION_PLUGIN_RESPOND));
        sendBroadcast(new Intent(PluginReceiver.ACTION_PLUGIN_CONTACT),
                PluginReceiver.PERMISSION_IM_A_PLUGIN);
    }

    class PluginExplorer extends BroadcastReceiver {
        @Override
        synchronized public void onReceive(final Context context, final Intent intent) {
            final int id = model.getId(intent.getStringExtra(PluginReceiver.EXTRA_PLUGIN_NAME));
            if (id >= 0) {
                model.clearTimer(id);
            } else {
                model.add(intent.getStringExtra(PluginReceiver.EXTRA_PLUGIN_NAME),
                        intent.getIntExtra(PluginReceiver.EXTRA_PLUGIN_PRIORITY, 10),
                        intent.getStringExtra(PluginReceiver.EXTRA_PLUGIN_ACTION),
                        intent.getStringExtra(PluginReceiver.EXTRA_PLUGIN_PACKAGE),
                        intent.getBooleanExtra(PluginReceiver.EXTRA_PLUGIN_BROADCAST, false));

                adapter.add(new PluginItem(AnnouncifyActivity.this, model.getId(intent
                        .getStringExtra(PluginReceiver.EXTRA_PLUGIN_NAME))));
                adapter.notifyDataSetChanged();
            }
        }
    }
}