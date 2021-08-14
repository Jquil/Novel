package com.wong.novel.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;

public class SwipeBackLayout extends FrameLayout {

    private static final String TAG = "SwipeBackLayout";

    // 结束回调
    private OnFinishScrollListener setOnFinishScrollListener;

    // 拖拽类
    private ViewDragHelper mViewDragHelper;

    // 左触发点
    private int mCurEdgeFlag = ViewDragHelper.EDGE_LEFT;

    // 唯一子节点
    private View mDragView;

    // 当前横坐标
    private int mCurrentX;

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init(){

        // 创建ViewDragHelper
        mViewDragHelper = ViewDragHelper.create(this,new DragBackCallBack());

        // 设置边缘触发：左边
        mViewDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
    }


    /* 主要功能是计算拖动的位移量、更新背景、设置要显示的屏幕
     *  在父控件执行drawChild时，会调用这个方法 */
    @Override
    public void computeScroll() {
        super.computeScroll();
        // 如果为true，表明移动未结束
        if (mViewDragHelper.continueSettling(true)){
            invalidate();
        }
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // 由ViewDragHelper判断是否拦截
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 将事件传给ViewDragHelper消费
        mViewDragHelper.processTouchEvent(event);
        return true;
    }


    public interface OnFinishScrollListener{
        void completed();
    }


    public void setOnFinishScrollListener(OnFinishScrollListener setOnFinishScrollListener) {
        this.setOnFinishScrollListener = setOnFinishScrollListener;
    }


    private class DragBackCallBack extends ViewDragHelper.Callback{


        // 视图 捕捉 视图
        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return false;
        }


        // 开始拖拽的边缘
        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mCurEdgeFlag = edgeFlags;
            if (mDragView == null){
                mDragView = getChildAt(0);
            }
            // 捕捉子View
            mViewDragHelper.captureChildView(mDragView,pointerId);
        }


        // 水平拖拽
        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            mCurrentX = left;
            return mCurrentX;
        }


        // 释放View
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            //super.onViewReleased(releasedChild, xvel, yvel);
            // 判断：如果此时横坐标 >= width   => 返回
            if (mCurEdgeFlag == ViewDragHelper.EDGE_LEFT){
                if (mCurrentX > getWidth()/2){
                    mViewDragHelper.settleCapturedViewAt(getWidth(),0);
                }
                else{
                    mViewDragHelper.settleCapturedViewAt(0,0);
                }
                invalidate();
            }
        }


        // 视图位置改变
        @Override
        public void onViewPositionChanged(@NonNull View changedView, int left, int top, int dx, int dy) {
            if (left >= getWidth()){

                if (getContext() instanceof Activity){
                    ((Activity) getContext()).finish();
                }

                if (setOnFinishScrollListener != null){
                    setOnFinishScrollListener.completed();
                }
            }
        }
    }
}
