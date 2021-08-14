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
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.wong.novel.R;
import com.wong.novel.bean.Content;

import java.util.ArrayList;
import java.util.List;

public class FinalReadView extends ViewGroup {

    private static final String TAG = "FinalReadView";

    private static final int FLAG_NEW_VIEW          = 0x01,
                             FLAG_SLIDE_LEFT_ZERO   = 0x02,
                             FLAG_SLIDE_LEFT_NEGATIVE_WIDTH = 0x03,
                             FLAG_SLIDE_RIGHT_ZERO  = 0x04,
                             FLAG_SLIDE_RIGHT_WIDTH = 0x05,
                             FLAG_SLIDE_BACK_WIDTH  = 0x06,
                             FLAG_SLIDE_BACK_ZERO   = 0x07,
                             FLAG_SLIDE_BACK_NEGATIVE_WIDTH = 0x08,
                             FLAG_FIRST_START_LOAD  = 0x09,
                             FLAG_VIEW_INDEX        = 1,
                             FLAG_VIEW_POSITION     = 2,
                             FLAG_VIEW_TYPE         = 3,
                             FLAG_LAYOUT_LEFT       = 1,
                             FLAG_LAYOUT_RIGHT      = 2,
                             DEFAULT_TEXT_SIZE      = 18,
                             DEFAULT_LOAD_PRE       = 2,
                             DEFAULT_LOAD_MAX_PAGE  = 5,
                             DEFAULT_LOAD_PAGE      = 3;

    public static final int FLAG_LOAD_PRE  = 1,
                            FLAG_LOAD_NEXT = 2;

    private Context mContext;

    // ...
    private int mWidth,                     // 宽
                mHeight,                    // 高
                mChildCount,                // 子View数量
                mContentIndex,              // 当前Content下标
                mMaxContentIndex,           // 最大Content下标(主要是判断是否为最后一节最后一页)
                mContentTextLength,         // 当前Content文字数量
                mContentTextStartIndex,     // 当前Content文本开始下标
                mPreContentTextLength,      // 前一Content文字数量
                mPreContentTextStartIndex,  // 前一Content文本开始下标
                mNextContentTextLength,     // 后一Content文字数量
                mNextContentTextStartIndex, // 后一Content文本开始下标
                mFirstAlreadyLoadPage,      // 第一次加载已经加载的View数量
                mDefaultContentLoadPage,    // 默认当前Content需要加载View的数量(默认为3)
                mPreViewStartIndex,         // 上一节View开始下标
                mPreViewCount,              // 上一节View数量
                mViewLoadForFrontPosition,  // 向前加载最后的位置
                mPreViewLoadPosition,       // 前一章加载最后的位置
                mLastCatchViewPosition,     // 上一次捕捉View的Position
                mPageIndex;                 // 页码(默认为1)


    private float mX,
                  mDownX,
                  mLastX,
                  mViewOffsetX,
                  mTouchSlop;

    private int mLayoutLeftX,
                mLayoutRightX,
                mLimitDistance,
                mDistance;

    // 一些属性
    private boolean isFirstLoadView,             // 是否为第一次加载View
                    isFirstLoadViewCompleted,    // 第一次加载View是否完成
                    isFirstLoadPreViewCompleted, // 第一次加载PreView是否完成
                    isFirstLoadContentFinish,    // 第一次加载Content完成
                    isFirstLoadNextContentFinish,// 第一次加载下一Content完成
                    isFirstLoadPreContentFinish, // 第一次加载上一Content完成
                    isLoadPreView,               // 是否加载上一章View
                    isMeasured,                  // 是否已测量过
                    isLast,                      // 是否为最后一章
                    isSliding,                   // View是否在滑动
                    isIntercept,                 // 是否阻止View响应touch事件
                    isSureDirection,             // 是否确定了滑动方向
                    isChangingPreContent,        // 是否正在切换到上一章
                    isContentViewLoadFinish;     // Content View 全部加载完成

    // 章节正文
    private Content mContent,
                    mPreContent,
                    mNextContent;

    // 章节文字的偏移
    private List<Integer> mContentOffset,
                          mPreContentOffset;

