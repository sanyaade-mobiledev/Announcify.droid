
package com.announcify.ui.widget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
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

public class SectionedAdapter extends CursorAdapter {
    
    private static final String SECTIONS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final AlphabetIndexer indexer;    
    private final LayoutInflater inflater;
    private final PluginModel model;
    
    private int idIndex;

    public SectionedAdapter(final Context context, final PluginModel model, final Cursor cursor) {
        super(context, cursor);
        
        this.model = model;
        inflater = LayoutInflater.from(context);
        indexer = new AlphabetIndexer(getCursor(), cursor.getColumnIndexOrThrow(PluginModel.KEY_PLUGIN_NAME), SECTIONS);
        
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
        
        convertView = super.getView(position, convertView, parent);
        
        final int section = indexer.getSectionForPosition(position);

        TextView sectionView = (TextView) convertView.findViewById(R.id.section);
        if (indexer.getPositionForSection(section) == position) {
            sectionView.setBackgroundColor(Color.parseColor("#AD0000"));
            sectionView.setText(indexer.getSections()[section].toString().trim());
            sectionView.setVisibility(View.VISIBLE);
        } else {
            sectionView.setVisibility(View.GONE);
            sectionView.setText(null);
        }

        return convertView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.e("smn", "newView");
        return inflater.inflate(com.announcify.R.layout.list_item_plugin, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Log.e("smn", "bindView");
        
        final long id = cursor.getLong(idIndex);
        view.setTag(id);
        
        ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(context.getResources().getDrawable(R.drawable.launcher_icon));
        
        ((TextView)view.findViewById(R.id.plugin)).setText(model.getName(id));
        
        final ImageView check = (ImageView)view.findViewById(R.id.check);
        check.setTag(id);
        check.setImageDrawable(context.getResources().getDrawable(model.getActive(id) ? R.drawable.btn_check_buttonless_on : R.drawable.btn_check_buttonless_off));
        check.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                model.togglePlugin((Long) v.getTag());
                
                check.setImageDrawable(context.getResources().getDrawable(model.getActive(id) ? R.drawable.btn_check_buttonless_on : R.drawable.btn_check_buttonless_off));
            }
        });
    }
}