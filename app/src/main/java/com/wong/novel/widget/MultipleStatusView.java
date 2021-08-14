package com.wong.novel.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.wong.novel.R;

import java.util.ArrayList;
import java.util.List;

public class MultipleStatusView extends RelativeLayout {

    /** 多功能View
     *      --> Loading
     *      --> Content
     *      --> Error
     *      --> Empty
     * */

    private static final String TAG = "MultipleStatusView";

    private static final RelativeLayout.LayoutParams DEFAULT_LAYOUT_PARAMS = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

    public static final int  STATUS_CONTENT   = 0x01,
                             STATUS_LOADING   = 0x02,
                             STATUS_ERROR     = 0x03,
                             STATUS_EMPTY     = 0x04,
                             NULL_RESOURCE_ID = -1;

    private View mContentView,
                 mLoadingView,
                 mErrorView,
                 mEmptyView;

    private int mContentResId,
                mLoadingResId,
                mErrorResId,
                mEmptyResId,
                mViewStatus;

    private List<Integer> mIds;

    private final LayoutInflater inflater = LayoutInflater.from(getContext());


    public OnClickListener RetryListener;

    public MultipleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.MultipleStatusView);
        mContentResId = ta.getResourceId(R.styleable.MultipleStatusView_view_content,NULL_RESOURCE_ID);
        mLoadingResId = ta.getResourceId(R.styleable.MultipleStatusView_view_loading,R.layout.layout_loading);
        mEmptyResId   = ta.getResourceId(R.styleable.MultipleStatusView_view_empty,R.layout.layout_empty);
        mErrorResId   = ta.getResourceId(R.styleable.MultipleStatusView_view_error,R.layout.layout_error);
        ta.recycle();
        mIds = new ArrayList<>();
    }

    private View inflateView(int layoutId){
        return inflater.inflate(layoutId,null);
    }


    /** 显示内容视图
     * */
    public final void showContent(){
        if (mContentResId != NULL_RESOURCE_ID && mContentView == null){
            mContentView = inflater.inflate(mContentResId,null);
            addView(mContentView,0,DEFAULT_LAYOUT_PARAMS);
        }
        mViewStatus  = STATUS_CONTENT;
        showContentView();
    }

    public final void showContent(int layoutId, ViewGroup.LayoutParams lp){

    }

    public final void showContent(View view,ViewGroup.LayoutParams lp){

    }

    private void showContentView(){
        int childCount = getChildCount();
        View view;
        for (int i = 0; i < childCount; i++){
            view = getChildAt(i);
            if (view != null)
                view.setVisibility(mIds.contains(view.getId()) ? GONE : VISIBLE);
        }
    }


    /** 显示加载视图
     * */
    public final void showLoading(){
        showLoading(mLoadingResId,DEFAULT_LAYOUT_PARAMS);
    }

    public final void showLoading(int layoutId, ViewGroup.LayoutParams lp){
        showLoading(mLoadingView == null ? inflater.inflate(layoutId,null) : mLoadingView,lp);
    }

    public final void showLoading(View view, ViewGroup.LayoutParams lp){
        checkNull(view,"LoadingView is Null");
        checkNull(lp,"LoadingView LayoutParams is Null");

        mViewStatus = STATUS_LOADING;
        if (mLoadingView == null){
            mLoadingView = view;

            if (RetryListener != null){
                //
            }
            mIds.add(mLoadingView.getId());
            addView(mLoadingView,0,lp);
        }
        mLoadingView.animate().alpha(1).start();
        showViewById(mLoadingView.getId());
    }

    public final void hideLoading(int status_view_show){
        if (mLoadingView != null && mLoadingView.getVisibility() == VISIBLE){
            hide(mLoadingView,status_view_show);
        }
    }


    /** 显示错误视图
    * */
    public final void showError(){
        showError(mErrorResId,DEFAULT_LAYOUT_PARAMS);
    }

    public final void showError(int layoutId, ViewGroup.LayoutParams lp){
        showError(mErrorView == null ? inflater.inflate(layoutId,null) : mErrorView,lp);
    }

    public final void showError(View view, ViewGroup.LayoutParams lp){
        checkNull(view,"ErrorView is Null");
        checkNull(lp,"ErrorView LayoutParams is Null");

        mViewStatus = STATUS_ERROR;
        if (mErrorView == null){
            mErrorView = view;
            if (RetryListener != null){
                mErrorView.setOnClickListener(RetryListener);
            }
            Log.d(TAG,mErrorView.getId() + "<< ErrorID");
            mIds.add(mErrorView.getId());
            addView(mErrorView,0,lp);
        }

        mErrorView.animate().alpha(1).start();
        showViewById(mErrorView.getId());
    }

    public final void hideError(){

    }


    /** 显示空视图
     * */
    public final void showEmpty(){
        showEmpty(mEmptyResId,DEFAULT_LAYOUT_PARAMS);
    }

    public final void showEmpty(int layoutId, ViewGroup.LayoutParams lp){
        showEmpty(mEmptyView == null ? inflater.inflate(layoutId,null) : mEmptyView,lp);
    }

    public final void showEmpty(View view, ViewGroup.LayoutParams lp){
        checkNull(view,"EmptyView is Null");
        checkNull(lp,"EmptyView LayoutParams is Null");

        mViewStatus = STATUS_EMPTY;
        if (mEmptyView == null){
            mEmptyView = view;

            if (RetryListener != null){
                mEmptyView.setOnClickListener(RetryListener);
            }

            mIds.add(mEmptyView.getId());
            addView(mEmptyView,0,lp);
        }
        mEmptyView.animate().alpha(1).start();
        showViewById(mEmptyView.getId());
    }

    public final void hideEmpty(int status_view_show){
        if (mEmptyView != null && mEmptyView.getVisibility() == VISIBLE){
            hide(mEmptyView,status_view_show);
        }
    }


    /** 一些工作(显示，检查，清除)
     * */
    private void showViewById(int viewId){
        int childCount = getChildCount();
        View view;
        for (int i = 0; i < childCount; i++){
            view = getChildAt(i);
            view.setVisibility(viewId == view.getId() ? VISIBLE : GONE);
        }
    }

    private void showViewByStatus(int status){
        switch (status){
            case STATUS_CONTENT:
                showContent();
                break;

            case STATUS_EMPTY:
                showEmpty();
                break;

            case STATUS_ERROR:
                showError();
                break;
        }
    }

    private void checkNull(Object o,String hint){
        if (o == null){
            throw new NullPointerException(hint);
        }
    }

    private void clear(View...views){
        if (views == null)
            return;

        try {
            for (View view : views){
                if (view != null){
                    removeView(view);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void hide(View view,int status){
        if (view == null)
            return;

        ObjectAnimator animator = ObjectAnimator
                .ofFloat(view,"Alpha",0)
                .setDuration(1000);

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }


            @Override
            public void onAnimationEnd(Animator animation) {
                showViewByStatus(status);
            }


            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    public int getViewVisible(int status_view){
        switch (status_view){
            case STATUS_EMPTY:
                if (mEmptyView != null)
                    return mEmptyView.getVisibility();
                break;

            case STATUS_ERROR:
                if (mErrorView != null)
                    return mErrorView.getVisibility();
                 break;
        }
        return 0;
    }


    public void setRetryListener(OnClickListener retryListener) {
        RetryListener = retryListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        showContent();
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clear(mContentView,mEmptyView,mEmptyView,mLoadingView);
        if (mIds != null){
            mIds = null;
        }
    }
}
