package com.wong.novel.mvp.contract;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.Chapter;

import java.util.List;

import io.reactivex.Observable;

public interface InfoContract {

    interface View extends CommonContract.View{

        void setChapterList(List<Chapter> data);

        void setBookInfo(Book book);
    }


    interface Presenter extends CommonContract.Presenter<View>{

        void getChapterList(String book_src);

        void cacheAll(String book_src);

        void collect(Book book);

        void remove(String book_id);

        void getBookInfo(String book_src);
    }


    interface Model extends CommonContract.Model{

        Observable<List<Chapter>> getChapterList(String book_src);

        Observable<Book> getBookInfo(String book_src);

        Observable<Integer> collect(Book book);

        void cacheAll(String book_src);

        void remove(String book_id);
    }
}
