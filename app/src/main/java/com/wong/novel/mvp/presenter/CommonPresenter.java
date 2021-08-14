package com.wong.novel.mvp.presenter;

import com.wong.novel.base.BasePresenter;
import com.wong.novel.mvp.contract.CommonContract;

public abstract class CommonPresenter<V extends CommonContract.View,M extends CommonContract.Model>
                extends BasePresenter<V,M>
                implements CommonContract.Presenter<V> {



}
