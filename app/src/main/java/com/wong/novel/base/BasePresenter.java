package com.wong.novel.base;

import androidx.lifecycle.LifecycleObserver;

public abstract class BasePresenter<V extends IView,M extends IModel> implements IPresenter<V>, LifecycleObserver {

    /**
     *  Presenter 连接 View(Activity) & Model
     * */


    protected V mView;
    protected M mModel;

    protected abstract M onCreateModel();


    @Override
    public void attachView(V view) {
        this.mView  = view;
        this.mModel = onCreateModel();
        if (mView instanceof LifecycleObserver){
            /* todo */
        }
    }

    @Override
    public void detachView() {
        mModel.onDetach();
        mModel = null;
        mView  = null;
    }
}