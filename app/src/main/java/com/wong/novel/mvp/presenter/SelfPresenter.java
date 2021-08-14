package com.wong.novel.mvp.presenter;

import android.content.ContentValues;
import android.util.Log;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.UpdateChapter;
import com.wong.novel.constant.App;
import com.wong.novel.db.TB_Collect;
import com.wong.novel.mvp.contract.SelfContract;
import com.wong.novel.mvp.model.SelfModel;
import com.wong.novel.util.RxExt;

import java.util.List;

public class SelfPresenter extends CommonPresenter<SelfContract.View,SelfContract.Model> implements SelfContract.Presenter{

    private static final String TAG = "SelfPresenter";

    @Override
    protected SelfContract.Model onCreateModel() {
        return new SelfModel();
    }


    @Override
    public void getCollectBooks() {
        RxExt.ss(mModel.getCollectBooks(),mView,mModel,(Object o) -> {
            mView.setCollectBooks((List<Book>) o);
        });
    }


    @Override
    public void checkBooksUpdate(List<Book> data) {
        for (Book item : data){
            RxExt.ss(mModel.checkBookUpdate(item),mView,mModel,(Object o) -> {
                UpdateChapter uc = (UpdateChapter)o;
                ContentValues v;
                if (!uc.title.equals(item.update_chapter)){
                    // 数据表更新..
                    v = new ContentValues();
                    v.put(TB_Collect.COLUMN_BOOK_UPDATE_CHAPTER,uc.title);
                    v.put(TB_Collect.COLUMN_BOOK_IS_UPDATE,true);
                    item.update_chapter = uc.title;
                    item.isUpdateChapter = true;
                    if (item.order == TB_Collect.COMMON){
                        item.order = TB_Collect.UPDATE;
                        v.put(TB_Collect.COLUMN_BOOK_ORDER,TB_Collect.ORDER);
                    }
                    mModel.updateBook(item.collect_id,v);
                }
                mView.checkUpdateBook(item,item.isUpdateChapter ? true : false);
            });
        }
    }


    @Override
    public void removeBook(int id) {
        mModel.removeBook(id);
    }


    @Override
    public void cacheBook() {

    }


    @Override
    public void setOrCancelTop(boolean flag, int id) {
        if (flag){
            mModel.setOrCancelTop(TB_Collect.ORDER,id);
        }
        else{
            mModel.setOrCancelTop(TB_Collect.COMMON,id);
        }
    }


    @Override
    public void setReading(boolean updateOrder, int collect_id) {
        ContentValues v = new ContentValues();
        v.put(TB_Collect.COLUMN_BOOK_IS_UPDATE,false);
        if (updateOrder){
            v.put(TB_Collect.COLUMN_BOOK_ORDER,TB_Collect.COMMON);
        }
        mModel.updateBook(collect_id,v);
    }


    @Override
    public void deleteCache(int collect_id) {
        mModel.deleteCache(collect_id);
    }
}
