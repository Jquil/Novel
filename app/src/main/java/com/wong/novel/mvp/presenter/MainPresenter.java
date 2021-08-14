package com.wong.novel.mvp.presenter;

import com.wong.novel.mvp.contract.MainContract;
import com.wong.novel.mvp.model.MainModel;

public class MainPresenter extends CommonPresenter<MainContract.View,MainContract.Model> implements MainContract.Presenter{

    @Override
    protected MainContract.Model onCreateModel() {
        return new MainModel();
    }


}
