package com.wong.novel.base;

import android.view.View;

import com.wong.novel.R;

public abstract class BaseMVPActivity<V extends IView,P extends IPresenter<V>> extends BaseActivity implements IView{

    protected P mPresenter = null;

    protected boolean isFirstLoadComplete;

    protected abstract P onCreatePresenter();

    @Override
    protected void initView() {
        mPresenter = onCreatePresenter();
        if (mPresenter != null){
            mPresenter.attachView((V) this);
        }

        mMultipleStatusView = findViewById(R.id.view_multiple_status);
        if (mMultipleStatusView != null){
            mMultipleStatusView.setRetryListener((View v) -> {
                start();
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null){
            mPresenter.detachView();
            mPresenter = null;
        }
    }
}
