package com.wong.novel.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wong.novel.bean.Content;

import java.util.ArrayList;
import java.util.List;

public class ReadView extends ViewGroup {

    /** 需求
     *  1. 预加载三页，翻页就加载一页。(默认加载前章最后一页 以及 后章第一页)
     *  2. 以及预加载 上一章 和 下一章
     *  3. 回调方法
     *      OnClick,
     *      getPreContent,
     *      gerNextContent,
     *      getContent,
     *      isLoading,
     *      onLoadComplete,
     *
     ** 实现
     *  01. 首先来实现当前章的预加载
     *  02. Dialog Loading，不使用多状态View
     *  03. 第一页放在中间，下一页..放在右边
     *  04. 左滑：切换下一页，终点是：(0,0)
     *  05. 右滑：切换上一页：终点是：(-Width,0)
     *  06. 判断当前是否为第一章，第一页。
     *  07. 判断当前是为为最后一章最后一页。
     *  08. View数量维持在5，也就是前两页，后两页，加上当前页。（如何实现呢？）
     *  09. PreContentTVS 需要翻转
     *  10. setContentView 需要分情况
     * */

    private static final String TAG = "ReadView";

    private static final int  FLAG_NEW_VIEW          = 0x01,
                              FLAG_SLIDE_LEFT        = 0x02,
                              FLAG_SLIDE_RIGHT       = 0x03,
                              FLAG_SLIDE_BACK_RIGHT  = 0x04,
                              FLAG_SLIDE_BACK_LEFT   = 0x05,
                              FLAG_SLIDE_TO_WIDTH    = 0x06,
                              PRE_LOADING_PAGE       = 3,
                              DEFAULT_PADDING        = 45,
                              DEFAULT_TEXT_SIZE      = 18,
                              DIRECTION_LAYOUT_LEFT  = 1,
                              DIRECTION_LAYOUT_RIGHT = 2;

    public static final int FLAG_LOAD_PRE_CONTENT  = 0,
                            FLAG_LOAD_NEXT_CONTENT = 1;


    private Context mContext;


    private Content mContent,
                    mPreContent,
                    mNextContent;

    private View mLayoutView;

    private int mWidth,
                mHeight,
                mHalfWidth;

    private int mContentTextLength,
                mContentStartIndex,
                mNextContentTextLength,
                mNextContentStartIndex,
                mPreContentTextLength,
                mPreContentStartIndex,
                mPageIndex,
                mHashLoadPage,
                mChildCount,
                mDistance;

    private int
                mChapterIndex,
                mPreOffsetStartIndex,
                mPreOffsetEndIndex,
                mMaxIndex;

    private List<ReadTextView>  mContentTVs,
                                mPreContentTVs,
                                mNextContentTVs;

    private List<Integer>       mPreContentOffset,
                                mContentOffset;

    private float   mDownX,
                    mLastX,
                    mX,
                    mLeftLayoutX,
                    mRightLayoutX;

    private float mOffset,
                  mTouchSlop;

    private boolean isMeasure,
                    isContentFirstLoadFinish,
                    isPreContentFirstLoadFinish,
                    isContentLoadFinish,
                    isSlide,
                    isSlideOfPre,
                    isIntercept,
                    isSureDirection,
                    isChange;

    private ReadTextView mCurrentView,mReadTV;

    private ReadTextView.ReadTextMeasureView mPreMeasureTV;

    private Direction mDirection;

    private Change  mChange;


    private enum Direction{
        Left,
        Right
    }

