package com.wong.novel.mvp.presenter;

import com.wong.novel.bean.Book;
import com.wong.novel.mvp.contract.ListContract;
import com.wong.novel.mvp.model.ListModel;
import com.wong.novel.util.RxExt;

import java.util.List;


public class ListPresenter extends CommonPresenter<ListContract.View,ListContract.Model> implements ListContract.Presenter {

    @Override
    protected ListContract.Model onCreateModel() {
        return new ListModel();
    }


    @Override
    public void getColumnBookList(String column_src, int page) {
        RxExt.ss(mModel.getColumnBookList(column_src, page),mView,mModel,(Object o) -> {
            mView.setList((List<Book>) o);
        });
    }


    @Override
    public void getTypeBookList(String type_url, int page) {
        RxExt.ss(mModel.getTypeBookList(type_url, page),mView,mModel,(Object o) -> {
            mView.setList((List<Book>) o);
        });
    }


    @Override
    public void getSearchData(String key, int page) {
        RxExt.ss(mModel.getSearchData(key, page),mView,mModel,(Object o) -> {
            mView.setList((List<Book>) o);
        });
    }
}
