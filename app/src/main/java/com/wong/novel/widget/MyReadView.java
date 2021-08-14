package com.wong.novel.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;


import androidx.annotation.NonNull;

import com.wong.novel.R;
import com.wong.novel.bean.Content;

import java.util.ArrayList;
import java.util.List;

public class MyReadView extends BaseReadView{

    private static final String TAG = "MyReadView";

    private static final int FLAG_START             = 0x01,
                             FLAG_NEW               = 0x02,
                             FLAG_SLIDE_LEFT_ZERO   = 0x03,
                             FLAG_SLIDE_LEFT_NEGATIVE_WIDTH = 0x04,
                             FLAG_SLIDE_RIGHT_ZERO  = 0x05,
                             FLAG_SLIDE_RIGHT_WIDTH = 0x06,
                             FLAG_SLIDE_BACK_WIDTH  = 0x07,
                             FLAG_SLIDE_BACK_ZERO   = 0x08,
                             FLAG_SLIDE_BACK_NEGATIVE_WIDTH = 0x09,
                             DEFAULT_TEXT_SIZE  = 18;



    private List<ReadTextView>  mViewList,
                                mPreViewList,
                                mNextViewList;

    private List<Integer> mTextOffsetList,
                          mPreTextOffsetList;

    private int mTextLen,
                mTextStartIndex,
                mNextTextLen,
                mNextTextStartIndex,
                mPreViewStartIndex,
                mRemoveViewIndex;

    private float mX,
                  mLastX,
                  mDownX,
                  mViewOffsetX,
                  mDistance,
                  mTouchSlop;

    private int mLoadReady, // 1 --> common  2 --> content OK 3 --> pre content Ok   ==> start Load View
                mChildCount,
                mViewLoadPage,      //  这两个加起来
                mPreViewLoadPage,   //              = 5
                mHasLoadPage,
                mPreOffsetPosition,
                mOffsetPosition,
                mLayoutLeftX,
                mLayoutRightX,
                mPreViewCount;

    private boolean isFirstLoadView,
                    isSliding,
                    isSureDirection,
                    isChange,
                    isStarted,
                    mFirstLoadViewComplete,
                    mFirstLoadPreViewComplete,
                    mContentViewLoadFinish;

    private LayoutParams mLP;

    private View mLayoutView,
                 mRemoveView,
                 mCacheView,
                 mZeroView,
                 mLastOtherView,
                 mCatchView;

    private ReadTextView mReadTV;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case FLAG_START:
                    if (mLoadReady == 3 && isFirstLoadView && !isStarted){
                        //Log.e(TAG,"FLAG::StartLoadView");
                        setContentView();
                        isStarted = true;
                    }
                    break;

                case FLAG_NEW:
                    //Log.e(TAG,"FLAG::SetContentView");
                    setContentView();
                    break;

                case FLAG_SLIDE_LEFT_ZERO:

                    if (mCatchView == null)
                        return;