    // 缓存View
    private List<ReadTextView> mViewList,
                               mPreViewList,
                               mNextViewList;

    // 需要用到的View
    private View mCatchView,    // 捕捉需要滑动的View
                 mRemoveView,   // 需要移除的View
                 mCacheView,
                 mLayoutView;   // 布局的View

    private ViewGroup mParent;  // 移除View的老爸

    // 文本View
    private ReadTextView mTextView;

    // 测量文本工具View
    private ReadTextView.ReadTextMeasureView mMeasureView;

    // 布局样式
    private LayoutParams mTVLP;

    // 滑动方向
    private enum SlideDirection{
        Left,
        Right
    }
    private SlideDirection mDirection;

    // 加载方向
    private enum LoadWay{
        Front,
        After
    }
    private LoadWay mLoadWay;


    // Handler，主要处理创建View ~ 自滑
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case FLAG_NEW_VIEW:
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
                    break;

                case FLAG_SLIDE_RIGHT_ZERO:
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

                case FLAG_SLIDE_BACK_WIDTH:
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

                case FLAG_SLIDE_BACK_ZERO:
                    if (mViewOffsetX - mDistance < 0){
                        mDistance = 1;
                    }
                    mViewOffsetX -= mDistance;
                    mCatchView.layout(0 + (int) mViewOffsetX,0,mWidth + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideBackToZero();
                    break;

                case FLAG_SLIDE_BACK_NEGATIVE_WIDTH:
                    if (mViewOffsetX - mDistance < 0){
                        mDistance = 1;
                    }
                    mViewOffsetX -= mDistance;
                    mCatchView.layout(-mWidth + (int) mViewOffsetX,0,0 + (int) mViewOffsetX,mHeight);
                    invalidate();
                    slideBackToNegativeWidth();
                    break;

                case FLAG_FIRST_START_LOAD:
                    if (isFirstLoadContentFinish && isFirstLoadPreContentFinish){
                        setContentView();
                    }
                    break;
            }
        }
    };


    public FinalReadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init(){
        mDefaultContentLoadPage = 3;
        mPageIndex              = 1;
        isFirstLoadView         = true;
        mTouchSlop              = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mLoadWay                = LoadWay.After;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (isMeasured){
            return;
        }
        mWidth         = MeasureSpec.getSize(widthMeasureSpec);
        mHeight        = MeasureSpec.getSize(heightMeasureSpec);
        mLimitDistance = mWidth / 2 / 2;
        isMeasured     = true;
        //Log.d(TAG,"onMeasure() >> " + mWidth + " - " + mHeight);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mChildCount = getChildCount();

        if (mChildCount == 0)
            return;

        mLayoutView = getChildAt(mChildCount - 1);
        //Log.d(TAG,"onLayout() >> " + mLayoutView.getTag(R.id.view_type));
        Log.d(TAG,"onLayout() ChildCount >> " + mChildCount + " && ->" + isFirstLoadViewCompleted + " , " + isFirstLoadPreViewCompleted);
        // 第一页
        if (mChildCount - 1 == 0){
            Log.d(TAG,"onLayout() >> 现在是第一页，放在中间显示！！！");
            mLayoutView.layout(l, t, r, b);
        }
        // 其余页
        else{
            switch ((int) mLayoutView.getTag(R.id.view_type)){
                case FLAG_LAYOUT_LEFT:
                    //Log.d(TAG,"onLayout() >> Here layout Left && " + mChildCount);
                    mLayoutView.layout(-mWidth,t,0,b);
                    break;

                case FLAG_LAYOUT_RIGHT:
                    mLayoutView.layout(r,t,r + mWidth,b);
                    //Log.d(TAG,"onLayout() >> Here layout Right && " + mChildCount);
                    break;
            }
        }


        if (isFirstLoadViewCompleted && isFirstLoadPreViewCompleted){
            if (Call == null)
                return;

            //Log.d(TAG,"Completed");
            Call.complete();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(TAG,isSliding + " >> " + mPageIndex + " >> " + mChildCount);
        if (isSliding)
            return false;

        float x = event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownX          = x;
                mLastX          = x;
                mDirection      = null;
                mCatchView      = null;
                isSureDirection = false;
                isIntercept     = false;

                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mLastX - mDownX) >= mTouchSlop){
                    mX = x - mLastX;

                    // 确定滑动方向
                    if (!isSureDirection){

                        // >> 右滑：上一页
                        if (mX > 0){
                            /*if (mPageIndex == 1){
                                if (mContentIndex == 0){
                                    // 第一章 第一页，不能右滑切换上一页
                                    isIntercept = true;
                                    return false;
                                    //Log.d(TAG,"程序走到了这里~");
                                }

                                // 当前章的第一页：切换到上一章最后一页
                                else {
                                    mCatchView           = getChildAt(mPreViewStartIndex);
                                    mDirection           = SlideDirection.Right;
                                    mLayoutLeftX         = -mWidth;
                                    mLayoutRightX        = 0;
                                    isSureDirection      = true;
                                    isChangingPreContent = true;
                                }
                            }
                            else{
                                if (mLoadWay == LoadWay.Front){
                                    mCatchView      = getChildAt(mPageIndex);
                                    //Log.d(TAG,">> CatchIndex:"+mPageIndex);
                                    mDirection      = SlideDirection.Right;
                                    mLayoutLeftX    = -mWidth;
                                    mLayoutRightX   = 0;
                                    isSureDirection = true;
                                }
                                else{
                                    mCatchView      = getChildAt(mPageIndex);
                                    mDirection      = SlideDirection.Right;
                                    mLayoutLeftX    = 0;
                                    mLayoutRightX   = mWidth;
                                    isSureDirection = true;
                                }
                            }*/


                            if (mPageIndex == 1){
                                if (mContentIndex == 0){
                                    isIntercept = true;
                                    return false;
                                }
                                else{
                                    mLoadWay             = LoadWay.Front;
                                    mPageIndex           = mPreViewStartIndex;
                                    isChangingPreContent = true;
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

                            switch (mLoadWay){
                                case After:
                                    if (mPageIndex >= mChildCount){
                                        isIntercept = true;
                                        return false;
                                    }
                                    mCatchView    = getChildAt(mPageIndex);
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
                        Log.d(TAG,"Distance >> " + mDistance);
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
                    if (isChangingPreContent || mLoadWay == LoadWay.Front){
                        mDistance = mWidth - (int) mViewOffsetX;
                        mDistance /= 10;
                        slideRightToZero();
                    }
                    else{
                        mDistance = mWidth - (int) mViewOffsetX;
                        mDistance /= 10;
                        slideRightToWidth();
                    }
                    break;
            }
        }
        // 滑动距离没有超过限制的距离，滚回~
        else{
            slideBack();
        }
    }


    /* CatchView 滑动最终的左边界是：0 */
    private void slideLeftToZero(){
        if (mViewOffsetX > -mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_LEFT_ZERO,1);
        }
        else{
            // Slide Finish
            mLoadWay = LoadWay.After;

            // Remove View
            if (mPreViewCount > 0){
                mRemoveView = getChildAt(mPreViewStartIndex + 1);
                mPreViewCount--;
            }
            else{
                mRemoveView = getChildAt(0);
            }
            removeViewForParent(mRemoveView);

            // Add View
            if (isContentViewLoadFinish){
                //Log.d(TAG,">> LoadNextView~");
                setNextContentView();
            }
            else{
                setContentView();
            }

            // ...
            isSliding    = false;
            mViewOffsetX = 0;
            mPageIndex++;
            if (mPageIndex >= mChildCount - 2){
                mPageIndex = mChildCount - 2;
            }


            // Change
            if ((int)mCatchView.getTag(R.id.view_index) != mContentIndex){
                mPreContent   = mContent;
                mContent      = mNextContent;
                mContentIndex = mContent.index;
                if (Call != null && mContentIndex != mMaxContentIndex){
                    Call.loadChapter(FLAG_LOAD_NEXT,mContentIndex + 1);
                }
                mPreViewList.clear();
                mPreViewList.addAll(mViewList);
                mViewList.clear();
                mViewList.addAll(mNextViewList);
                mNextViewList.clear();
                mContentTextStartIndex  = mNextContentTextStartIndex;
                mContentTextLength      = mNextContentTextLength;
                isContentViewLoadFinish = false;
                mLoadWay = LoadWay.After;
            }
        }
    }


    private void slideRightToZero(){
        if (mViewOffsetX < mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_RIGHT_ZERO,1);
        }
        else{
            //Log.d(TAG,"Slide Right To Zero Finish~");
            if ((int)mCatchView.getTag(R.id.view_index) != mContentIndex){
                isChangingPreContent = true;
            }

            if (isChangingPreContent){

                mNextContent  = mContent;
                mContent      = mPreContent;
                mContentIndex = mContent.index;
                if (Call != null && mContentIndex != 0){
                    isLoadPreView = false;
                    Call.loadChapter(FLAG_LOAD_PRE,mContentIndex - 1);
                }

                mNextViewList.clear();
                mNextViewList.addAll(mViewList);
                mViewList.clear();
                mViewList.addAll(mPreViewList);
                mPreViewList.clear();

                mNextContentTextLength = mContentTextLength;
                mContentTextLength     = mPreContentTextLength;
                mContentTextStartIndex = mPreContentTextStartIndex;

                isChangingPreContent = false;
                mLoadWay = LoadWay.Front;

                mContentOffset.clear();
                mContentOffset.addAll(mPreContentOffset);
                mPreContentOffset.clear();

                mViewLoadForFrontPosition = mPreViewLoadPosition;

                mRemoveView = getChildAt(mPreViewStartIndex - 1);
                removeViewForParent(mRemoveView);
                //removeViewAt(0);
                setContentView();

                isSliding = false;
                isContentViewLoadFinish = false;
                //Log.d(TAG,">> PageIndex:"+mPageIndex);
            }
            else{
                if (isContentViewLoadFinish){
                    if (mContentIndex != 0){
                        removeViewAt(0);
                        isLoadPreView = true;
                        setPreContentView();
                        //Log.d(TAG,">>" + mPreViewLoadPosition + " -- " + getChildCount() + " -- " + mPreContent + " -- " + mPreContentOffset);
                    }
                }
                else{
                    removeViewAt(0);
                    setContentView();
                }
            }

            mPageIndex   = mChildCount - 2;
            mViewOffsetX = 0;
            isSliding    = false;
        }
    }


    private void slideRightToWidth(){
        if (mViewOffsetX < mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_RIGHT_WIDTH,1);
        }
        else{
            mPageIndex = mPageIndex - 1 < 1 ? 1 : mPageIndex - 1;
            int position = (int) getChildAt(mPageIndex - 1).getTag(R.id.view_position);
            //Log.d(TAG,"Position >> " + position);

            if (position == 0){
                if (mPreViewList == null || mPreViewList.size() == 0)
                    return;

                removeViewAt(mChildCount - 1);
                mCacheView = mPreViewList.get(mPreViewList.size()-1);
                mPreViewCount++;
                addView(mCacheView);
                //Log.d(TAG,"After  Add() = " + getChildCount());
            }
            else{

            }

            isSliding = false;
            mViewOffsetX = 0;
        }
    }


    private void slideLeftToNegativeWidth(){
        if (mViewOffsetX > -mWidth){

        }
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
                    Log.d(TAG,"Back Distance >> " + mDistance);
                    slideBackToZero();
                }
                break;

            case Right:
                if (isChangingPreContent){
                    // 上一章最后一页：返回 => (-width,0)
                    isChangingPreContent = false;
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


    private void slideBackToZero(){
        if (mViewOffsetX > 0){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_ZERO,1);
        }
        else{
            isSliding = false;
        }
    }


    private void slideBackToWidth(){
        if (mViewOffsetX < 0){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_WIDTH,1);
        }
        else{
            isSliding = false;
        }
    }


    private void slideBackToNegativeWidth(){
        if (mViewOffsetX > 0){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_NEGATIVE_WIDTH,1);
        }
        else{
            isSliding = false;
        }
    }


    // 负责“当前Content”-View的创建
    private void setContentView(){

        if (mContent == null)
            return;

        if (mViewList == null)
            return;

        //Log.d(TAG,"setContentView() >> " + mContent.title + " >> " + mFirstAlreadyLoadPage + " >> " + mDefaultContentLoadPage + " >> " + isFirstLoadView);

        if (isFirstLoadView){
            //Log.d(TAG,"setContentView(), " + mFirstAlreadyLoadPage + " && " + mDefaultContentLoadPage + " && " + DEFAULT_LOAD_PAGE);

            if (mFirstAlreadyLoadPage < mDefaultContentLoadPage){
                mFirstAlreadyLoadPage++;
                mTextView = new ReadTextView(mContext,mContent.title);
                mTVLP     = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
                mTextView.setLayoutParams(mTVLP);
                mTextView.setText(mContent.content.substring(mContentTextStartIndex));
                mTextView.setTextSize(DEFAULT_TEXT_SIZE);
                mTextView.setTag(R.id.view_index,mContentIndex);
                mTextView.setTag(R.id.view_position,mViewList.size());
                mTextView.setTag(R.id.view_type,FLAG_LAYOUT_RIGHT);
                mViewList.add(mTextView);
                addView(mTextView);
                mTextView.setCall((int len) -> {
                    //Log.d(TAG,">>" + mContentTextStartIndex);
                    mContentTextStartIndex += len;
                    if (mContentTextStartIndex == mContentTextLength){
                        isContentViewLoadFinish = true;
                    }
                    else{
                        mHandler.sendEmptyMessage(FLAG_NEW_VIEW);
                    }
                    //Log.d(TAG,"setContentView >> createView");
                });
            }

            // 没有前一章，可以结束了~
            if (isFirstLoadPreViewCompleted){
                isFirstLoadViewCompleted = true;
                isFirstLoadView = false;
            }
            else{
                if (mDefaultContentLoadPage == DEFAULT_LOAD_PAGE){
                    isLoadPreView = true;
                    setPreContentView();
                }
                isFirstLoadViewCompleted = true;
                isFirstLoadView = false;
            }
        }
        else{
            switch (mLoadWay){
                // 向后加载
                case After:
                    mTextView = new ReadTextView(mContext,mContent.title);
                    mTVLP     = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
                    mTextView.setLayoutParams(mTVLP);
                    mTextView.setText(mContent.content.substring(mContentTextStartIndex));
                    mTextView.setTextSize(DEFAULT_TEXT_SIZE);
                    mTextView.setTag(R.id.view_index,mContentIndex);
                    mTextView.setTag(R.id.view_position,mViewList.size());
                    mTextView.setTag(R.id.view_type,FLAG_LAYOUT_RIGHT);
                    mViewList.add(mTextView);
                    //Log.d(TAG,"Position >> " + mViewList.size());
                    addView(mTextView);
                    mTextView.setCall((int len) -> {
                        //Log.d(TAG,">>" + mContentTextStartIndex);
                        mContentTextStartIndex += len;
                        if (mContentTextStartIndex == mContentTextLength){
                            isContentViewLoadFinish = true;
                        }
                    });
                    break;

                case Front:

                    if (mViewLoadForFrontPosition - 1 < 0){
                        return;
                    }

                    if (mViewLoadForFrontPosition - 1 == 0){
                        isContentViewLoadFinish = true;
                    }

                    mTextView = new ReadTextView(mContext,mContent.title);
                    mTextView.setTag(R.id.view_index,mContent.index);
                    mTextView.setTag(R.id.view_position,mViewLoadForFrontPosition);
                    mTextView.setTag(R.id.view_type,FLAG_LAYOUT_LEFT);
                    mTextView.setTextSize(DEFAULT_TEXT_SIZE);
                    mTextView.setText(mContent.content.substring(mContentOffset.get(mViewLoadForFrontPosition - 1),mContentOffset.get(mViewLoadForFrontPosition)));
                    mPreViewList.add(mTextView);
                    addView(mTextView);
                    mViewLoadForFrontPosition -= 1;
                    break;
            }
        }

    }


    // 负责“上一Content”-View的创建
    private void setPreContentView(){
        if (mPreContent == null)
            return;

        if (mPreContentOffset == null || mPreContentOffset.size() == 0)
            return;

        if (!isLoadPreView)
            return;

        mPreViewStartIndex = mChildCount;

        //Log.d(TAG,"setPreContentView, Child View Count >> " + getChildCount());

        if (!isFirstLoadPreViewCompleted){
            // 默认2页
            for (int i = 0; i < 2; i++){
                if (mPreViewLoadPosition - 1 < 0)
                    break;
                //Log.d(TAG,"End:" + end + " && " + mPreContent.content.substring(mPreContentOffset.get(end - 1),mPreContentOffset.get(end)) );
                mTextView = new ReadTextView(mContext,mPreContent.title);
                mTextView.setTag(R.id.view_index,mPreContent.index);
                mTextView.setTag(R.id.view_position,mPreViewLoadPosition);
                mTextView.setTag(R.id.view_type,FLAG_LAYOUT_LEFT);
                mTextView.setTextSize(DEFAULT_TEXT_SIZE);
                mTextView.setText(mPreContent.content.substring(mPreContentOffset.get(mPreViewLoadPosition - 1),mPreContentOffset.get(mPreViewLoadPosition)));
                mPreViewList.add(mTextView);
                addView(mTextView);
                mPreViewLoadPosition -= 1;
                mPreViewCount += 1;
            }
            isFirstLoadPreViewCompleted = true;
        }
        else{

            //Log.d(TAG,">> " + mPreViewLoadPosition + " --- " + isLoadPreView);
            if (mPreViewLoadPosition - 1 < 0)
                return;

            //if (!isLoadPreView)
            //    return;

            mTextView = new ReadTextView(mContext,mPreContent.title);
            mTextView.setTag(R.id.view_index,mPreContent.index);
            mTextView.setTag(R.id.view_position,mPreViewLoadPosition);
            mTextView.setTag(R.id.view_type,FLAG_LAYOUT_LEFT);
            mTextView.setTextSize(DEFAULT_TEXT_SIZE);
            mTextView.setText(mPreContent.content.substring(mPreContentOffset.get(mPreViewLoadPosition - 1),mPreContentOffset.get(mPreViewLoadPosition)));
            mPreViewList.add(mTextView);
            addView(mTextView);
            mPreViewLoadPosition -= 1;
            mPreViewCount += 1;
            //Log.d(TAG,"Add Pre View~");
        }
    }


    // 负责“下一Content”-View的创建
    private void setNextContentView(){
        if (mNextContent == null)
            return;

        if (mNextViewList == null)
            return;

        mTextView = new ReadTextView(mContext,mNextContent.title);
        mTextView.setTag(R.id.view_index,mNextContent.index);
        mTextView.setTag(R.id.view_position,mNextViewList.size());
        mTextView.setTag(R.id.view_type,FLAG_LAYOUT_RIGHT);
        mTextView.setTextSize(DEFAULT_TEXT_SIZE);
        mTextView.setText(mNextContent.content.substring(mNextContentTextStartIndex));
        mNextViewList.add(mTextView);
        addView(mTextView);
        mTextView.setCall((int len) -> {
            mNextContentTextStartIndex += len;
        });
    }


    // 负责移除View
    private void removeViewForParent(View view){
        if (view == null || view.getParent() == null)
            return;
        endViewTransition(view);
        view.clearAnimation();
        removeView(view);
    }


    public void setContent(Content content){
        mContent                 = content;
        mContentIndex            = content.index;
        mContentTextStartIndex   = 0;
        mContentTextLength       = content.content.length();
        mViewList                = new ArrayList<>();
        mContentOffset           = new ArrayList<>();
        isFirstLoadView          = true;
        isLoadPreView            = true;
        isFirstLoadContentFinish = true;
        isFirstLoadViewCompleted = isFirstLoadPreViewCompleted = isFirstLoadPreContentFinish = false;


        if (Call == null)
            return;

        // 需要加载正文的页数
        if (mContentIndex == 0){
            mDefaultContentLoadPage = DEFAULT_LOAD_MAX_PAGE;
        }
        else{
            mDefaultContentLoadPage = DEFAULT_LOAD_PAGE;
        }

        //setContentView();


        // 加载上一章
        if (mDefaultContentLoadPage == DEFAULT_LOAD_PAGE){
            Call.loadChapter(FLAG_LOAD_PRE,mContentIndex - 1);
        }
        else{
            isFirstLoadPreViewCompleted = true;
            isFirstLoadPreContentFinish = true;
        }

        // 加载下一章正文
        if (mContentIndex != mMaxContentIndex){
            Call.loadChapter(FLAG_LOAD_NEXT,mContentIndex + 1);
        }

        mHandler.sendEmptyMessageDelayed(FLAG_FIRST_START_LOAD,0);
        //Log.d(TAG,"ContentIndex >> " + mContentIndex + " --- " + getChildCount() + " --- " + mDefaultContentLoadPage);

    }


    public void setPreContent(Content content){

        if (content == null)
            return;

        //Log.d(TAG,content.title);
        mPreContent               = content;
        mPreContentOffset         = new ArrayList<>();
        mPreViewList              = new ArrayList<>();
        mPreContentTextLength     = content.content.length();
        mPreContentTextStartIndex = 0;
        mPreViewLoadPosition      = 0;
        mPreViewCount             = 0;
        mPreContentOffset.add(0);
        while (mPreContentTextLength != mPreContentTextStartIndex){
            //Log.d(TAG,"setPreContent() >> " + mPreContentTextStartIndex);
            mMeasureView = new ReadTextView.ReadTextMeasureView(mContext,content.content.substring(mPreContentTextStartIndex),DEFAULT_TEXT_SIZE);
            mPreContentTextStartIndex += mMeasureView.getLength();
            mPreContentOffset.add(mPreContentTextStartIndex);
        }
        mPreViewLoadPosition = mPreContentOffset.size() - 1;

        if (!isFirstLoadPreContentFinish){
            isFirstLoadPreContentFinish = true;
            mHandler.sendEmptyMessageDelayed(FLAG_FIRST_START_LOAD,0);
        }
        //Log.d(TAG,"setPreContent() >> " + mPreContentOffset.size() + " && " + mPreContentTextLength);
    }


    public void setNextContent(Content content){
        mNextContent               = content;
        mNextViewList              = new ArrayList<>();
        mNextContentTextLength     = content.content.length();
        mNextContentTextStartIndex = 0;
    }


    public void setMaxContentIndex(int index){
        mMaxContentIndex = index;
    }


    public void reset(){
        if (Call != null){
            removeAllViews();
            //Log.d(TAG,"Clear,now size = " + getChildCount());
            Call.loading();
            mPageIndex                  = 1;
            mFirstAlreadyLoadPage       = 0;
            mPreViewStartIndex          = 0;
            mPreViewCount               = 0;
            mChildCount                 = 0;
            mViewLoadForFrontPosition   = mPreViewLoadPosition = 0;
            isFirstLoadView             = true;
            isFirstLoadPreViewCompleted = isFirstLoadViewCompleted    = false;
            isFirstLoadContentFinish    = isFirstLoadPreContentFinish = false;
            isContentViewLoadFinish     = false;
            mContent  = mPreContent = mNextContent = null;
            mLoadWay  = LoadWay.After;
            Call.loadFirstChapter();
        }
    }


    public int getIndex(){
        return mContentIndex;
    }


    public Content getContent(){
        return mContent;
    }


    public Content getPreContent(){
        return mPreContent;
    }


    public Content getNextContent(){
        return mNextContent;
    }


    public void initCall(){
        if (Call != null){
            Call.loadFirstChapter();
        }
    }


    private Call Call;


    public void setCall(FinalReadView.Call call) {
        Call = call;
    }


    public interface Call{

        void loading();

        void complete();

        void click();

        void loadFirstChapter();

        void loadChapter(int flag,int index);
    }
}
