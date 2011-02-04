
package com.announcify.ui.widget;

import java.util.Currency;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.announcify.R;
import com.announcify.api.background.sql.model.BaseModel;
import com.announcify.api.background.sql.model.PluginModel;

public class SectionedItemAdapter extends CursorAdapter {
    
    private static final String SECTIONS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final AlphabetIndexer indexer;    
    private final LayoutInflater inflater;
    private final PluginModel model;
    private final Context context;
    
    private int idIndex;

    public SectionedItemAdapter(final Context context, final PluginModel model, final Cursor cursor) {
        super(context, cursor);
        
        this.model = model;
        this.context = context;
        inflater = LayoutInflater.from(context);
        indexer = new AlphabetIndexer(getCursor(), 0, SECTIONS);
        
        idIndex = cursor.getColumnIndexOrThrow(BaseModel._ID);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        indexer.onChanged();
    }
    
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        Log.e("smn", "getView");
        
        if (convertView == null) {
            convertView = newView(context, getCursor(), parent);
        }
        
//        final int section = indexer.getSectionForPosition(position);
//
//        if (indexer.getPositionForSection(section) == position) {
//            item.header = indexer.getSections()[section].toString().trim();
//        } else {
//            item.header = null;
//        }

        return super.getView(position, convertView, parent);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.e("smn", "newView");
        return inflater.inflate(com.announcify.R.layout.list_item_plugin, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.e("smn", "bindView");
        
        final long id = cursor.getLong(idIndex);
        view.setTag(id);
        
        ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(context.getResources().getDrawable(R.drawable.launcher_icon));
        
        ((TextView)view.findViewById(R.id.plugin)).setText("Announcify");
        
        ImageView check = (ImageView)view.findViewById(R.id.check);
        check.setImageDrawable(context.getResources().getDrawable(model.getActive(id) ? R.drawable.btn_check_buttonless_on : R.drawable.btn_check_buttonless_off));
        check.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                model.togglePlugin((Long) v.getTag());
            }
        });
    }
}