                    // 准备要超出了,Distance 改小点
                    if (mDistance + mViewOffsetX < -mWidth){
                        if (mViewOffsetX + mWidth < 10){
                            mDistance = -1;
                        }
                        else{
                            mDistance /= 10;
                        }
                    }
                    mViewOffsetX += mDistance;
                    mCatchView.layout(mWidth + (int) mViewOffsetX,0,(mWidth * 2) + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideLeftToZero();
                    break;

                case FLAG_SLIDE_LEFT_NEGATIVE_WIDTH:
                    if (mCatchView == null)
                        return;
                    if (mViewOffsetX - mDistance < -mWidth){
                        if (-mWidth + mViewOffsetX < 10){
                            mDistance = -1;
                        }
                        else{
                            mDistance /= 10;
                        }
                    }
                    mViewOffsetX += mDistance;
                    mCatchView.layout(0 + (int) mViewOffsetX,0,mWidth + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideLeftToNegativeWidth();
                    break;

                case FLAG_SLIDE_RIGHT_ZERO:
                    if (mCatchView == null)
                        return;
                    if (mDistance + mViewOffsetX > mWidth){
                        if (mWidth - mViewOffsetX < 10){
                            mDistance = 1;
                        }
                        else{
                            mDistance /= 10;
                        }
                    }
                    mViewOffsetX += mDistance;
                    mCatchView.layout(-mWidth + (int) mViewOffsetX,0,0 + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideRightToZero();
                    break;

                case FLAG_SLIDE_RIGHT_WIDTH:
                    if (mCatchView == null)
                        return;
                    if (mDistance + mViewOffsetX > mWidth){
                        if (mWidth - mViewOffsetX < 10){
                            mDistance = 1;
                        }
                        else{
                            mDistance /= 10;
                        }
                    }
                    mViewOffsetX += mDistance;
                    mCatchView.layout(0 + (int) mViewOffsetX,0,mWidth + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideRightToWidth();
                    break;

                case FLAG_SLIDE_BACK_ZERO:
                    if (mCatchView == null)
                        return;

                    if (mViewOffsetX - mDistance < 0){
                        mDistance = 1;
                    }
                    mViewOffsetX -= mDistance;
                    mCatchView.layout(0 + (int) mViewOffsetX,0,mWidth + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideBackToZero();
                    break;

                case FLAG_SLIDE_BACK_WIDTH:
                    if (mCatchView == null)
                        return;
                    if (mDistance + mViewOffsetX > 0){
                        if (Math.abs(mViewOffsetX - mDistance) < 10){
                            mDistance = 1;
                        }
                        else{
                            mDistance /= 10;
                        }
                    }
                    mViewOffsetX += mDistance;
                    mCatchView.layout((int) mViewOffsetX + mWidth,0,mWidth * 2 + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideBackToWidth();
                    break;

                case FLAG_SLIDE_BACK_NEGATIVE_WIDTH:
                    if (mCatchView == null)
                        return;
                    if (mViewOffsetX - mDistance < 0){
                        mDistance = 1;
                    }
                    mViewOffsetX -= mDistance;
                    mCatchView.layout(-mWidth + (int) mViewOffsetX,0,0 + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideBackToNegativeWidth();
                    break;
            }
        }
    };


    public MyReadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mChildCount = getChildCount();
        if (mChildCount == 0)
            return;
        //Log.d(TAG,"ChildCount >> " + mChildCount + " - " + mFirstLoadViewComplete + " - " + mFirstLoadPreViewComplete);

        getChildAt(0).layout(l, t, r, b);

        mLayoutView = getChildAt(mChildCount - 1);
        if (mChildCount == 1){
            mLayoutView.layout(l, t, r, b);
        }

        else{
            switch ((LoadType) mLayoutView.getTag(R.id.view_type)){
                case Right:
                    mLayoutView.layout(r,t,r + mWidth,b);
                    break;

                case Left:
                    mLayoutView.layout(-mWidth,t,0,b);
                    break;
            }
        }

        Log.e(TAG,"onLayout >> " + mChildCount + " ~ " + isFirstLoadView + " ~ " + mFirstLoadViewComplete + " ~ " + mFirstLoadPreViewComplete);
        if (isFirstLoadView && mFirstLoadViewComplete && mFirstLoadPreViewComplete){
            isFirstLoadView = false;
            if (Call != null){
                Call.complete();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSliding){
            Log.e(TAG,"isSliding");
            return false;
        }

        float x = event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownX     = x;
                mLastX     = x;
                mCatchView = null;
                mDirection = null;
                isSureDirection = false;
                if (mPageIndex < 1){
                    mPageIndex = 1;
                }
                Log.e(TAG,"onTouchEvent::Down,Index = " + mPageIndex + " - " + mViewOffsetX + " - " + isSliding);
                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mLastX - mDownX) >= mTouchSlop){
                    mX = x - mLastX;

                    // 确定滑动方向
                    if (!isSureDirection){

                        // >> 右滑：上一页
                        if (mX > 0){
                            //Log.e(TAG,mPageIndex + " --- " + mContentIndex);
                            if (mPageIndex == 1){

                                if (mContentIndex == 0){
                                    Log.e(TAG,"TOUCH:RIGHT,Intercept");
                                    return false;
                                }

                                else if ((int) getChildAt(mPageIndex - 1).getTag(R.id.view_position) == 0){
                                    mLoadWay   = LoadWay.Front;

                                    if ((int) getChildAt(0).getTag(R.id.view_position) == 1){
                                        mPageIndex = mPreTextOffsetList == null ? mPreViewStartIndex : findIndexByPosition(mPreTextOffsetList.size() - 1,mContentIndex - 1);
                                    }
                                    else if ((int) getChildAt(0).getTag(R.id.view_position) == 0){
                                        mPageIndex = mPreTextOffsetList == null ? mPreViewStartIndex : findIndexByPosition(mPreViewList.size() - 1,mContentIndex - 1);
                                    }
                                    Log.e(TAG,"onTouchEvent::Move Of Start, Index = " + mPageIndex + " -- " + mPreViewList.get(0) + " - " + mPreTextOffsetList.size() + " - " + (mPreTextOffsetList.size() - 1));
                                    for (int i = 0; i < mChildCount; i++){
                                        Log.e(TAG,"onTouchEvent::Move, View Index = " + i + " and pos = " + getChildAt(i).getTag(R.id.view_position) + " - " + getChildAt(i).getTag(R.id.view_index));
                                    }
                                }
                            }

                            switch (mLoadWay){
                                case Front:
                                    mCatchView      = getChildAt(mPageIndex);
                                    mLayoutLeftX    = -mWidth;
                                    mLoadWay        = LoadWay.Front;
                                    mLayoutRightX   = 0;
                                    break;

                                case After:
                                    mCatchView      = getChildAt(mPageIndex - 1);
                                    mLayoutLeftX    = 0;
                                    mLayoutRightX   = mWidth;
                                    break;
                            }
                            mDirection      = SlideDirection.Right;
                            isSureDirection = true;
                        }

                        // >> 左滑：下一页
                        else{
                            //Log.e(TAG,"onTouchEvent::Move Of Right,Before--Index = " + mPageIndex);
                            switch (mLoadWay){
                                case After:
                                    if (mPageIndex >= mChildCount){
                                        //Log.e(TAG,"TOUCH:LEFT,Intercept");
                                        return false;
                                    }
                                    //Log.e(TAG,"onTouchEvent::Move Of Right,After--Index = " + mPageIndex);
                                    //Log.e(TAG,"onTouchEvent()::Move,PageIndex = " + mPageIndex);
                                    mCatchView    = getChildAt(mPageIndex);
                                    //Log.e(TAG,mPageIndex + " ___ " + getChildAt(2).getTag(R.id.view_position));
                                    mLayoutLeftX  = mWidth;
                                    mLayoutRightX = (mWidth * 2);
                                    break;

                                case Front:
                                    // View to (-Width,0)
                                    mCatchView    = getChildAt(mPageIndex - 1);
                                    mLayoutLeftX  = 0;
                                    mLayoutRightX = mWidth;
                                    break;
                            }

                            mDirection      = SlideDirection.Left;
                            isSureDirection = true;
                        }

                        //Log.e(TAG,"onTouchEvent:Move()，" + mDirection + " -- " + mCatchView + " -- " + mPageIndex);
                    }

                    if (mCatchView == null)
                        return false;

                    mViewOffsetX += mX;
                    mCatchView.layout(mLayoutLeftX + (int) mViewOffsetX,0,mLayoutRightX + (int) mViewOffsetX,mHeight);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (Math.abs(mLastX - mDownX) >= mTouchSlop) {
                    up();
                }
                else{
                    // 判定为点击
                    if (Call != null){
                        Call.click();
                    }
                }
                break;

        }
        mLastX = x;
        return true;
    }


    private void up(){
        if (mCatchView == null)
            return;

        if (mDirection == null)
            return;

        isSliding    = true;
        mViewOffsetX = (int) mViewOffsetX;
        if (Math.abs(mViewOffsetX) >= mLimitDistance){
            switch (mDirection){

                case Left:
                    if (mLoadWay == LoadWay.Front){
                        mDistance = (int) mViewOffsetX / 10;
                        //Log.d(TAG,"Distance >> " + mDistance);
                        slideLeftToNegativeWidth();
                    }
                    else{
                        mDistance = (int) mViewOffsetX + mWidth;
                        mDistance/= 10;
                        mDistance = -mDistance;
                        slideLeftToZero();
                    }
                    break;

                case Right:
                    if (mLoadWay == LoadWay.Front){
                        mDistance = mWidth - (int) mViewOffsetX;
                        mDistance /= 10;
                        slideRightToZero();
                    }
                    else{
                        mDistance = mWidth - (int) mViewOffsetX;
                        mDistance /= 10;
                        slideRightToWidth();
                    }
            }
        }
        // 滑动距离没有超过限制的距离，滚回~
        else{
            slideBack();
        }

        //Log.e(TAG,"up(),PageIndex = " + mPageIndex + " and ChildCount = " + getChildCount());
    }


    private void slideBack(){
        switch (mDirection){
            case Left:
                // >> 下一页：返回 => (width,0)，另Offset = 0
                mDistance = - (int) mViewOffsetX / 10;
                if (mLoadWay == LoadWay.After){
                    slideBackToWidth();
                }
                else{
                    slideBackToZero();
                }
                break;

            case Right:
                if ((int)mCatchView.getTag(R.id.view_index) != mContentIndex){
                    isChange = true;
                }

                if (isChange || mLoadWay == LoadWay.Front){
                    // 上一章最后一页：返回 => (-width,0)
                    isChange = false;
                    mDistance = (int) mViewOffsetX / 10;
                    slideBackToNegativeWidth();
                }
                else{
                    // 上一页：返回 => (0,0)
                    mDistance = (int) mViewOffsetX / 10;
                    slideBackToZero();
                }
                break;
        }
    }


    @Override
    void slideLeftToZero() {
        if (mViewOffsetX > -mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_LEFT_ZERO,1);
        }
        else{
            // Remove View
            //Log.e(TAG,"slideLeftToZero >> ");
            //Log.e(TAG,"ChildCount = " + mChildCount);
            mCacheView  = getLastView();
            if (mCacheView == null){
                mCacheView = getChildAt(mChildCount - 1);
            }
            mRemoveView = getPreView();
            if (mRemoveView == null){
                mRemoveView = getChildAt(0);
            }
            //Log.e(TAG,"slideLeftToZero()，RemoveViewPos = " + mRemoveView.getTag(R.id.view_position));
            removeViewForParent(mRemoveView);

            //Log.e(TAG,">>> " + mContentViewLoadFinish + " - " + hasOtherView() + " -- " + mPageIndex);
            //mCacheView = findViewByPosition((int) mCacheView.getTag(R.id.view_position) + 1,mViewList);
            if (isLastView((ReadTextView) mCacheView)){
                //Log.e(TAG,"slideLeftToZero()，Finish");
                 setNextContentView();
            }
            else{
                mCacheView = findViewByPosition((int) mCacheView.getTag(R.id.view_position) + 1,mViewList);
                if (mCacheView != null){
                    addView(mCacheView);
                }
                else{
                    setContentView();
                }
                /* findViewByPosition,if none -> setContentView */
            }
            // ...
            isSliding    = false;
            mLoadWay     = LoadWay.After;
            mViewOffsetX = 0;
            mPageIndex   = mPageIndex + 1 > 3 ? 3 : mPageIndex + 1;


            // Change
            if ((int)mCatchView.getTag(R.id.view_index) != mContentIndex){
                mPreContent   = mContent;
                mContent      = mNextContent;
                mContentIndex = mContent.index;
                if (Call != null && mContentIndex != mMaxIndex){
                    Call.loadChapter(FLAG_NEXT,mContentIndex + 1);
                }
                if (mPreViewList == null){
                    mPreViewList = new ArrayList<>();
                }
                mPreViewList.clear();
                mPreViewList.addAll(mViewList);
                mViewList.clear();
                mViewList.addAll(mNextViewList);
                mNextViewList.clear();
                mTextStartIndex        = mNextTextStartIndex;
                mTextLen               = mNextTextLen;
                mContentViewLoadFinish = false;
            }
        }

        for (int i = 0; i < mChildCount; i++){
            Log.e(TAG,"slideLeftToZero()，index = " + i + " and pos = " + getChildAt(i).getTag(R.id.view_position) + " and contentIndex = " + getChildAt(i).getTag(R.id.view_index) );
        }
    }


    @Override
    void slideLeftToNegativeWidth() {
        //Log.e(TAG,"slideLeftToNegativeWidth() >> " + mDistance);
        /* 下一页：针对LoadWay = Front
            1. 移除 Index = 当前章的第一页（不一定是等于0）
            2. 添加下一页：最后一页position(**ContentIndex**)
          */
        if (mViewOffsetX > -mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_LEFT_NEGATIVE_WIDTH,1);
        }
        else{
            isSliding    = false;
            mViewOffsetX = 0;

            if ((int) mCatchView.getTag(R.id.view_position) == mTextOffsetList.size() - 1){
                isChange = true;
            }
            //Log.e(TAG,"slideLeftToNegativeWidth(), isChange = " + isChange + " - " + mCatchView.getTag(R.id.view_position) + " - " + (mTextOffsetList.size()-1) + " - " + mCatchView.getTag(R.id.text_offset) + " - " + ((ReadTextView)mCatchView).getText().length() + " - " + mContent.content.length());
            if (isChange){
                /* todo */
                isChange      = false;
                mLoadWay      = LoadWay.After;
                mPageIndex    = 1;
                mPreContent   = mContent;
                mContent      = mNextContent;
                mContentIndex = mContent.index;

                mPreViewList.clear();
                mPreViewList.addAll(mViewList);
                mViewList.clear();
                mViewList.addAll(mNextViewList);
                mNextViewList.clear();

                mPreTextOffsetList.clear();
                mPreTextOffsetList.addAll(mTextOffsetList);
                mPreOffsetPosition = mOffsetPosition;

                //Log.e(TAG,"slideLeftToNegativeWidth()," + mPreViewList.size() + " - " + mViewList.size());
                if (mContentIndex != mMaxIndex && Call != null){
                    Call.loadChapter(FLAG_NEXT,mContentIndex + 1);
                }

                mTextLen         = mContent.content.length();
                mTextStartIndex += mViewList.get(2).length();
                //Log.e(TAG,"LEN>"+mTextStartIndex);
                mContentViewLoadFinish = false;

                mRemoveView = getPreView();
                //Log.e(TAG,"slideLeftToNegativeWidth(),remove -- " + mRemoveView.getTag(R.id.view_position));
                if (mRemoveView == null){
                    mRemoveView = getChildAt(mChildCount - 1);
                }
                //Log.e(TAG,"slideLeftToNegativeWidth(), position = " + mRemoveView.getTag(R.id.view_position));
                //Log.e(TAG,"slideLeftToNegativeWidth()," + getChildAt(0).getTag(R.id.view_position) + " - " + findIndexByPosition(0,mContentIndex));
                removeViewForParent(mRemoveView);
                //Log.e(TAG,"slideLeftToNegativeWidth()," + getChildAt(0).getTag(R.id.view_position));
                addView(mViewList.get(2),2);
                getChildAt(2).layout(mWidth,0,mWidth * 2,mHeight);
            }
            else{
                mRemoveView  = getChildAt(mChildCount - 1);
                mZeroView    = getChildAt(0);
                removeViewForParent(mRemoveView);
                int position = (int) mZeroView.getTag(R.id.view_position);
                //Log.e(TAG,"POSITION>>>"+position);
                if (!hasOtherView()){
                    mCacheView = findViewByPosition(position+1,mViewList);
                    if (mCacheView == null){
                        mTextStartIndex = mNextViewList.get(0).length();
                        //Log.e(TAG,"LEN>"+mTextStartIndex);
                        addView(mNextViewList.get(0),0);
                    }
                    else{
                        addView(mCacheView,0);
                    }
                }
                else{
                    mTextStartIndex += mNextViewList.get(1).length();
                    //Log.e(TAG,"LEN>"+mTextStartIndex);
                    addView(mNextViewList.get(1),1);
                    getChildAt(1).layout(mWidth,0,mWidth * 2,mHeight);
                }
            }
            for (int i = 0; i < getChildCount(); i++){
                //Log.e(TAG,"slideLeftToNegativeWidth()，ChildIndex = " + i + " and Position = " + getChildAt(i).getTag(R.id.view_position) + " and index = " + getChildAt(i).getTag(R.id.view_index));
            }
            //Log.e(TAG,"slideLeftToNegativeWidth(),PageIndex = " + mPageIndex);
        }
    }


