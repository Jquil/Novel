package com.wong.novel.mvp.presenter;

import android.content.ContentValues;
import android.util.Log;

import com.wong.novel.bean.Chapter;
import com.wong.novel.bean.Content;
import com.wong.novel.db.TB_Cache;
import com.wong.novel.db.TB_Collect;
import com.wong.novel.mvp.contract.ReadContract;
import com.wong.novel.mvp.model.ReadModel;
import com.wong.novel.util.RxExt;

import java.util.ArrayList;
import java.util.List;

public class ReadPresenter extends CommonPresenter<ReadContract.View,ReadContract.Model> implements ReadContract.Presenter {

    private static final String TAG = "ReadPresenter";


    @Override
    protected ReadContract.Model onCreateModel() {
        return new ReadModel();
    }


    @Override
    public void getContent(int book_collect_id, int chapter_index) {
        RxExt.ss(mModel.getContent(book_collect_id, chapter_index),mView,mModel,(Object o) -> {
            mView.setContentList((List<Content>) o);
        });
    }


    @Override
    public void getContent(String chapter_url,int index) {
        RxExt.ss(mModel.getContent(chapter_url),mView, mModel,(Object o) -> {
            mView.setContent((Content) o,index);
        });
    }


    @Override
    public void getPreContent(String chapter_url,int index) {
        RxExt.ss(mModel.getContent(chapter_url),mView, mModel,(Object o) -> {
            mView.setPreContent((Content) o,index);
        });
    }

    @Override
    public void getNextContent(String chapter_url,int index) {
        RxExt.ss(mModel.getContent(chapter_url),mView, mModel,(Object o) -> {
            mView.setNextContent((Content) o,index);
        });
    }


    @Override
    public void cacheContent(int book_collect_id, List<Content> list) {
        if (list == null || list.size() == 0)
            return;
        ContentValues v;
        List<ContentValues> vList = new ArrayList<>();
        for (Content c : list){
            if (c == null)
                continue;
            v = new ContentValues();
            v.put(TB_Cache.COLUMN_BOOK_COLLECT_ID,book_collect_id);
            v.put(TB_Cache.COLUMN_CHAPTER_INDEX,c.index);
            v.put(TB_Cache.COLUMN_CHAPTER_TITLE,c.title);
            v.put(TB_Cache.COLUMN_CHAPTER_CONTENT,c.content);
            vList.add(v);
        }
        mModel.cacheContent(vList);
    }


    @Override
    public void deleteAllContent(int book_id) {
        mModel.deleteAllContent(book_id);
    }


    @Override
    public void getChapterList(String book_src) {
        RxExt.ss(mModel.getChapterList(book_src),mView,mModel,(Object o) ->{
            mView.setChapterList((List<Chapter>)o);
        });
    }


    @Override
    public void keepLastIndex(int book_collect_id, int lastIndex) {
        ContentValues values = new ContentValues();
        values.put(TB_Collect.COLUMN_READ_CHAPTER_INDEX,lastIndex);
        mModel.updateBookInfo(book_collect_id,values);
    }
}
