package com.wong.novel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wong.novel.R;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout extends ViewGroup {

    private static final String TAG = "FlowLayout";

    private static final int
            mDefaultTextColor        = Color.BLACK,
            mDefaultBackGround       = Color.parseColor("#dadada"),
            mDefaultDownBackground   = Color.BLACK,
            mDefaultHorizontalMargin = 25,
            mDefaultVerticalMargin   = 25,
            mDefaultItemPadding      = 25;


    private int
            mWidth,
            mPaddingLeft,
            mPaddingTop,
            mPaddingRight,
            mPaddingBottom,
            mTextColor,
            mBackground,
            mDownBackground,
            mHorizontalMargin,
            mVerticalMargin,
            mItemPadding;


    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHorizontalMargin = mDefaultHorizontalMargin;
        mVerticalMargin   = mDefaultVerticalMargin;
        mItemPadding      = mDefaultItemPadding;

        TypedArray tt   = context.obtainStyledAttributes(attrs,R.styleable.FlowLayout);
        mBackground     = tt.getColor(R.styleable.FlowLayout_commonBackground,mDefaultBackGround);
        mDownBackground = tt.getColor(R.styleable.FlowLayout_clickBackground,mDefaultDownBackground);
        mTextColor      = tt.getColor(R.styleable.FlowLayout_flowTextColor,mDefaultTextColor);
        tt.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mPaddingLeft   = getPaddingLeft();
        mPaddingRight  = getPaddingRight();
        mPaddingTop    = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        int childCount = getChildCount(),
        childWidth,childHeight = 0,
        left = mPaddingLeft,
        top  = mPaddingTop;
        //Log.d(TAG,"ChildCount = " + childCount);
        for (int i = 0; i < childCount; i++){
            View child = getChildAt(i);
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
            childWidth  = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            //Log.d(TAG,"Measure Child width = " + childWidth + ", height = " + childHeight);
            if (left + childWidth + mHorizontalMargin + mPaddingRight > mWidth){
                // 超过宽度 需要换行
                //Log.d(TAG,"换行前：" + top);
                top += childHeight + mVerticalMargin;
                left = mPaddingLeft + childWidth;
                //Log.d(TAG,"换行后：" + top);
            }
            else{
                left += childWidth + mHorizontalMargin;
            }
        }
        top += (childHeight + mPaddingBottom);
        //Log.d(TAG,"Measure width = " + mWidth + ", height = " + top);
        setMeasuredDimension(mWidth,top);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount(),
                left   = mPaddingLeft,
                top    = mPaddingTop,
                childWidth,childHeight;
        for (int i = 0; i < childCount; i++){
            View child  = getChildAt(i);
            childWidth  = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            if (left + childWidth + mPaddingRight + mHorizontalMargin > mWidth){
                // 换行
                left = mPaddingLeft;
                top += childHeight + mVerticalMargin;
            }
            //Log.d(TAG,left + " & " + top);
            child.layout(left,top,left + childWidth,top + childHeight);
            left += childWidth + mHorizontalMargin;
        }
    }


    public void setItem(List<String> data){
        if (data == null || data.size() == 0)
            return;
        int size = data.size();

        for (int i = 0; i < size; i++){
            //Log.d(TAG,data.size() + " -- " + data.get(i));
            FlowTextView child = new FlowTextView(getContext(),i);
            child.setText(data.get(i));
            child.setTextColor(mTextColor);
            child.setPadding(mItemPadding,mItemPadding,mItemPadding,mItemPadding);
            child.setBackgroundColor(mBackground);
            addView(child);
        }
        invalidate();
    }


    class FlowTextView extends TextView{

        private int mIndex;

        private float mUpX,mUpY;

        public FlowTextView(Context context,int index) {
            super(context);
            mIndex = index;
        }


        public FlowTextView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }


        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    setBackgroundColor(mDownBackground);
                    break;

                case MotionEvent.ACTION_UP:
                    mUpX = event.getX();
                    mUpY = event.getY();

                    // 判断手指抬起时是否在该View范围内
                    if (mUpX > 0 && mUpX < (getRight() - getLeft()) && mUpY > 0 && mUpY < (getBottom() - getTop())){

                        if (ItemClickListener != null){
                            ItemClickListener.click(this,mIndex);
                        }
                    }

                    setBackgroundColor(mBackground);
                    break;
            }
            return true;
        }

    }


    public interface ItemClickListener{
        void click(View view,int position);
    }

    private ItemClickListener ItemClickListener;

    public void setItemClickListener(FlowLayout.ItemClickListener itemClickListener) {
        ItemClickListener = itemClickListener;
    }
}
