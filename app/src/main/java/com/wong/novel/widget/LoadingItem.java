package com.wong.novel.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class LoadingItem extends View {

    private static final String TAG = "LoadingItem";

    private int mWidth,mHeight,mRadius,mColor;

    private Paint mPaint;

    public LoadingItem(Context context,int width,int height,int radius,int color) {
        super(context);
        mWidth  = width;
        mHeight = height;
        mRadius = radius;
        mColor  = color;
        init();
    }


    private void init(){
        mPaint = new Paint();
        mPaint.setColor(mColor);
    }


    public LoadingItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(0,0,mWidth,mHeight,mRadius,mRadius,mPaint);
    }
}
