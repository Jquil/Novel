package com.wong.novel.base;

public interface IPresenter<V extends IView> {

    void attachView(V view);

    void detachView();
}
