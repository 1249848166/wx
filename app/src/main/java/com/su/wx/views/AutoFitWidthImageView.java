package com.su.wx.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class AutoFitWidthImageView extends ImageView {

    public AutoFitWidthImageView(Context context) {
        super(context);
    }

    public AutoFitWidthImageView(Context context,AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitWidthImageView(Context context,AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable=getDrawable();
        if(drawable!=null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            if(drawable.getIntrinsicWidth()<width){
                width=drawable.getIntrinsicWidth();
            }
            int height = (int) (width * ((float) drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth()));
            setMeasuredDimension(width, height);
        }else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}