    @Override
    void slideRightToZero() {
        /* 上一页
        *  LoadWay = Front
        * 1. 优先删除下一章（最后 -> 最前）
        * 2. 然后就是(index = 0)
        * */
        if (mViewOffsetX < mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_RIGHT_ZERO,1);
        }
        else{
            //Log.d(TAG,"Slide Right To Zero Finish~");
            if ((int) mCatchView.getTag(R.id.view_index) != mContentIndex){
                isChange = true;
            }

            if (isChange){
                mNextContent  = mContent;
                mContent      = mPreContent;
                mContentIndex = mContent.index;
                if (Call != null && mContentIndex != 0){
                    //isLoadPreView = false;
                    Call.loadChapter(FLAG_PRE,mContentIndex - 1);
                }

                if (mNextViewList == null){
                    mNextViewList = new ArrayList<>();
                }
                mNextViewList.clear();
                mNextViewList.addAll(mViewList);
                mViewList.clear();
                mViewList.addAll(mPreViewList);
                mPreViewList.clear();

                isChange = false;
                //mLoadWay = LoadWay.Front;

                mTextOffsetList.clear();
                mTextOffsetList.addAll(mPreTextOffsetList);
                mPreTextOffsetList.clear();

                mOffsetPosition = mPreOffsetPosition;

                isSliding = false;
                mContentViewLoadFinish = false;
                //Log.e(TAG,"slideRightToZero(),LoadWay = " + mLoadWay);
                mZeroView = getChildAt(0);
                if ((int) mZeroView.getTag(R.id.view_position) == 0){
                    mRemoveViewIndex = findIndexOfRemoveOtherView();
                    //Log.e(TAG,"slideRightToZero(),RemoveIndex = " + mRemoveViewIndex);
                    mRemoveView = getChildAt(mRemoveViewIndex == -1 ? 0 : mRemoveViewIndex);
                    removeViewForParent(mRemoveView);
                }
                else{
                    mRemoveViewIndex = findIndexByPosition(3,mContentIndex + 1);
                    mRemoveView = getChildAt(mRemoveViewIndex == -1 ? 0 : mRemoveViewIndex);
                    removeViewForParent(mRemoveView);
                    mRemoveView = getChildAt(0);
                    mCacheView  = mRemoveView;
                    removeViewForParent(mRemoveView);
                    addView(mCacheView,1);
                    mCacheView.layout(mWidth,0,mWidth * 2,mHeight);
                }
                //Log.e(TAG,">>> " + mRemoveViewIndex + " _____ " + mRemoveViewIndex);

                mLoadWay = LoadWay.Front;
                setContentView();
                //Log.e(TAG,"count >>> " + getChildCount() + " ____ " + getOtherViewCount());
                //Log.d(TAG,">> PageIndex:"+mPageIndex);
            }
            else{
                /*
                * 上一页：
                * 1. remove，优先Remove Other Last View，齐次是0
                * 2. addView ~
                * */
                mRemoveView = getNextView();
                if (mRemoveView == null){
                    mRemoveView = getChildAt(0);
                }

                //mRemoveViewIndex = findIndexOfRemoveOtherView();
                //Log.e(TAG,">>>>" + mRemoveViewIndex);
                //mRemoveView = getChildAt(mRemoveViewIndex);
                removeViewForParent(mRemoveView);
                if (mContentViewLoadFinish){
                    if (mContentIndex != 0){
                        setPreContentView();
                        //Log.d(TAG,">>" + mPreViewLoadPosition + " -- " + getChildCount() + " -- " + mPreContent + " -- " + mPreContentOffset);
                    }
                }
                else{
                    mChildCount = getChildCount();
                    mCacheView  = getChildAt(mChildCount - 1);
                    mCacheView  = findViewByPosition((int) mCacheView.getTag(R.id.view_position) - 1,mViewList);
                    if (mCacheView != null){
                        addView(mCacheView);
                    }
                    else{
                        setContentView();
                    }
                    //Log.e(TAG,"slideRightToZero() >> setContentView");
                }
            }
            //Log.e(TAG,"slideRightToZero(),ZeroViewPosition = " + getChildAt(0).getTag(R.id.view_position) + " and removeViewPos = " + mRemoveView.getTag(R.id.view_position));
            //Log.e(TAG,"slideRightToZero(),RemoveViewPosition = " + mRemoveView.getTag(R.id.view_position));
            //Log.e(TAG,"slideRightToZero() >> " + mRemoveViewIndex + " & " + mContentViewLoadFinish + " & " + mContent);
            mPageIndex   = mChildCount - 2;
            mViewOffsetX = 0;
            isSliding    = false;
        }
    }


    @Override
    void slideRightToWidth() {
        /* 上一页：出现情景：LoadWay = After
           1. 移除最后一页，向底层添加View，layout in center
           2. 如果底层View position == 0：表示是第一页了，这是需要添加上一章最后一页
           3. Page 维持在3 就不需要改动，否则就-1 */
        if (mViewOffsetX < mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_RIGHT_WIDTH,1);
        }
        else{
            if (mPageIndex != 3){
                mPageIndex = mPageIndex - 1 < 1 ? 1 : mPageIndex - 1;
                //Log.e(TAG,"slideRightToWidth(), (1) PageIndex = " + mPageIndex);
            }
            mZeroView    = getChildAt(0);
            int position = (int) mZeroView.getTag(R.id.view_position);
            //Log.e(TAG,">>>" + position + " --- " + hasOtherView());

            if (hasOtherView()){
                /* 有两种情况
                    1. 存在前一章的后几页
                    2. 存在下一章的前几页
                 */
                if (getLastOtherViewIndex() < mContentIndex){
                    mRemoveView  = getChildAt(findIndexOfRemoveByDESC());
                    removeViewForParent(mRemoveView);
                    //Log.e(TAG,"slideRightToWidth(), "+mPreViewList.size());
                    if ((int) mZeroView.getTag(R.id.view_position) == 1){
                        mCacheView = mPreViewList.get(1);
                        addView(mCacheView);
                    }
                    else{
                        mCacheView = mPreViewList.get(mPreViewList.size() - 2);
                        addView(mCacheView);
                    }
                    mPageIndex = mPageIndex - 1 < 1 ? 1 : mPageIndex - 1;
                    //Log.e(TAG,"slideRightToWidth(), (2) PageIndex = " + mPageIndex);
                }
                else{
                    mRemoveView = getChildAt(mChildCount - 1);
                    removeViewForParent(mRemoveView);
                    addView(findViewByPosition(((int)mZeroView.getTag(R.id.view_position) - 1),mViewList),0);
                }
                //mPageIndex = mPageIndex - 1 < 1 ? 1 : mPageIndex - 1;
                //Log.e(TAG,"Add Pre 1");
            }
            else{
                if (mLoadWay == LoadWay.Front && position == 1){
                    mRemoveView  = getChildAt(findIndexOfRemoveByDESC());
                    removeViewForParent(mRemoveView);
                    mCacheView = mPreViewList.get(0);
                    addView(mCacheView);
                    mPageIndex = mPageIndex - 1 < 1 ? 1 : mPageIndex - 1;

                    //Log.e(TAG,"slideRightToWidth(), (3) PageIndex = " + mPageIndex);
                    //Log.e(TAG,"Add Pre 0," + mCacheView.getTag(R.id.view_index).toString() +"---" + mContentIndex);
                }
                else if (mLoadWay == LoadWay.After && position == 0){
                    mRemoveView  = getChildAt(findIndexOfRemoveByDESC());
                    removeViewForParent(mRemoveView);
                    mCacheView = mPreViewList.get(mPreViewList.size() - 1);
                    addView(mCacheView);
                    mPageIndex = mPageIndex - 1 < 1 ? 1 : mPageIndex - 1;
                }
                else if (mLoadWay == LoadWay.After && position == 1 || mLoadWay == LoadWay.Front && position == 2){
                    mRemoveView  = getChildAt(mChildCount - 1);
                    removeViewForParent(mRemoveView);
                    mCacheView = mViewList.get(0);
                    addView(mCacheView,0);
                    //Log.e(TAG,"")
                }
                else{
                    if (mLoadWay == LoadWay.After){
                        mRemoveView  = getChildAt(mChildCount - 1);
                        removeViewForParent(mRemoveView);
                        mCacheView = findViewByPosition(position - 1,mViewList);
                        addView(mCacheView,0);
                    }
                    else{

                    }
                }
            }
            //Log.e(TAG,"slideRightToWidth()，PageIndex = " + mPageIndex);
            //Log.e(TAG,"slideRightToWidth()," + mRemoveView.getTag(R.id.view_position));
            //Log.e(TAG,"ZERO >> " + getChildAt(0).getTag(R.id.view_position) + " --- " + getChildAt(mChildCount - 1).getTag(R.id.view_position));
            //Log.e(TAG,"slideRightToWidth(),ZeroViewPosition = " + getChildAt(0).getTag(R.id.view_position));
            for (int i = 0; i < mChildCount; i++){
                Log.e(TAG,"slideRightToWidth()，index = " + i + " and pos = " + getChildAt(i).getTag(R.id.view_position) + " and contentIndex = " + getChildAt(i).getTag(R.id.view_index) );
            }
            isSliding = false;
            mViewOffsetX = 0;
        }
    }


    @Override
    void slideBackToZero() {
        if (mViewOffsetX > 0){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_ZERO,1);
        }
        else{
            isSliding = false;
            mViewOffsetX = 0;
        }
    }


    @Override
    void slideBackToWidth() {
        if (mViewOffsetX < 0){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_WIDTH,1);
        }
        else{
            isSliding = false;
            mViewOffsetX = 0;
        }
    }


    @Override
    void slideBackToNegativeWidth() {
        if (mViewOffsetX > 0){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_NEGATIVE_WIDTH,1);
        }
        else{
            if ((int)mCatchView.getTag(R.id.view_index) != mContentIndex){
                mLoadWay   = LoadWay.After;
                mPageIndex = 1;
            }
            isSliding = false;
            mViewOffsetX = 0;
        }
    }


    @Override
    void init() {
        mLoadReady              = 1;
        mPageIndex              = 1;
        mViewLoadPage           = 3;
        mPreViewLoadPage        = 2;
        mPreViewCount           = 0;
        mViewOffsetX            = 0;
        mRemoveViewIndex        = 0;
        mHasLoadPage            = 0;
        mTouchSlop              = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mLoadWay                = LoadWay.After;
        mContent                = mNextContent = mPreContent = null;
        isSliding               = false;
        isStarted               = false;
        isFirstLoadView         = true;
        mContentViewLoadFinish  = mFirstLoadViewComplete = mFirstLoadPreViewComplete = false;
    }


    @Override
    void setContentView(){

        if (mContent == null)
            return;

        if (mContentViewLoadFinish)
            return;


        //Log.d(TAG,"setContentView() ViewLoadPage -> " + mViewLoadPage);
        if (isFirstLoadView){
            //Log.e(TAG,"" + mChildCount + " --- " + mViewLoadPage);
            //synchronized (MyReadView.class) {
                //mHasLoadPage++;
                //if (mHasLoadPage > mViewLoadPage)
                //    return;
                mReadTV = new ReadTextView(mContext, mContent.title);
                mLP = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mReadTV.setLayoutParams(mLP);
                mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
                mReadTV.setText(mContent.content.substring(mTextStartIndex));
                mReadTV.setTag(R.id.text_offset,mTextStartIndex);
                mReadTV.setTag(R.id.view_type, LoadType.Right);
                mReadTV.setTag(R.id.view_index, mContentIndex);
                mReadTV.setTag(R.id.view_position, mViewList.size());
                mViewList.add(mReadTV);
                addView(mReadTV);
                //Log.e(TAG,"HasLoadViewPage = " + mHasLoadPage + "  ChildCount When After AddView = " + getChildCount() + " And Position = " + mReadTV.getTag(R.id.view_position) + " And nowTextStartIndex = " + mTextStartIndex);
                //Log.e(TAG,"" + getChildCount() + " && " + mHasLoadPage);
                //mHasLoadPage++;
                //Log.e(TAG,"ChildCount = " + getChildCount() + " - " + mHasLoadPage + " - " + mViewLoadPage);
                mReadTV.setCall((int len) -> {
                    mTextStartIndex += len;
                    mHasLoadPage++;
                    //Log.e(TAG, ">> " + mHasLoadPage + " - " + mViewLoadPage + " - " + mReadTV.getTag(R.id.view_position));
                    if (mHasLoadPage < mViewLoadPage) {
                        mHandler.sendEmptyMessage(FLAG_NEW);
                    }
                    else{
                        mFirstLoadViewComplete = true;
                        setPreContentView();
                        if (mChildCount == mViewLoadPage) {

                        }
                    }

                    if (mTextStartIndex == mTextLen) {
                        mContentViewLoadFinish = true;
                    }
                    //Log.e(TAG,"First >> " + mTextStartIndex + " --- " + mChildCount + " --- " + mHasLoadPage + " --- " + mViewLoadPage);

                });
            //}
        }
        else{
            switch (mLoadWay){
                case Front:
                    if (mOffsetPosition - 1 < 0)
                        return;
                    //Log.e(TAG,"POSITION>>"+mOffsetPosition);
                    mReadTV = new ReadTextView(mContext,mContent.title);
                    mReadTV.setTag(R.id.view_index,mContentIndex);
                    mReadTV.setTag(R.id.view_type,LoadType.Left);
                    mReadTV.setTag(R.id.view_position,mOffsetPosition);
                    //Log.e(TAG,"setContentView,position = " + mOffsetPosition);
                    mReadTV.setTag(R.id.text_offset,mTextOffsetList.get(mOffsetPosition - 1));
                    mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
                    mReadTV.setText(mContent.content.substring(mTextOffsetList.get(mOffsetPosition - 1),mTextOffsetList.get(mOffsetPosition)));
                    mViewList.add(mReadTV);
                    addView(mReadTV);
                    mOffsetPosition -= 1;
                    if (mOffsetPosition == 0){
                        mContentViewLoadFinish = true;
                    }
                    //Log.d(TAG,"setContentView >>");
                    break;

                case After:
                    mReadTV = new ReadTextView(mContext,mContent.title);
                    mLP     = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
                    mReadTV.setLayoutParams(mLP);
                    mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
                    mReadTV.setText(mContent.content.substring(mTextStartIndex));
                    mReadTV.setTag(R.id.text_offset,mTextStartIndex);
                    mReadTV.setTag(R.id.view_index,mContentIndex);
                    mReadTV.setTag(R.id.view_position,mViewList.size());
                    mReadTV.setTag(R.id.view_type,LoadType.Right);
                    mReadTV.setTag(R.id.view_position,mViewList.size());
                    mViewList.add(mReadTV);
                    addView(mReadTV);
                    mReadTV.setCall((int len) -> {
                        this.mTextStartIndex += len;
                        //Log.e(TAG,"O >> " + mTextStartIndex);
                        if (mTextStartIndex == mTextLen){
                            mContentViewLoadFinish = true;
                        }
                    });
                    break;
            }
        }
    }


    @Override
    void setPreContentView() {
        //Log.e(TAG,"setPreContentView::"+mPreContent);
        if (mPreContent == null)
            return;

        if (mPreTextOffsetList == null || mPreTextOffsetList.size() == 0)
            return;

        if (isFirstLoadView){
            mPreViewStartIndex = mViewLoadPage;
            mRemoveViewIndex   = mPreViewStartIndex;
            //Log.d(TAG,"setPreContentView() -> " + mPreViewStartIndex);
            for (int i = 0; i < mPreViewLoadPage; i++){
                if (mPreOffsetPosition - 1 < 0){
                    break;
                }
                mReadTV = new ReadTextView(mContext,mPreContent.title);
                mLP     = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
                mReadTV.setLayoutParams(mLP);
                mReadTV.setTag(R.id.view_index,mPreContent.index);
                mReadTV.setTag(R.id.view_type,LoadType.Left);
                mReadTV.setTag(R.id.view_position,mPreOffsetPosition);
                mReadTV.setTag(R.id.text_offset,mPreTextOffsetList.get(mPreOffsetPosition - 1));
                mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
                mReadTV.setText(mPreContent.content.substring(mPreTextOffsetList.get(mPreOffsetPosition - 1),mPreTextOffsetList.get(mPreOffsetPosition)));
                //Log.d(TAG,"setPreContentView >> " + mPreContent.content.substring(mPreTextOffsetList.get(mPreOffsetPosition - 1),mPreTextOffsetList.get(mPreOffsetPosition)));
                //Log.e(TAG,"setPreContentView() >> " + mPreOffsetPosition);
                mPreOffsetPosition -= 1;
                mPreViewCount++;
                mPreViewList.add(mReadTV);
                addView(mReadTV);
                mChildCount = getChildCount();
                //Log.e(TAG,"addPreContentView and now child-count = " + getChildCount());
            }
            mFirstLoadPreViewComplete = true;
        }
        else{
            mReadTV = new ReadTextView(mContext,mPreContent.title);
            mLP     = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            mReadTV.setLayoutParams(mLP);
            mReadTV.setTag(R.id.view_index,mPreContent.index);
            mReadTV.setTag(R.id.view_type,LoadType.Left);
            mReadTV.setTag(R.id.view_position,mPreOffsetPosition);
            mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
            mReadTV.setText(mPreContent.content.substring(mPreTextOffsetList.get(mPreOffsetPosition - 1),mPreTextOffsetList.get(mPreOffsetPosition)));
            //Log.d(TAG,"setPreContentView >> " + mPreContent.content.substring(mPreTextOffsetList.get(mPreOffsetPosition - 1),mPreTextOffsetList.get(mPreOffsetPosition)));
            mPreOffsetPosition -= 1;
            mPreViewCount++;
            mPreViewList.add(mReadTV);
            addView(mReadTV);
        }
    }


    @Override
    void setNextContentView() {
        if (mNextContent == null)
            return;

        if (mNextViewList == null)
            return;

        mReadTV = new ReadTextView(mContext,mNextContent.title);
        mLP     = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mReadTV.setLayoutParams(mLP);
        mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
        mReadTV.setText(mNextContent.content.substring(mNextTextStartIndex));
        mReadTV.setTag(R.id.view_type,LoadType.Right);
        mReadTV.setTag(R.id.text_offset,mNextTextStartIndex);
        mReadTV.setTag(R.id.view_index,mNextContent.index);
        mReadTV.setTag(R.id.view_position,mNextViewList.size());
        mNextViewList.add(mReadTV);
        addView(mReadTV);
        mReadTV.setCall((int len) -> {
            mNextTextStartIndex += len;
        });
    }


    @Override
    public void setContent(Content content) {
        mContent        = content;
        mContentIndex   = content.index;
        mTextStartIndex = 0;
        mTextLen        = content.content.length();
        mViewList       = new ArrayList<>();
        mTextOffsetList = new ArrayList<>();
        mLoadReady     += 1;

        if (Call == null)
            return;

        if (mContentIndex != mMaxIndex){
            Call.loadChapter(FLAG_NEXT,mContentIndex + 1);
        }

        if (mContentIndex != 0){
            //Log.e(TAG,"setContent(),LoadPreContent = =");
            Call.loadChapter(FLAG_PRE,mContentIndex - 1);
        }
        else{
            mFirstLoadPreViewComplete = true;
            mViewLoadPage    = 5;
            mPreViewLoadPage = 0;
            mLoadReady += 1;
        }

        //Log.e(TAG,"setContent(), " + mContent + " ~ " + mLoadReady);
        mHandler.sendEmptyMessage(FLAG_START);
    }


    @Override
    public void setPreContent(Content content) {
        //if (mPreViewList != null && mPreViewList.size() > 0)
        //    return;
        mPreContent        = content;
        mPreViewCount      = 0;
        mPreViewList       = new ArrayList<>();
        mPreTextOffsetList = new ArrayList<>();
        mPreTextOffsetList.addAll(calculationTextOffset(DEFAULT_TEXT_SIZE,mPreContent.content));
        mPreOffsetPosition = mPreTextOffsetList.size() - 1;
        //Log.e(TAG,"setPreContent(),"+mPreTextOffsetList.size() + " >> " + mPreContent + " >> " + mLoadReady + " >> " + isFirstLoadView);
        if (isFirstLoadView){
            mLoadReady += 1;
            mHandler.sendEmptyMessage(FLAG_START);
        }
    }


    @Override
    public void setNextContent(Content content) {
        mNextContent  = content;
        mNextViewList = new ArrayList<>();
        mNextTextStartIndex = 0;
        mNextTextLen  = mNextContent.content.length();
    }


    @Override
    public void setMaxIndex(int index) {
        mMaxIndex = index;
    }


    @Override
    public void reset() {
        if (Call == null)
            return;

        Call.loading();
        removeAllViews();
        init();
        Call.loadFirstChapter();
        //Log.e(TAG,"reset()");
    }


    @Override
    public Content getContent() {
        return mContent;
    }


    @Override
    public Content getPreContent() {
        return mPreContent;
    }


    @Override
    public Content getNextContent() {
        return mNextContent;
    }


    @Override
    public int getIndex() {
        return mContentIndex;
    }


    public void initCall(){
        if (Call == null)
            return;

        Call.loadFirstChapter();
    }


    private boolean hasOtherView(){
        mChildCount = getChildCount();
        //Log.e(TAG,"NOW CHILD VIEW COUNT = " + mChildCount);
        View child;
        for (int i = 0; i < mChildCount; i++){
            child = getChildAt(i);
            if (child == null)
                continue;
            if (child.getTag(R.id.view_index) == null)
                continue;
            if ((int) child.getTag(R.id.view_index) != mContentIndex){
                return true;
            }
        }
        return false;
    }


    private boolean isLastView(ReadTextView tv){
        if (tv == null)
            return false;
        if (tv.getTag(R.id.text_offset) == null)
            return false;
        if ((int) tv.getTag(R.id.text_offset) + tv.getText().length() == mTextLen){
            return true;
        }
        return false;
    }


    private View getPreView(){
        mChildCount = getChildCount();
        View child;
        for (int i = mChildCount - 1; i >= 0; i--){
            child = getChildAt(i);
            if ((int) child.getTag(R.id.view_index) < mContentIndex){
                return child;
            }
        }
        return null;
    }


    private View getNextView(){
        mChildCount = getChildCount();
        View child;
        for (int i = mChildCount - 1; i >= 0; i--){
            child = getChildAt(i);
            if (child == null)
                continue;
            if ((int) child.getTag(R.id.view_index) > mContentIndex){
                return child;
            }
        }
        return null;
    }


    private View findViewByPosition(int position,List<ReadTextView> list){
        for (ReadTextView view : list){
            if ((int) view.getTag(R.id.view_position) == position){
                return view;
            }
        }
        return null;
    }


    private int findIndexByPosition(int position,int contentIndex){
        View child;
        for (int i = 0; i < mChildCount; i++){
            child = getChildAt(i);
            if (child == null)
                continue;

            if ((int)child.getTag(R.id.view_position) == position && (int) child.getTag(R.id.view_index) == contentIndex){
                return i;
            }
        }
        return -1;
    }


    private int findIndexOfRemoveOtherView(){
        View child;

        for (int i = mChildCount - 1; i >= 0; i--){
            child = getChildAt(i);
            if (child == null)
                continue;
            //Log.e(TAG,"REMOVE >> " + child.getTag(R.id.view_index) + " *** " + i);
            if ((int) child.getTag(R.id.view_index) != mContentIndex){
                return i;
            }
        }
        return 0;
    }


    private int findIndexOfRemoveByDESC(){
        View child;
        for (int i = mChildCount - 1; i >= 0; i--){
            child = getChildAt(i);
            if (child == null)
                continue;
            if ((int) child.getTag(R.id.view_index) == mContentIndex){
                return i;
            }
        }
        return -1;
    }


    private int getLastOtherViewIndex(){
        View child;
        int index;
        for (int i = mChildCount - 1; i >= 0; i--){
            child = getChildAt(i);
            if (child == null)
                continue;
            index = (int) child.getTag(R.id.view_index);
            if (index != mContentIndex){
                return index;
            }
        }
        return -1;
    }


    private View getLastView(){
        //mChildCount = getChildCount();
        View child;
        for (int i = mChildCount - 1; i >= 0; i--){
              child = getChildAt(i);
              if ((int) child.getTag(R.id.view_index) == mContentIndex){
                  return child;
              }
        }
        return null;
    }


    @Override
    void firstLoadView() {

    }

    @Override
    void handleSlideLeftToZero() {

    }

    @Override
    void handleSlideLeftToNegativeWidth() {

    }

    @Override
    void handleSlideRightToZero() {

    }

    @Override
    void handlerSlideRightToWidth() {

    }

    @Override
    void handleSlideBackToZero() {

    }

    @Override
    void handleSlideBackToWidth() {

    }

    @Override
    void handleSlideBackToNegativeWidth() {

    }
}
