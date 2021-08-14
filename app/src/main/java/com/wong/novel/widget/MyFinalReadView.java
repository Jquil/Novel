package com.wong.novel.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.wong.novel.R;
import com.wong.novel.bean.Content;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyFinalReadView extends BaseReadView{

    private static final String TAG = "MyFinalReadView";

    private int mTextLen,
                mTextStartIndex,
                mNextTextLen,
                mNextTextStartIndex,
                mLayoutLeftX,
                mLayoutRightX,
                mOffsetPosition,
                mPreOffsetPosition,
                mViewPage,
                mPreViewPage,
                mHasLoadPage,
                mChildCount,
                mLastViewTextLen;

    private float mX,
                  mLastX,
                  mDownX,
                  mViewOffsetX,
                  mDistance,
                  mTouchSlop;

    private boolean isSliding,
                    isSureDirection,
                    mContentViewLoadFinish,
                    mViewLoadComplete,
                    mPreViewLoadComplete,
                    mChangeNextContent,
                    mChangePreContent;

    private LayoutParams mLP;

    private ReadTextView mReadTV;

    private Call Call;


    public MyFinalReadView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        mChildCount = getChildCount();
        if (mChildCount == 0)
            return;

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

        if (isFirstLoad && mViewLoadComplete && mPreViewLoadComplete){
            isFirstLoad = false;
            if (Call == null)
                return;
            Call.complete();
        }
        //Log.e(TAG,"onLayout(),ChildCount = " + mChildCount);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSliding){
            Log.e(TAG,"onTouchEvent::Sliding!");
            return false;
        }
        float x = event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownX          = x;
                mLastX          = x;
                mCatchView      = null;
                mDirection      = null;
                isSureDirection = false;
                //Log.e(TAG,"onTouchEvent::ChildCount = " + getChildCount());
                break;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(mLastX - mDownX) >= mTouchSlop){
                    mX = x - mLastX;

                    // 确定滑动方向
                    if (!isSureDirection && mCatchView == null){

                        // >> 右滑：上一页
                        if (mX > 0){
                            if (mPageIndex == 1){
                                if (mContentIndex == 0){
                                    Log.e(TAG,"onTouchEvent::Intercept => It's first page!");
                                    return false;
                                }
                                else{
                                    // 切换到上一章：从前向后捕捉
                                    mLoadWay   = LoadWay.Front;
                                    mPageIndex = getFirstPreViewIndex();
                                }
                            }
                            switch (mLoadWay){
                                case Front:
                                    mCatchView      = getChildAt(mPageIndex);
                                    mLayoutLeftX    = -mWidth;
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
                                        Log.e(TAG,"onTouchEvent::Left => PageIndex > mChildCount");
                                        return false;
                                    }
                                    // View to (0,Width)
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
                if (Math.abs(mLastX - mDownX) >= mTouchSlop){
                    up();
                }
                else{
                    if (Call != null) {
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

        isSliding = true;
        if (Math.abs(mViewOffsetX) >= mLimitDistance){
            switch (mDirection){
                case Left:
                    if (mLoadWay == LoadWay.Front){
                        mDistance = (int) mViewOffsetX / 10;
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
                    break;
            }
        }
        else{
            slideBack();
        }
    }


    private void slideBack(){
        switch (mDirection){
            case Left:
                // >> 下一页：返回 => (width,0)，令 Offset = 0
                mDistance = - (int) mViewOffsetX / 10;
                if (mLoadWay == LoadWay.After){
                    slideBackToWidth();
                }
                else{
                    Log.e(TAG,"slideBack()::BackToZero()");
                    slideBackToZero();
                }
                break;

            case Right:
                if ((int)mCatchView.getTag(R.id.view_index) < mContentIndex){
                    mChangePreContent = true;
                }

                if (mChangePreContent || mLoadWay == LoadWay.Front){
                    // 上一章最后一页：返回 => (-width,0)
                    mChangePreContent = false;
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
    void init() {
        mLoadReady   = 1;
        mPageIndex   = 1;
        mViewOffsetX = 0;
        mViewPage    = 3;
        mPreViewPage = 2;
        mHasLoadPage = 0;
        mTouchSlop   = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mLoadWay     = LoadWay.After;
        isStarted    = mContentViewLoadFinish = mChangeNextContent =
        isSliding    = mViewLoadComplete = mPreViewLoadComplete =
        mChangePreContent = false;
        isFirstLoad  = true;
        mContent     = mPreContent = mNextContent = null;
    }


    @Override
    void firstLoadView() {
        if (mContent == null)
            return;

        if (!isFirstLoad)
            return;

        //Log.e(TAG,"firstLoadView(), HasLoadPage = " + mHasLoadPage + ",ViewPage = " + mViewPage);
        if (mHasLoadPage < mViewPage){
            mReadTV = new ReadTextView(mContext,mContent.title);
            mLP = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
            mReadTV.setLayoutParams(mLP);
            mReadTV.setTag(R.id.view_position,mViewList.size());
            mReadTV.setTag(R.id.view_index,mContent.index);
            mReadTV.setTag(R.id.view_type,LoadType.Right);
            mReadTV.setTag(R.id.text_offset,mTextStartIndex);
            mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
            mReadTV.setText(mContent.content.substring(mTextStartIndex));
            mViewList.add(mReadTV);
            addView(mReadTV);
            mHasLoadPage++;
            mReadTV.setCall((int len) -> {
                mTextStartIndex += len;
                //Log.e(TAG,"firstLoadView,TextStartIndex = " + mTextStartIndex);
                if (mTextStartIndex == mTextLen){
                    mContentViewLoadFinish = true;
                }
                else{
                    //Log.e(TAG,"firstLoadView()，递归，" + mContent + " -- " + isFirstLoad);
                    myHandler.post(() -> {
                        firstLoadView();
                    });
                }
                //Log.e(TAG,"firstLoadView(), TextStartIndex = " + mTextStartIndex + "，TextLength = " + mTextLen);
            });
            if (mHasLoadPage == mViewPage){
                mViewLoadComplete = true;
            }
        }
        else{
            if (mViewPage == 5){
                /*todo*/
            }
            else{
                for (int i = 0; i < mPreViewPage; i++){
                    mReadTV = new ReadTextView(mContext,mPreContent.title);
                    mLP = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
                    mReadTV.setLayoutParams(mLP);
                    mReadTV.setTag(R.id.view_position,mPreOffsetPosition - 1);
                    mReadTV.setTag(R.id.view_index,mPreContent.index);
                    mReadTV.setTag(R.id.view_type,LoadType.Left);
                    mReadTV.setTag(R.id.text_offset,mPreTextOffsetList.get(mPreOffsetPosition - 1));
                    mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
                    mReadTV.setText(mPreContent.content.substring(mPreTextOffsetList.get(mPreOffsetPosition - 1),mPreTextOffsetList.get(mPreOffsetPosition)));
                    mPreViewList.add(mReadTV);
                    addView(mReadTV);
                    mPreOffsetPosition -= 1;
                }
                mPreViewLoadComplete = true;
            }
        }
    }


    @Override
    void setContentView() {
        switch (mLoadWay){
            case Front:
                if (mOffsetPosition - 1 < 0)
                    return;
                mReadTV = new ReadTextView(mContext,mContent.title);
                mReadTV.setTag(R.id.view_index,mContentIndex);
                mReadTV.setTag(R.id.view_type,LoadType.Left);
                mReadTV.setTag(R.id.view_position,mOffsetPosition - 1);
                mReadTV.setTag(R.id.text_offset,mTextOffsetList.get(mOffsetPosition - 1));
                mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
                mReadTV.setText(mContent.content.substring(mTextOffsetList.get(mOffsetPosition - 1),mTextOffsetList.get(mOffsetPosition)));
                mViewList.add(mReadTV);
                addView(mReadTV);
                mOffsetPosition -= 1;
                if (mOffsetPosition == 0){
                    mContentViewLoadFinish = true;
                }
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
                    if (mTextStartIndex == mTextLen){
                        mContentViewLoadFinish = true;
                    }
                });
                break;
        }
    }


    @Override
    void setPreContentView() {
        if (mPreOffsetPosition - 1 < 0)
            return;
        mReadTV = new ReadTextView(mContext,mPreContent.title);
        mLP     = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        mReadTV.setLayoutParams(mLP);
        mReadTV.setTag(R.id.view_index,mPreContent.index);
        mReadTV.setTag(R.id.view_type,LoadType.Left);
        mReadTV.setTag(R.id.view_position,mPreOffsetPosition - 1);
        mReadTV.setTag(R.id.text_offset,mPreTextOffsetList.get(mPreOffsetPosition - 1));
        mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
        mReadTV.setText(mPreContent.content.substring(mPreTextOffsetList.get(mPreOffsetPosition - 1),mPreTextOffsetList.get(mPreOffsetPosition)));
        mPreOffsetPosition -= 1;
        mPreViewList.add(mReadTV);
        addView(mReadTV);
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
            if (mChangeNextContent){
                mTextStartIndex    = mNextTextStartIndex;
                mChangeNextContent = false;
            }
        });
    }


    @Override
    void handleSlideLeftToZero() {
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
    }


    @Override
    void slideLeftToZero() {
        /*
        * 切换下一页
        * 1. 更改PageIndex
        * 2. 移除
        *     3.1 优先移除上一章最后一页 (start from mChildCount - 1)
        *     3.2 没有上一章视图，则移除 Index = 0' View
        * 3. 添加
        *     4.1 默认添加该章节的下一页，先从缓存中寻找 (catch last view, find this position + 1’s View)
        *     4.2 没有缓存，setContentView
        *     4.3 mChildCount -1's View 已经是最后一页 => setNextContentView
        * 4. 切换章节
        * 5. 保持LoadWay = After
        * */
        if (mViewOffsetX > -mWidth){
            myHandler.sendEmptyMessageDelayed(FLAG_SLIDE_LEFT_ZERO,1);
        }
        else{
            mPageIndex   = mPageIndex + 1 > 3 ? 3 : mPageIndex + 1;
            isSliding    = false;
            mLoadWay     = LoadWay.After;
            mViewOffsetX = 0;
            mLastView    = getLastView();
            if (mLastView == null){
                Log.e(TAG,"slideLeftToZero()::LastView is Null");
            }
            mRemoveView  = getLastPreView();
            if (mRemoveView == null){
                mRemoveView = getChildAt(0);
            }
            removeViewForParent(mRemoveView);

            mLastViewTextLen = (int) mLastView.getTag(R.id.text_offset) + ((ReadTextView)mLastView).getText().length();
            if (mContentViewLoadFinish || mLastViewTextLen == mTextLen){
                setNextContentView();
            }
            else{
                mCacheView = getCacheViewByPosition((int) mLastView.getTag(R.id.view_position) + 1,mViewList);
                if (mCacheView == null){
                    setContentView();
                }
                else{
                    addView(mCacheView);
                }
            }


            if ((int) mCatchView.getTag(R.id.view_index) > mContentIndex){
                // ChangeContent
                mLoadWay      = LoadWay.After;
                mPageIndex    = 1;
                mPreContent   = mContent;
                mContent      = mNextContent;
                mContentIndex = mContent.index;
                if (mPreViewList == null){
                    mPreViewList = new ArrayList<>();
                }
                mPreViewList.clear();
                mPreViewList.addAll(mViewList);
                mViewList.clear();
                mViewList.addAll(mNextViewList);
                mTextLen               = mNextTextLen;
                mTextStartIndex        = mNextTextStartIndex;
                mContentViewLoadFinish = false;
                mChangeNextContent     = true;  // 主要用来处理TextStartIndex(Next 赋给 Now)
                if (mContentIndex != mMaxIndex){
                    if (Call == null)
                        return;
                    Call.loadChapter(FLAG_NEXT,mContentIndex + 1);
                }

                changePreViewIndex();
            }


        }
    }


    @Override
    void handleSlideLeftToNegativeWidth() {
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
    }


    @Override
    void slideLeftToNegativeWidth() {
        /*
        *  LoadWay = Front 的 下一页
        * 1. 切换章节
        *   1.1 通过CatchView文字长度
        * 2. 移除View
        *   2.1 移除当前章节的第一页,因为第一页是位于顶部，所以(getLastView)
        * 3. 添加View
        *   3.1 ZeroView Position + 1
        *   3.2 如果 Index == 最后一页，添加下一章的第三页 Position = 2
        *   3.3 如果 ZeroView index != mContentIndex，addView Position = mZeroViewPos - 1
        *   3.4 addView Index也有要求
        * 4. PageIndex 更改为1
        * */
        if (mViewOffsetX > -mWidth){
            myHandler.sendEmptyMessageDelayed(FLAG_SLIDE_LEFT_NEGATIVE_WIDTH,1);
        }
        else {
            isSliding    = false;
            mViewOffsetX = 0;
            mRemoveView  = getLastView();
            removeViewForParent(mRemoveView);

            int len = (int) mCatchView.getTag(R.id.text_offset) + ((ReadTextView)mCatchView).getText().length();
            if (len == mContent.content.length()){
                // 切换到下一章
                mPreContent   = mContent;
                mContent      = mNextContent;
                mContentIndex = mContent.index;
                mLoadWay      = LoadWay.After;
                mPageIndex    = 1;
                if (mPreViewList == null){
                    mPreViewList = new ArrayList<>();
                }
                mPreViewList.addAll(mViewList);
                if ((int) mPreViewList.get(0).getTag(R.id.view_position) != 0){
                    Collections.reverse(mPreViewList);
                    //Log.e(TAG,"slideLeftToNegativeWidth()::Reverse -> PreViewList，nowLastItemPos = " + mPreViewList.get(mPreViewList.size()-1).getTag(R.id.view_position));
                }
                mViewList.clear();
                mViewList.addAll(mNextViewList);
                mNextViewList.clear();

                mPreOffsetPosition = mOffsetPosition;
                mPreTextOffsetList.clear();
                mPreTextOffsetList.addAll(mTextOffsetList);
                mTextOffsetList.clear();

                mLastView  = getLastView();
                mCacheView = getCacheViewByPosition((int) mLastView.getTag(R.id.view_position) + 1,mViewList);
                if (mCacheView == null){
                    setContentView();
                }
                else{
                    addView(mCacheView,2);
                }

                if (mContentIndex != mMaxIndex){
                    if (Call != null){
                        Call.loadChapter(FLAG_NEXT,mContentIndex + 1);
                    }
                }
            }
            else{
                mZeroView = getFirstView();
                if (mZeroView == null){
                    Log.e(TAG,"slideLeftToNegativeWidth()::ContentFirstView is Null!");
                }
                else{
                    len = (int) mZeroView.getTag(R.id.text_offset) + ((ReadTextView)mZeroView).getText().length();
                    if (len == mContent.content.length()){
                        // 添加下一章View
                        mCacheView = getLastNextView();
                        if (mCacheView == null){
                            mCacheView = getCacheViewByPosition(0,mNextViewList);
                            if (mCacheView == null){
                                Log.e(TAG,"slideLeftToNegativeWidth()::NextCacheView is Null");
                            }
                            else{
                                addView(mCacheView,0);
                            }
                        }
                        else{
                            int pos = (int) mCacheView.getTag(R.id.view_position);
                            mCacheView = getCacheViewByPosition(pos + 1,mNextViewList);
                            if (mCacheView == null){
                                Log.e(TAG,"slideLeftToNegativeWidth()::NextCacheView is Null");
                            }
                            else{
                                addView(mCacheView,1);
                            }
                        }
                    }
                    else{
                        mCacheView = getCacheViewByPosition((int) mZeroView.getTag(R.id.view_position) + 1,mViewList);
                        if (mCacheView == null){
                            Log.e(TAG,"slideLeftToNegativeWidth()::CacheView is Null!");
                        }
                        else{
                            addView(mCacheView,0);
                        }
                    }
                }
            }
            Log.e(TAG,">> CacheView::pos = " + mCacheView.getTag(R.id.view_position) + ",index = " + mCacheView.getTag(R.id.view_index));
        }
    }


    @Override
    void handleSlideRightToZero() {
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
    }


    @Override
    void slideRightToZero() {
        /*
        * 切换上一页：
        * 1. 移除View
        *   1.1 移除 index 分类别：
        *      1.1.1 After Index = mChildCount - 1
        *      1.1.2 Front Index = 0
        *   1.2 如果是切换到前一章，则是移除原当前章的最后一页
        * 2. 添加View
        *   2.1 添加的View Index = 0 (优先从缓存中获取)
        *   2.2 如果ZeroViewPosition = 0,表示需要添加上一章的视图了。直接addView到顶层即可
        * 3. 切换章节 (CatchView)
        * 4. PageIndex 不变
        * 5. 倒转PreViewList,保存最后一个Item是最后一页
        * */
        if (mViewOffsetX < mWidth){
            myHandler.sendEmptyMessageDelayed(FLAG_SLIDE_RIGHT_ZERO,1);
        }
        else{
            if ((int) mCatchView.getTag(R.id.view_index) < mContentIndex){
                /* 切换章节 */
                mLoadWay      = LoadWay.Front;
                mNextContent  = mContent;
                mContent      = mPreContent;
                mContentIndex = mContent.index;

                if (mNextViewList == null){
                    mNextViewList = new ArrayList<>();
                }
                mNextViewList.clear();
                mNextViewList.addAll(mViewList);
                if ((int) mViewList.get(mViewList.size()-1).getTag(R.id.view_position) == 0){
                    // 需要反转 => 最后一页为真正的最后一页
                    Collections.reverse(mNextViewList);
                    //Log.e(TAG,"slideRightToZero(),ReverseNextViewList");
                }
                mViewList.clear();
                mViewList.addAll(mPreViewList);
                mPreViewList.clear();
                if (mPreTextOffsetList == null){
                    mPreTextOffsetList = new ArrayList<>();
                }
                mTextOffsetList.clear();
                mTextOffsetList.addAll(mPreTextOffsetList);
                mPreTextOffsetList.clear();
                mOffsetPosition = mPreOffsetPosition;
                mContentViewLoadFinish = false;

                if (mContentIndex != 0){
                    if (Call != null){
                        Call.loadChapter(FLAG_PRE,mContentIndex - 1);
                    }
                }

                mRemoveView = getLastNextView();
                removeViewForParent(mRemoveView);

                mCacheView = getLastView();
                mCacheView = getCacheViewByPosition((int) mCacheView.getTag(R.id.view_position) - 1,mViewList);
                if (mCacheView != null){
                    addView(mCacheView);
                }
                else{
                    setContentView();
                }
            }
            else{
                switch (mLoadWay){
                    case After:
                        mZeroView   = getChildAt(0);
                        mRemoveView = getLastNextView();
                        mLastView   = getLastPreView();
                        if (mRemoveView == null){
                            mRemoveView = getChildAt(mChildCount - 1);
                        }
                        removeViewForParent(mRemoveView);
                        if ((int) mZeroView.getTag(R.id.view_position) == 0 && (int) mZeroView.getTag(R.id.view_index) == mContentIndex){
                            // 添加上一章最后一页
                            mCacheView = mPreViewList.get(mPreViewList.size() - 1);
                            if (mCacheView == null){
                                Log.e(TAG,"slideRightToZero()::PreContentOtherCacheView is None!");
                            }
                            else{
                                addView(mCacheView);
                            }
                        }
                        else if (mLastView != null){
                            // 添加上一章其他页
                            mCacheView = getCacheViewByPosition((int) mLastView.getTag(R.id.view_position) - 1,mPreViewList);
                            if (mCacheView == null){
                                Log.e(TAG,"slideRightToZero()::PreContentOtherCacheView is None!");
                            }
                            else{
                                addView(mCacheView);
                            }
                        }
                        else{
                            mCacheView = getCacheViewByPosition((int) mZeroView.getTag(R.id.view_position) - 1,mViewList);
                            if (mCacheView == null){
                                Log.e(TAG,"slideRightToZero()::ContentCacheView is None!");
                            }
                            else{
                                addView(mCacheView);
                            }
                        }
                        break;

                    case Front:
                        mRemoveView = getLastNextView();
                        if (mRemoveView == null){
                            mRemoveView = getChildAt(0);
                        }
                        mLastView  = getLastView();
                        if ((int) mLastView.getTag(R.id.view_position) == 0){
                            // 避免在“处于文章第一章节向前切换”的情况下，不加载上一章View却又删除View
                            if (mContentIndex != 0){
                                removeViewForParent(mRemoveView);
                                setPreContentView();
                            }
                        }
                        else{
                            removeViewForParent(mRemoveView);
                            mCacheView = getCacheViewByPosition((int)mLastView.getTag(R.id.view_position) - 1,mViewList);
                            if (mCacheView == null){
                                setContentView();
                            }
                            else{
                                addView(mCacheView);
                            }
                        }
                        break;
                }
            }
            isSliding    = false;
            mViewOffsetX = 0;
        }
    }


    @Override
    void handlerSlideRightToWidth() {
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
    }


    @Override
    void slideRightToWidth() {
        /*
        *  针对LoadWay = After 的 上一页
        * 1. 移除View
        *   1.1 优先移除预加载的下一章节的View
        *   1.2 齐次就是LastView
        * 2. 添加View
        *   2.1 根据ZeroView Pos
        *   2.2 当ZeroView pos == 0,预加载上一章节View
        * 3. 不需要处理切换章节
        * 4. 调整PageIndex
        * */
        if (mViewOffsetX < mWidth){
            myHandler.sendEmptyMessageDelayed(FLAG_SLIDE_RIGHT_WIDTH,1);
        }
        else{
            isSliding = false;
            mViewOffsetX = 0;
            mRemoveView = getLastNextView();
            if (mRemoveView == null){
                mRemoveView = getLastView();
            }

            removeViewForParent(mRemoveView);
            mZeroView = getFirstView();
            if ((int) mZeroView.getTag(R.id.view_position) == 0){
                mCacheView = getLastPreView();
                if (mCacheView == null){
                    mCacheView = getLastCachePreView();
                    if (mCacheView == null){
                        /*todo*/
                    }
                    else{
                        addView(mCacheView);
                    }
                }
                else{
                    mCacheView = getCacheViewByPosition((int) mCacheView.getTag(R.id.view_position) - 1,mPreViewList);
                    addView(mCacheView);
                }
                mPageIndex = mPageIndex - 1 < 0 ? 1 : mPageIndex - 1;
            }
            else{
                mCacheView = getCacheViewByPosition((int) mZeroView.getTag(R.id.view_position) - 1,mViewList);
                addView(mCacheView,0);
            }

        }
    }


    @Override
    void handleSlideBackToZero() {
        if (mCatchView == null)
            return;

        if (mViewOffsetX + mDistance > 0){
            mDistance = 1;
        }
        mViewOffsetX += mDistance;
        mCatchView.layout(0 + (int) mViewOffsetX,0,mWidth + (int) mViewOffsetX,mHeight);
        invalidate();
        slideBackToZero();
    }


    @Override
    void slideBackToZero() {
        if (mViewOffsetX < 0){
            myHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_ZERO,1);
        }
        else{
            isSliding = false;
            mViewOffsetX = 0;
        }
    }


    @Override
    void handleSlideBackToWidth() {
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
    }


    @Override
    void slideBackToWidth() {
        if (mViewOffsetX < 0){
            myHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_WIDTH,1);
        }
        else{
            isSliding = false;
            mViewOffsetX = 0;
        }
    }


    @Override
    void handleSlideBackToNegativeWidth() {
        if (mCatchView == null)
            return;
        if (mViewOffsetX - mDistance < 0){
            mDistance = 1;
        }
        mViewOffsetX -= mDistance;
        mCatchView.layout(-mWidth + (int) mViewOffsetX,0,0 + (int) mViewOffsetX,mHeight);
        invalidate();
        slideBackToNegativeWidth();
    }


    @Override
    void slideBackToNegativeWidth() {
        if (mViewOffsetX > 0){
            myHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_NEGATIVE_WIDTH,1);
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
    public void setContent(Content content) {
        mContent        = content;
        mContentIndex   = mContent.index;
        mTextStartIndex = 0;
        mTextLen        = mContent.content.length();
        mViewList       = new ArrayList<>();
        mTextOffsetList = new ArrayList<>();
        mLoadReady += 1;

        if (Call == null)
            return;

        if (mContentIndex != mMaxIndex){
            Call.loadChapter(FLAG_NEXT,mContentIndex + 1);
        }

        if (mContentIndex != 0){
            Call.loadChapter(FLAG_PRE,mContentIndex - 1);
        }
        else{
            mPreViewLoadComplete = true;
            mViewPage    = 5;
            mPreViewPage = 0;
            mLoadReady  += 1;
        }

        myHandler.sendEmptyMessage(FLAG_START);
    }


    @Override
    public void setPreContent(Content content) {
        mPreContent        = content;
        mPreViewList       = new ArrayList<>();
        mPreTextOffsetList = new ArrayList<>();
        mPreTextOffsetList.addAll(calculationTextOffset(DEFAULT_TEXT_SIZE,mPreContent.content));
        mPreOffsetPosition = mPreTextOffsetList.size() - 1;
        if (isFirstLoad){
            mLoadReady += 1;
            myHandler.sendEmptyMessage(FLAG_START);
        }
    }


    @Override
    public void setNextContent(Content content) {
        mNextContent        = content;
        mNextViewList       = new ArrayList<>();
        mNextTextStartIndex = 0;
        mNextTextLen        = mNextContent.content.length();
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
    }


    @Override
    public void initCall() {
        if (Call == null)
            return;

        Call.loadFirstChapter();
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


    @Override
    public void setCall(BaseReadView.Call call) {
        Call = call;
    }


    private void changePreViewIndex(){
        // 纠正PreViewIndex，使得上一章View的Index位于尾端
        View child = getChildAt(0);
        if ((int) child.getTag(R.id.view_index) < mContentIndex){
            removeViewForParent(child);
            addView(child);

            child = getChildAt(0);
            if ((int) child.getTag(R.id.view_index) < mContentIndex){
                changePreViewIndex();
            }
        }

        View e1 = getChildAt(mChildCount - 1),
             e2 = getChildAt(mChildCount - 2);

        if (e1 == null || e2 == null)
            return;

        if ((int) e1.getTag(R.id.view_index) >= mContentIndex || (int) e2.getTag(R.id.view_index) >= mContentIndex || e1.getTag(R.id.view_index) != e2.getTag(R.id.view_index))
            return;

        if ((int) e1.getTag(R.id.view_position) > (int) e2.getTag(R.id.view_position)){
            removeViewForParent(e2);
            addView(e2);
        }

        e1.layout(-mWidth,0,0,mHeight);
        e2.layout(-mWidth,0,0,mHeight);

    }


    private View getLastPreView(){
        View child;
        for (int i = mChildCount - 1; i >= 0; i--){
            child = getChildAt(i);
            if (child == null)
                continue;
            if ((int) child.getTag(R.id.view_index) < mContentIndex){
                return child;
            }
        }
        return null;
    }


    private View getLastNextView(){
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


    private View getLastView(){
        View child;
        for (int i = mChildCount - 1; i >= 0; i--){
            child = getChildAt(i);
            if (child == null)
                continue;
            if ((int) child.getTag(R.id.view_index) == mContentIndex){
                return child;
            }
        }
        return null;
    }


    private View getFirstView(){
        View child;
        for (int i = 0; i < mChildCount - 1; i++){
            child = getChildAt(i);
            if (child == null)
                continue;
            if ((int) child.getTag(R.id.view_index) == mContentIndex){
                return child;
            }
        }
        return null;
    }


    private View getCacheViewByPosition(int position, List<ReadTextView> list){
        for (ReadTextView child : list){
            if ((int) child.getTag(R.id.view_position) == position){
                if (child.getParent() != null){
                    Log.e(TAG,"getCacheViewByPosition(), ViewHasParent and position = " + position);
                }
                return child;
            }
        }
        return null;
    }


    private View getLastCachePreView(){
        if (mPreViewList == null || mPreViewList.size() == 0)
            return null;

        View f1 = mPreViewList.get(0);

        View e1 = mPreViewList.get(mPreViewList.size()-1);

        View child;
        if ((int) f1.getTag(R.id.view_position) > (int) e1.getTag(R.id.view_position)){
            child = f1;
        }
        else{
            child = e1;
        }

        if (child.getParent() != null){
            Log.e(TAG,"getLastCachePreView()::CachePreView Has Parent!");
        }
        return child;
    }


    private int getFirstPreViewIndex(){
        View child;
        for (int i = 0; i < mChildCount; i++){
            child = getChildAt(i);
            if (child == null)
                continue;
            if ((int) child.getTag(R.id.view_index) < mContentIndex){
                return i;
            }
        }
        Log.e(TAG,"getFirstPreViewIndex()::None Index!");
        return -1;
    }
}
