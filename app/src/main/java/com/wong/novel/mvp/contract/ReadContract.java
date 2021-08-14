package com.wong.novel.mvp.contract;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.wong.novel.bean.Chapter;
import com.wong.novel.bean.Content;
import com.wong.novel.widget.ReadView;

import java.util.List;

import io.reactivex.Observable;

public interface ReadContract {

    interface View extends CommonContract.View{

        void setPreContent(Content content,int index);

        void setContent(Content content,int index);

        void setNextContent(Content content,int index);

        void setChapterList(List<Chapter> data);

        void setContentList(List<Content> data);
    }


    interface Presenter extends CommonContract.Presenter<View>{

        void getPreContent(String chapter_url,int index);

        void getContent(int book_collect_id,int chapter_index);

        void getContent(String chapter_url,int index);

        void getNextContent(String chapter_url,int index);

        void getChapterList(String book_src);

        void cacheContent(int book_collect_id,List<Content> list);

        void deleteAllContent(int book_collect_id);

        void keepLastIndex(int book_collect_id,int lastIndex);
    }


    interface Model extends CommonContract.Model{

        Observable<Content> getContent(String chapter_url);

        Observable<List<Content>> getContent(int book_collect_id,int chapter_index);

        Observable<List<Chapter>> getChapterList(String book_src);

        void deleteAllContent(int book_collect_id);

        void cacheContent(List<ContentValues> list);

        void updateBookInfo(int book_collect_id,ContentValues values);
    }
}
