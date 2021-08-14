package com.wong.novel.mvp.model;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.Chapter;
import com.wong.novel.constant.App;
import com.wong.novel.db.NovelDB;
import com.wong.novel.db.TB_Collect;
import com.wong.novel.http.RetrofitHelper;
import com.wong.novel.mvp.contract.InfoContract;

import java.util.List;

import io.reactivex.Observable;

public class InfoModel extends CommonModel implements InfoContract.Model {

    @Override
    public Observable<List<Chapter>> getChapterList(String book_src) {
        return RetrofitHelper.service.getChapterList(book_src);
    }


    @Override
    public Observable<Book> getBookInfo(String book_src) {
        return RetrofitHelper.service.getBookInfo(book_src);
    }


    @Override
    public void cacheAll(String book_src) {

    }

    @Override
    public Observable<Integer> collect(Book book) {
        return TB_Collect.insert(NovelDB.getInstance().getWritableDatabase(),book);
    }

    @Override
    public void remove(String book_id) {
        App.mExecutors.execute(() -> {
            TB_Collect.remove(NovelDB.getInstance().getWritableDatabase(),book_id);
        });
    }
}