    private enum Change{
        Pre,
        Common,
    }


    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){

                case FLAG_NEW_VIEW:
                    setContentView();
                    break;

                case FLAG_SLIDE_LEFT:
                    if (mCurrentView == null)
                        return;

                    //Log.d(TAG,"Slide Left...");

                    if (mOffset + mDistance < -mWidth){

                        if (Math.abs(mWidth + mOffset) < 10 ){
                            mDistance = -1;
                        }
                        else{
                            mDistance /= 10;
                        }
                    }

                    mOffset += mDistance;
                    mCurrentView.layout((int) (mWidth + mOffset),0,(int) (mWidth * 2 + mOffset),mHeight);
                    invalidate();
                    slideLeft();
                    break;

                case FLAG_SLIDE_RIGHT:
                    if (mCurrentView == null)
                        return;

                    if (mOffset + mDistance > mWidth){
                        mOffset += mDistance / 10;
                    }
                    else{
                        mOffset += mDistance;
                    }
                    mCurrentView.layout((int) (0 + mOffset),0,(int) (mWidth + mOffset),mHeight);
                    invalidate();
                    slideRight();
                    break;

                case FLAG_SLIDE_BACK_RIGHT:
                    //Log.d(TAG,mOffset + " -- " + mDistance);
                    if (mOffset + mDistance >= mWidth){

                        if (mWidth - mOffset < 10 ){
                            mDistance = 1;
                        }
                        else{
                            mDistance /= 10;
                        }

                    }
                    mOffset += mDistance;
                    mCurrentView.layout((int) (mWidth + mOffset),0,(int) ((mWidth * 2) + mOffset),mHeight);
                    invalidate();
                    slideBackOfRight();
                    break;

                case FLAG_SLIDE_BACK_LEFT:
                    if (mOffset - mDistance < 0){
                        mDistance /= 10;
                    }
                    mOffset -= mDistance;
                    mCurrentView.layout((int) (0 + mOffset),0,(int) (mWidth + mOffset),mHeight);
                    invalidate();
                    slideBackOfLeft();
                    break;

                case FLAG_SLIDE_TO_WIDTH:
                    if (mCurrentView == null)
                        return;

                    if (mOffset + mDistance > mWidth){
                        mOffset += mDistance / 10;
                    }
                    else{
                        mOffset += mDistance;
                    }
                    mCurrentView.layout((int) (-mWidth + mOffset),0,(int) (0 + mOffset),mHeight);

                    invalidate();
                    slideRightToWidth();
                    break;
            }
        }
    };


    public ReadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }


    private void init(){
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (isMeasure)
            return;

        mWidth     = MeasureSpec.getSize(widthMeasureSpec);
        mHeight    = MeasureSpec.getSize(heightMeasureSpec);
        mHalfWidth = mWidth / 2 / 2;
        isMeasure  = true;

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mChildCount = getChildCount();
        Log.d(TAG,"onLayout(): ChildCount >> " + mChildCount);
        if (mChildCount == 0)
            return;

        if (mChildCount - 1 == 0){
            getChildAt(mChildCount - 1).layout(l, t, r, b);
        }
        else{
            mLayoutView = getChildAt(mChildCount - 1);
            if (mLayoutView.getTag() != null){
                switch ((int) mLayoutView.getTag()){
                    case DIRECTION_LAYOUT_LEFT:
                        mLayoutView.layout(-mWidth,t,0,b);
                        break;

                    case DIRECTION_LAYOUT_RIGHT:
                        /*todo*/
                        break;
                }
            }
            else{
                // 这里是放正文下一页
                getChildAt(mChildCount - 1).layout(l + mWidth, t, r + mWidth, b);
            }

        }


        Log.d(TAG,isContentFirstLoadFinish + " >> " + isPreContentFirstLoadFinish);

        if (isContentFirstLoadFinish && isPreContentFirstLoadFinish){
            if (Call != null){
                Call.loadComplete();
            }
        }


        //Log.d(TAG,"ChildCount => " + mChildCount);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isSlide)
            return false;

        float x = event.getX();

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mLastX = x;
                isSureDirection = false;
                isIntercept     = false;
                mCurrentView    = null;
                mDirection      = null;
                return true;

            case MotionEvent.ACTION_MOVE:
                /*
                 * 1. 判断是否超过最小滑动距离
                 *  否：判断为点击：显示操作页
                 *  是：判断左滑 or 右滑
                 *      左滑：捕捉上一页，如果此时已是第一页，捕捉上一章最后一页
                 *      右滑：捕捉下一页，如果此时是最后一个，捕捉下一章的第一页
                 * */

                if (Math.abs(mLastX - mDownX) >= mTouchSlop){
                    mX = x - mLastX;

                    if (!isSureDirection){

                        // 右滑：切换上一页
                        if (mX > 0){
                            if (mPageIndex == 1){

                                if (mChapterIndex == 0){
                                    isIntercept = true;
                                    return false;
                                }
                                else{
                                    isSlideOfPre  = true;
                                    isChange      = false;
                                    mChange       = Change.Pre;
                                    mPageIndex    = mChildCount - 1;
                                    mCurrentView  = (ReadTextView) getChildAt(mPageIndex);
                                    mLeftLayoutX  = -mWidth;
                                    mRightLayoutX = 0;
                                }
                                //Log.d(TAG,"it is the first page..");
                            }

                            else if (isSlideOfPre){
                                mPageIndex    = mChildCount - 1;
                                mCurrentView  = (ReadTextView) getChildAt(mPageIndex);
                                mLeftLayoutX  = -mWidth;
                                mRightLayoutX = 0;
                            }
                            else{
                                mChange = Change.Common;
                                mCurrentView  = (ReadTextView) getChildAt(mPageIndex-1);
                                mLeftLayoutX  = 0;
                                mRightLayoutX = mWidth;
                            }
                            //Log.d(TAG,mPageIndex + " -- " + mChildCount);
                            mDirection    = Direction.Right;
                        }
                        // 左滑：切换下一页
                        else{
                            if (mPageIndex == mChildCount){
                                isIntercept = true;
                                //Log.d(TAG,"it is the last page..");
                                return false;
                            }
                            //Log.d(TAG,mPageIndex + " --- " + mChildCount);
                            mCurrentView  = (ReadTextView) getChildAt(mPageIndex);
                            mDirection    = Direction.Left;
                            mLeftLayoutX  = mWidth;
                            mRightLayoutX = mWidth * 2;
                        }

                        isSureDirection = true;
                    }

                    if (mCurrentView == null)
                        return false;

                    mOffset += mX;

                    // 左滑的限制
                    if (mDirection == Direction.Left){
                        if (mOffset > 0){
                            mOffset = 0;
                        }
                    }

                    mCurrentView.layout((int) (mLeftLayoutX + mOffset),0,(int) (mRightLayoutX + mOffset),mHeight);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (Math.abs(mLastX - mDownX) >= mTouchSlop){
                    up();
                }
                else{
                    if (Call != null){
                        Call.click();
                    }
                }
                break;
        }
        mLastX = x;
        return true;
    }


    // 松手具体操作
    private void up(){
        if (mCurrentView == null)
            return;

        if (isIntercept)
            return;

        isSlide = true;
        mOffset = (int) mOffset;
        mDistance = Math.abs((int) mOffset - mWidth);
        mDistance = mDistance / 15;

        if (Math.abs(mOffset) >= mHalfWidth ){
            if (mDirection == Direction.Left){
                // mOffset -> -Width
                mDistance = - (int) (mOffset + mWidth);
                mDistance /= 15;
                slideLeft();
            }

            else if (mDirection == Direction.Right){
                switch (mChange){
                    case Pre:
                        if (isSlideOfPre && !isChange){
                            Log.d(TAG,mPreContent + "--" + mContent + "--" + mNextContent);
                            mNextContent  = mContent;
                            mContent      = mPreContent;
                            mChapterIndex = mContent.index;
                            if (Call != null && mChapterIndex != 0){
                                Call.loadChapter(FLAG_LOAD_PRE_CONTENT,mChapterIndex - 1);
                            }
                            isChange = true;
                        }
                        slideRightToWidth();
                        break;
                    case Common:
                        slideRight();
                        break;
                }
            }
        }
        else{
            slideBack();
        }
    }


    // 向左自滑(下一页)
    private void slideLeft(){
        if (mOffset > -mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_LEFT,1);
        }
        else{
            isSlide = false;
            mOffset = 0;
            mPageIndex += 1;


            // 判断是否为下一章的第一页了：更换Content 和  startIndex 以及 获取下一章
            //Log.d(TAG,"NextSize => " + mNextContentTVs.size());
            if (mNextContentTVs != null && mNextContentTVs.size() == 2){
                isContentLoadFinish = false;
                mPreContent         = mContent;
                mContent            = mNextContent;
                mContentStartIndex  = mNextContentStartIndex;
                mChapterIndex       = mContent.index;
                mContentTextLength  = mContent.content.length();
                mPreContentTVs.clear();
                mPreContentTVs.addAll(mContentTVs);
                mContentTVs.clear();
                mContentTVs.addAll(mNextContentTVs);
                mNextContentTVs.clear();
                if (Call != null){
                    //Log.d(TAG,"NowChapterIndex ->" + mChapterIndex);
                    Call.loadChapter(FLAG_LOAD_NEXT_CONTENT,mChapterIndex + 1);
                }
            }


            // 维持5页：我是预加载了后两页，也就是只需要移除前面的
            if (mPageIndex >= 4){
                removeViewAt(0);
                mChildCount = getChildCount();
                mPageIndex  = mChildCount - 1;
            }


            if (mChildCount - mPageIndex == 1){
                if (!isContentLoadFinish){
                    setContentView();
                }
                else{
                    setNextContentView();
                }
            }
        }
    }


    // 向右自滑(上一页)
    private void slideRight(){
        if (mOffset < mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_RIGHT,1);
        }
        else{
            isSlide = false;
            mPageIndex -= 1;
            mOffset = 0;
        }
    }


    // 向右自滑(针对切换至上一章的)
    private void slideRightToWidth(){
        if (mOffset < mWidth){
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_TO_WIDTH,1);
        }
        else{
            mOffset = 0;
            isSlide = false;

            if (mPreOffsetEndIndex - 1 != 0){
                mPreOffsetEndIndex    -= 1;
                mPreOffsetStartIndex  -= 1;
                setPreContentView(mContent);
            }
            else{
                setPreContent(mPreContent);
            }

            if (mChildCount > 4){
                removeViewAt(0);
                Log.d(TAG,getChildCount() + "<<");
            }
        }
    }


    // 滑动返回
    private void slideBack(){
        mDistance /= 5;
        switch (mDirection){
            // 左滑切换下一页，归位时我们需要让View向右自滑
            case Left:
                slideBackOfRight();
                break;

            // 同理：我们需要让View向左自滑，这时View是隐藏在屏幕左侧的
            case Right:
                slideBackOfLeft();
                break;
        }
    }


    // 右滑的返回
    private void slideBackOfRight(){
        // 切换下一页时，距离不够，向右滑回: 终点：left(width)
        if (mOffset >= mWidth){
            mOffset = 0;
            isSlide = false;
        }
        else{
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_RIGHT,1);
        }
    }


    // 左滑的返回
    private void slideBackOfLeft(){
        // 切换上一页时，距离不够，向左返回：终点：left(0)
        if (mOffset <= 0){
            mOffset = 0;
            isSlide = false;
        }
        else{
            mHandler.sendEmptyMessageDelayed(FLAG_SLIDE_BACK_LEFT,1);
        }
    }


    public void setContent(Content content){
        mPageIndex                  = 1;
        mHashLoadPage               = 0;
        mContentStartIndex          = 0;
        isContentLoadFinish         = false;
        isContentFirstLoadFinish    = false;
        isPreContentFirstLoadFinish = false;
        mContent                    = content;
        mChapterIndex               = content.index;
        mContentTextLength          = mContent.content.length();
        mContentTVs                 = new ArrayList<>();
        if (Call != null){
            Call.loadChapter(FLAG_LOAD_NEXT_CONTENT,mChapterIndex + 1);
            if (mChapterIndex != 0){
                Call.loadChapter(FLAG_LOAD_PRE_CONTENT,mChapterIndex - 1);
            }
            else{
                isPreContentFirstLoadFinish = true;
            }
        }
        setContentView();
    }


    public void setPreContent(Content content){
        mPreContentTVs              = new ArrayList<>();
        mPreContentOffset           = new ArrayList<>();
        mPreContent                 = content;
        mPreContentTextLength       = content.content.length();
        mPreContentStartIndex       = 0;
        mPreContentOffset.add(0);
        Log.d(TAG,">>" + mPreContent);
        calculationPreContentOffset();
    }


    public void setNextContent(Content content){
        mNextContentTVs        = new ArrayList<>();
        mNextContent           = content;
        mNextContentTextLength = content.content.length();
        mNextContentStartIndex = 0;
    }


    private void setContentView(){
        String funName = "setContentView";
        Log.d(TAG,funName + " >> " + mContent + " -- " + isContentLoadFinish);
        if (mContent == null)
            return;

        if (isContentLoadFinish)
            return;

        isContentFirstLoadFinish = false;
        Log.d(TAG,"setContentView(): addView");
        mReadTV = new ReadTextView(mContext,mContent.title);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mReadTV.setLayoutParams(lp);
        mReadTV.setText(mContent.content.substring(mContentStartIndex));
        mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
        mContentTVs.add(mReadTV);
        addView(mReadTV);
        mReadTV.setCall((int len) -> {

            mContentStartIndex += len;

            if (mContentStartIndex == mContentTextLength){
                isContentLoadFinish = true;
            }
            else{

            }

            mHashLoadPage += 1;
            if (mHashLoadPage == PRE_LOADING_PAGE){
                isContentFirstLoadFinish = true;
                mHashLoadPage = PRE_LOADING_PAGE - 1;
            }
            else{
                mHandler.sendEmptyMessage(FLAG_NEW_VIEW);
            }
        });

    }


    private void setPreContentView(Content content){
        if (content == null)
            return;

        mReadTV = new ReadTextView(mContext,mPreContent.title);
        mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
        mReadTV.setText(content.content.substring(mPreContentOffset.get(mPreOffsetStartIndex),mPreContentOffset.get(mPreOffsetEndIndex)));

        mPreContentTVs.add(mReadTV);
        mReadTV.setTag(DIRECTION_LAYOUT_LEFT);
        addView(mReadTV);

        if (!isPreContentFirstLoadFinish)
            isPreContentFirstLoadFinish = true;
    }


    private void setNextContentView(){
        if(mNextContent == null)
            return;

        mReadTV = new ReadTextView(mContext,mNextContent.title);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mReadTV.setLayoutParams(lp);
        mReadTV.setText(mNextContent.content.substring(mNextContentStartIndex));
        mReadTV.setTextSize(DEFAULT_TEXT_SIZE);
        mNextContentTVs.add(mReadTV);
        addView(mReadTV);
        mReadTV.setCall((int len) -> {
            mNextContentStartIndex += len;
        });
    }


    private void calculationPreContentOffset(){
        if (mPreContent == null)
            return;

        mPreMeasureTV = new ReadTextView.ReadTextMeasureView(mContext,mPreContent.content.substring(mPreContentStartIndex),DEFAULT_TEXT_SIZE);
        mPreContentStartIndex += mPreMeasureTV.getLength();
        if (mPreContentStartIndex == mPreContentTextLength){
            mPreContentOffset.add(mPreContentTextLength);
            mPreOffsetEndIndex    = mPreContentOffset.size() - 1;
            mPreOffsetStartIndex  = mPreOffsetEndIndex - 1;

            if (isContentFirstLoadFinish && !isPreContentFirstLoadFinish){
                setPreContentView(mPreContent);
            }
        }
        else{
            mPreContentOffset.add(mPreContentStartIndex);
            calculationPreContentOffset();
        }
    }


    // 初始化回调
    public void initCall(){
        if (Call != null){
            Call.loadFirstChapter();
        }
    }


    // 获取当前Index
    public int getIndex(){
        return mChapterIndex;
    }


    // 重置
    public void reset(){
        if (Call != null){
            Call.isLoading();
            removeAllViews();
            Call.loadFirstChapter();
        }
    }


    // 改变模式
    public void changeMode(boolean isNight){
        mChildCount = getChildCount();
        ReadTextView mChildView;
        for (int i = 0; i < mChildCount; i++){
            mChildView = (ReadTextView) getChildAt(i);
            mChildView.change(isNight);
        }
    }


    //
    public void setMaxIndex(int index){
        mMaxIndex = index;
    }


    // 回调
    public interface Call{

        void click();

        void isLoading();

        void loadComplete();

        void loadFirstChapter();

        void loadChapter(int flag,int index);
    }


    private Call Call;


    public void setCall(ReadView.Call call) {
        Call = call;
    }
}
