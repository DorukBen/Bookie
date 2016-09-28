package com.karambit.bookie.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by doruk on 31.07.2016.
 */
public class RobotoFontTextView extends TextView {

    public RobotoFontTextView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/roboto_thin.ttf"));
    }
}
