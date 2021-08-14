package com.wong.novel.mvp.presenter;

import android.util.Log;

import androidx.lifecycle.ViewModelProviders;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.Chapter;
import com.wong.novel.mvp.contract.InfoContract;
import com.wong.novel.mvp.model.InfoModel;
import com.wong.novel.ui.fragment.BookSelfFragment;
import com.wong.novel.util.RxExt;
import com.wong.novel.vm.CollectVM;

import java.util.List;

public class InfoPresenter extends CommonPresenter<InfoContract.View,InfoContract.Model> implements InfoContract.Presenter{

    private static final String TAG = "InfoPresenter";

    @Override
    protected InfoContract.Model onCreateModel() {
        return new InfoModel();
    }

    @Override
    public void getChapterList(String book_src) {
        RxExt.ss(mModel.getChapterList(book_src),mView, mModel,new RxExt.Ext() {
            @Override
            public void call(Object o) {
                mView.setChapterList((List<Chapter>) o);
            }
        });
    }


    @Override
    public void getBookInfo(String book_src) {
        RxExt.ss(mModel.getBookInfo(book_src),mView, mModel,new RxExt.Ext() {
            @Override
            public void call(Object o) {
                mView.setBookInfo((Book)o);
            }
        });
    }

    @Override
    public void cacheAll(String book_src) {

    }

    @Override
    public void collect(Book book) {
        RxExt.ss(mModel.collect(book),mView,mModel,(Object o) -> {
            book.collect_id = (int) o;
            BookSelfFragment.add(book);
            mView.setBookInfo(book);
        });
    }

    @Override
    public void remove(String book_id) {
        mModel.remove(book_id);
        BookSelfFragment.remove(book_id);
    }
}
