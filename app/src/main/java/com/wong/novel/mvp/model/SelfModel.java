package com.wong.novel.mvp.model;

import android.content.ContentValues;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.UpdateChapter;
import com.wong.novel.constant.App;
import com.wong.novel.db.NovelDB;
import com.wong.novel.db.TB_Cache;
import com.wong.novel.db.TB_Collect;
import com.wong.novel.http.RetrofitHelper;
import com.wong.novel.mvp.contract.SelfContract;

import java.util.List;

import io.reactivex.Observable;

public class SelfModel extends CommonModel implements SelfContract.Model {

    @Override
    public Observable<List<Book>> getCollectBooks() {
        return TB_Collect.get(NovelDB.getInstance().getReadableDatabase());
    }


    @Override
    public Observable<UpdateChapter> checkBookUpdate(Book book) {
        return RetrofitHelper.service.getUpdateChapter(book.src);
    }


    @Override
    public void removeBook(int id) {
        App.mExecutors.execute(() -> {
            TB_Collect.remove(NovelDB.getInstance().getWritableDatabase(),id);
        });
    }


    @Override
    public void cacheBook() {

    }


    @Override
    public void setOrCancelTop(int flag, int id) {
        App.mExecutors.execute(() -> {
            TB_Collect.update(NovelDB.getInstance().getWritableDatabase(),id,flag);
        });
    }


    @Override
    public void updateBook(int id, ContentValues v) {
        App.mExecutors.execute(() -> {
            TB_Collect.update(NovelDB.getInstance().getWritableDatabase(),id,v);
        });
    }


    @Override
    public void deleteCache(int collect_id) {
        App.mExecutors.execute(()->{
            TB_Cache.delete(NovelDB.getInstance().getWritableDatabase(),TB_Cache.COLUMN_BOOK_COLLECT_ID + "=?",new String[]{ String.valueOf(collect_id) });
        });
    }
}
