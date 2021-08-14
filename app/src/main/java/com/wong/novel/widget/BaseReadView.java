package com.wong.novel.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.wong.novel.bean.Content;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseReadView extends ViewGroup {

    private static final String TAG = "BaseReadView";

    protected static final int  FLAG_START             = 0x01,
                                FLAG_NEW               = 0x02,
                                FLAG_SLIDE_LEFT_ZERO   = 0x03,
                                FLAG_SLIDE_LEFT_NEGATIVE_WIDTH = 0x04,
                                FLAG_SLIDE_RIGHT_ZERO  = 0x05,
                                FLAG_SLIDE_RIGHT_WIDTH = 0x06,
                                FLAG_SLIDE_BACK_WIDTH  = 0x07,
                                FLAG_SLIDE_BACK_ZERO   = 0x08,
                                FLAG_SLIDE_BACK_NEGATIVE_WIDTH = 0x09,
                                DEFAULT_TEXT_SIZE      = 18;

    public static final int FLAG_NEXT = 1,
                            FLAG_PRE  = 2;

    protected Context mContext;

    protected Content mContent,mPreContent,mNextContent;

    protected int mPageIndex,mMaxIndex,mContentIndex;

    protected int mWidth,mHeight,mLimitDistance,mLoadReady;

    protected boolean isMeasured,isFirstLoad,isStarted;

    protected View mCacheView,mCatchView,mLayoutView,mRemoveView,mLastView,mZeroView;

    protected List<ReadTextView> mViewList,mPreViewList,mNextViewList;

    protected List<Integer> mTextOffsetList,mPreTextOffsetList;

    protected enum LoadWay{
        Front,
        After
    }

    protected enum LoadType{
        Left,
        Right
    }

    protected enum SlideDirection{
        Left,
        Right
    }

    protected LoadWay  mLoadWay;

    protected LoadType mLoadType;

    protected SlideDirection mDirection;

    public Call Call;

    protected Handler myHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case FLAG_START:
                    if (mLoadReady == 3 && isFirstLoad && !isStarted){
                        firstLoadView();
                        isStarted = true;
                    }
                    break;

                case FLAG_NEW:
                    setContentView();
                    break;

                case FLAG_SLIDE_LEFT_ZERO:
                    handleSlideLeftToZero();
                    break;

                case FLAG_SLIDE_LEFT_NEGATIVE_WIDTH:
                    handleSlideLeftToNegativeWidth();
                    break;

                case FLAG_SLIDE_RIGHT_ZERO:
                    handleSlideRightToZero();
                    break;

                case FLAG_SLIDE_RIGHT_WIDTH:
                    handlerSlideRightToWidth();
                    break;

                case FLAG_SLIDE_BACK_ZERO:
                    handleSlideBackToZero();
                    break;

                case FLAG_SLIDE_BACK_WIDTH:
                    handleSlideBackToWidth();
                    break;

                case FLAG_SLIDE_BACK_NEGATIVE_WIDTH:
                    handleSlideBackToNegativeWidth();
                    break;
            }
        }
    };

    public BaseReadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (isMeasured)
            return;

        mWidth         = MeasureSpec.getSize(widthMeasureSpec);
        mHeight        = MeasureSpec.getSize(heightMeasureSpec);
        mLimitDistance = mWidth / 2 / 2;
        isMeasured     = true;
    }

    abstract void init();

    abstract void firstLoadView();

    abstract void setContentView();

    abstract void setPreContentView();

    abstract void setNextContentView();

    abstract void handleSlideLeftToZero();

    abstract void slideLeftToZero();

    abstract void handleSlideLeftToNegativeWidth();

    abstract void slideLeftToNegativeWidth();

    abstract void handleSlideRightToZero();

    abstract void slideRightToZero();

    abstract void handlerSlideRightToWidth();

    abstract void slideRightToWidth();

    abstract void handleSlideBackToZero();

    abstract void slideBackToZero();

    abstract void handleSlideBackToWidth();

    abstract void slideBackToWidth();

    abstract void handleSlideBackToNegativeWidth();

    abstract void slideBackToNegativeWidth();

    public abstract void setContent(Content content);

    public abstract void setPreContent(Content content);

    public abstract void setNextContent(Content content);

    public abstract void setMaxIndex(int index);

    public abstract void reset();

    public abstract void initCall();

    public abstract Content getContent();

    public abstract Content getPreContent();

    public abstract Content getNextContent();

    public abstract int getIndex();

    protected void removeViewForParent(View view){

        if (view == null || view.getParent() == null)
            return;

        endViewTransition(view);
        view.clearAnimation();
        removeView(view);
    }

    protected List<Integer> calculationTextOffset(int textSize,String content){

        if (content == null || content.length() == 0)
            return null;

        List<Integer> mTextOffsetList = new ArrayList<>();
        mTextOffsetList.add(0);

        int textStartIndex = 0,
            textLength     = content.length();

        ReadTextView.ReadTextMeasureView measureView;

        while (textStartIndex != textLength){
            measureView = new ReadTextView.ReadTextMeasureView(mContext,content.substring(textStartIndex),textSize);
            textStartIndex += measureView.getLength();
            mTextOffsetList.add(textStartIndex);
        }

        //Log.e(TAG,"PreContent TextOffset Size = " + mTextOffsetList.size());

        return mTextOffsetList;
    }

    public interface Call{

        void loading();

        void complete();

        void click();

        void loadFirstChapter();

        void loadChapter(int flag,int index);
    }

    public void setCall(BaseReadView.Call call) {
        Call = call;
    }

}
