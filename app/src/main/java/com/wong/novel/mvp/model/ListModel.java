package com.wong.novel.mvp.model;

import com.wong.novel.bean.Book;
import com.wong.novel.http.RetrofitHelper;
import com.wong.novel.mvp.contract.ListContract;

import java.util.List;

import io.reactivex.Observable;

public class ListModel extends CommonModel implements ListContract.Model {

    @Override
    public Observable<List<Book>> getColumnBookList(String column_src, int page) {
        return RetrofitHelper.service.getColumnBookList(column_src, page);
    }

    @Override
    public Observable<List<Book>> getTypeBookList(String type_url, int page) {
        return RetrofitHelper.service.getTypeBookList(type_url, page);
    }

    @Override
    public Observable<List<Book>> getSearchData(String key, int page) {
        return RetrofitHelper.service.search(key, page);
    }
}
