package com.dm.material.dashboard.candybar.utils.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/*
 * This code taken from http://stackoverflow.com/a/19449488/2341387
 */

public class WallpaperView extends ImageView {

    public WallpaperView(Context context) {
        super(context);
    }

    public WallpaperView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, (int) (width * 1.3 ));
    }

}
