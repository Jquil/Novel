package com.wong.novel.base;

import android.view.View;

public abstract class BaseMVPFragment<V extends IView,P extends IPresenter<V>> extends BaseFragment implements IView{

    protected P mPresenter;

    protected abstract P onCreatePresenter();


    @Override
    protected void initView(View view) {
        mPresenter = onCreatePresenter();
        if (mPresenter != null){
            mPresenter.attachView((V) this);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null){
            mPresenter.detachView();
            mPresenter = null;
        }
    }
}
