package com.wong.novel.mvp.model;

import android.content.ContentValues;

import com.wong.novel.bean.Chapter;
import com.wong.novel.bean.Content;
import com.wong.novel.constant.App;
import com.wong.novel.db.NovelDB;
import com.wong.novel.db.TB_Cache;
import com.wong.novel.db.TB_Collect;
import com.wong.novel.http.RetrofitHelper;
import com.wong.novel.mvp.contract.ReadContract;

import java.util.List;

import io.reactivex.Observable;

public class ReadModel extends CommonModel implements ReadContract.Model {

    
    @Override
    public Observable<Content> getContent(String chapter_url) {
        return RetrofitHelper.service.getContent(chapter_url);
    }

    @Override
    public Observable<List<Content>> getContent(int book_collect_id, int chapter_index) {
        return TB_Cache.query(NovelDB.getInstance().getReadableDatabase(),TB_Cache.COLUMN_BOOK_COLLECT_ID + " =? and " + TB_Cache.COLUMN_CACHE_ID + " >=?",new String[]{ String.valueOf(book_collect_id),String.valueOf(chapter_index-1) },TB_Cache.COLUMN_CHAPTER_INDEX + " DESC","3");
    }

    @Override
    public void deleteAllContent(int book_collect_id) {
        App.mExecutors.execute(()->{
            TB_Cache.delete(NovelDB.getInstance().getWritableDatabase(),TB_Cache.COLUMN_BOOK_COLLECT_ID + " =? ",new String[]{ String.valueOf(book_collect_id) });
        });
    }

    @Override
    public void cacheContent(List<ContentValues> list) {
        if (list == null || list.size() == 0)
            return;
        App.mExecutors.execute(() -> {
            for (ContentValues v : list){
                TB_Cache.insert(NovelDB.getInstance().getWritableDatabase(),v);
            }
        });
    }

    @Override
    public Observable<List<Chapter>> getChapterList(String book_src) {
        return RetrofitHelper.service.getChapterList(book_src);
    }


    @Override
    public void updateBookInfo(int book_collect_id, ContentValues values) {
        App.mExecutors.execute(()->{
            TB_Collect.update(NovelDB.getInstance().getWritableDatabase(),book_collect_id,values);
        });
    }
}
