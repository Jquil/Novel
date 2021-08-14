package com.wong.novel.widget;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class LoadingView extends ViewGroup {

    private static final String TAG = "LoadingView";

    private Context mContext;

    private int mWidth,             // 视图宽度
                mHeight,            // 视图高度
                mCenterX,           // 视图关于X轴的中心点
                mCenterY,           // 视图关于Y轴的中心点
                mItemWidth,         // 条目宽度
                mItemHeight,        // 条目高度
                mItemHalfHeight,    // 条目高度的一半
                mRadius,            // 圆角大小
                mShowCount,         // 显示条目的数量
                mStartLeft,         // 绘制条目的左距离
                mMargin;            // 条目边距

    private String[] mItemColor;    // 条目颜色


    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init(){
        mItemWidth      = 35;
        mItemHeight     = 150;
        mItemHalfHeight = mItemHeight / 2;
        mRadius         = 15;
        mShowCount      = 6;
        mMargin         = 25;
        mItemColor      = new String[]{ "#2b80e2","#31547c","#5880ac","#0d47a1","#8ebbf0","#0277bd" };
        setLoadingItems();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth     = MeasureSpec.getSize(widthMeasureSpec);
        mHeight    = MeasureSpec.getSize(heightMeasureSpec);
        mCenterX   = mWidth / 2;
        mCenterY   = mHeight / 2;
        mStartLeft = mCenterX - (2 * mItemWidth + 2 * mMargin + mItemWidth / 2);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount > 0){
            for (int i = 0; i < childCount; i++){
                View childView = getChildAt(i);
                childView.layout(mStartLeft,mCenterY - mItemHalfHeight,mStartLeft + mItemWidth,mCenterY + mItemHalfHeight);
                mStartLeft += mItemWidth + mMargin;
            }
            dance();
        }
    }


    // 添加加载条目
    private void setLoadingItems(){
        for (int i = 0; i < mShowCount;i++){
            LoadingItem loadingItem = new LoadingItem(mContext,mItemWidth,mItemHeight,mRadius, Color.parseColor(mItemColor[i]));
            addView(loadingItem);
        }
    }


    // 跳舞拉~
    private void dance(){
        // 关键帧 Keyframe
        Keyframe k1 = Keyframe.ofFloat(0,1);
        Keyframe k2 = Keyframe.ofFloat(0.5f,0.5f);
        Keyframe k3 = Keyframe.ofFloat(1,1);
        PropertyValuesHolder holder   = PropertyValuesHolder.ofKeyframe("scaleY",k1,k2,k3);
        ObjectAnimator objectAnimator;
        long delay = -600;
        for (int i = 0; i < mShowCount; i++){
            View child = getChildAt(i);
            if (child == null)
                continue;

            objectAnimator = ObjectAnimator.ofPropertyValuesHolder(child,holder);

            // 设置重复动画数量
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);

            // 设置动画速度
            objectAnimator.setDuration(1000);

            // 设置动画的延迟
            objectAnimator.setStartDelay(delay);

            // 开始动画
            objectAnimator.start();
            delay += 100;
        }
    }
}
