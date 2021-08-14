package com.wong.novel.mvp.model;

import com.wong.novel.base.BaseModel;
import com.wong.novel.mvp.contract.CommonContract;

import io.reactivex.disposables.Disposable;


public abstract class CommonModel
                extends BaseModel
                implements CommonContract.Model {



    @Override
    public void addDisposable(Disposable disposable) {
        super.addDisposable(disposable);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
