package com.wong.novel.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

import com.wong.novel.R;

import java.util.ArrayList;

public class ViewPagerIndicator extends LinearLayout {



    private static final String TAG = "ViewPagerIndicator";

    private final int   default_Indicator       = 2,
                        default_ItemPadding     = 50,
                        default_ItemSize        = 16,
                        default_ItemSelectColor = Color.BLACK,
                        default_ItemColor       = Color.GRAY,
                        default_IndicatorColor  = Color.BLACK,
                        default_IndicatorHeight = 20,
                        default_IndicatorWidth  = 20,
                        Indicator_Delta         = 1,
                        Indicator_Oblong        = 2;

    private int mIndicator,
            mItemPadding,
            mItemSize,
            mItemSelectColor,
            mItemColor,
            mIndicatorHeight,
            mIndicatorWidth,
            mIndicatorColor,
            mItemWidth,
            mItemHeight,
            mItemCount,
            mHalfWidth = 0,
            mOffset    = 0,
            mIndex     = 0;

    private Paint mPaint;
    private Path mPath;
    private ViewPager mVP;

    private onClickListener onClickListener;

    public void setOnClickListener(ViewPagerIndicator.onClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public ViewPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta   = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);
        mItemPadding    = ta.getDimensionPixelSize(R.styleable.ViewPagerIndicator_indicator_itemPadding,default_ItemPadding);
        mItemSize       = ta.getDimensionPixelSize(R.styleable.ViewPagerIndicator_indicator_itemSize,default_ItemSize);
        mItemColor      = ta.getColor(R.styleable.ViewPagerIndicator_indicator_itemColor,default_ItemColor);
        mItemSelectColor= ta.getColor(R.styleable.ViewPagerIndicator_indicator_itemSelectColor,default_ItemSelectColor);
        mIndicator      = ta.getInt(R.styleable.ViewPagerIndicator_indicator_style,default_Indicator);
        mIndicatorWidth = ta.getDimensionPixelOffset(R.styleable.ViewPagerIndicator_indicator_width,default_IndicatorWidth);
        mIndicatorHeight= ta.getDimensionPixelOffset(R.styleable.ViewPagerIndicator_indicator_height,default_IndicatorHeight);
        mIndicatorColor = ta.getColor(R.styleable.ViewPagerIndicator_indicator_color,default_IndicatorColor);
        ta.recycle();
        init();
    }


    private void init(){
        mPaint = new Paint();
        mPaint.setColor(mIndicatorColor);
        mPath = new Path();
        this.setOrientation(HORIZONTAL);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildCount() > 0){
            mItemWidth  = MeasureSpec.getSize(widthMeasureSpec) / getChildCount();
            mItemHeight = getChildAt(0).getHeight();
            mHalfWidth  = mItemWidth / 2;
        }
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mIndicator){

            case Indicator_Delta:
                drawDelta(canvas);
                break;

            case Indicator_Oblong:
                drawOblong(canvas);
                break;
        }

        setTabProperty();
    }


    // 设置Item
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setItem(ArrayList<String> titles){
        TextView tv;
        for (String item : titles){
            tv = new TextView(getContext());
            LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            tv.setLayoutParams(lp);
            tv.setText(item);
            tv.setPadding(mItemPadding,mItemPadding,mItemPadding,mItemPadding);
            tv.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            tv.setTextColor(mItemColor);
            tv.setTextSize(mItemSize);
            tv.setTag(item);
            addView(tv);
        }
        mItemCount = titles.size();
        setItemClickListener();
        requestLayout();
        invalidate();
    }


    // 绑定ViewPager
    public void setupWidthVP(ViewPager vp){
        if (vp != null){
            mVP = vp;
            vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // 重点
                    mOffset = position * mItemWidth + (int) (positionOffset * mItemWidth);
                    invalidate();
                }

                @Override
                public void onPageSelected(int position) {
                    mIndex = position;
                    setTabProperty();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }


    // 设置 Item 属性
    private void setTabProperty(){
        for (int i = 0; i < mItemCount; i++){
            TextView child = (TextView) getChildAt(i);
            child.setTextColor(mItemColor);
            if (i == mIndex){
                child.setTextColor(mItemSelectColor);
            }
        }
    }


    // 监听 Item Click
    private void setItemClickListener(){
        for (int i = 0; i < mItemCount; i++){
            View child = getChildAt(i);
            final int index = i;
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOffset = index * mItemWidth;
                    mIndex  = index;
                    setTabProperty();
                    invalidate();
                    if (mVP != null){
                        mVP.setCurrentItem(mIndex);
                    }
                    if (onClickListener != null){
                        onClickListener.setOnClickListener(v);
                    }
                }
            });
        }
    }


    // 绘制三角形
    private void drawDelta(Canvas canvas){
        mPath.reset();
        mPath.moveTo(mHalfWidth + mOffset - mIndicatorWidth,mItemHeight);
        mPath.lineTo(mHalfWidth + mOffset,mItemHeight - mIndicatorHeight);
        mPath.lineTo(mHalfWidth + mOffset + mIndicatorWidth,mItemHeight);
        mPath.close();
        canvas.drawPath(mPath,mPaint);
    }


    // 绘制长方形
    private void drawOblong(Canvas canvas){
        canvas.drawRect(mHalfWidth + mOffset - mIndicatorWidth,mItemHeight - mIndicatorHeight,mHalfWidth + mOffset + mIndicatorWidth,mItemHeight,mPaint);
    }


    // 暴露给外部的点击事件
    public interface onClickListener{
        void setOnClickListener(View view);
    }
}
