package com.announcify.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class GreedyImageView extends ImageView {

    public GreedyImageView(Context context) {
        super(context);
    }
    
    public GreedyImageView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        
        return true;
    }
}