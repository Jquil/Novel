package com.wong.novel.mvp.contract;

import com.wong.novel.bean.Book;

import java.util.List;

import io.reactivex.Observable;

public interface ListContract {


    interface View extends CommonContract.View{

        void setList(List<Book> data);
    }


    interface Presenter extends CommonContract.Presenter<View>{

        void getColumnBookList(String column_src,int page);

        void getTypeBookList(String type_url,int page);

        void getSearchData(String key,int page);
    }


    interface Model extends CommonContract.Model{

        Observable<List<Book>> getColumnBookList(String column_src,int page);

        Observable<List<Book>> getTypeBookList(String type_url,int page);

        Observable<List<Book>> getSearchData(String key,int page);
    }
}
