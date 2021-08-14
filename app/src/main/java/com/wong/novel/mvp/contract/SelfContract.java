package com.wong.novel.mvp.contract;

import android.content.ContentValues;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.UpdateChapter;

import java.util.List;

import io.reactivex.Observable;

public interface SelfContract {

    interface View extends CommonContract.View{

        void setCollectBooks(List<Book> data);

        void checkUpdateBook(Book book,boolean flag);

        void cacheBook();
    }


    interface Presenter extends CommonContract.Presenter<View>{

        void getCollectBooks();

        void checkBooksUpdate(List<Book> data);

        void removeBook(int id);

        void deleteCache(int collect_id);

        void cacheBook();

        void setOrCancelTop(boolean flag,int id);

        void setReading(boolean updateOrder,int collect_id);
    }


    interface Model extends CommonContract.Model{

        Observable<List<Book>> getCollectBooks();

        Observable<UpdateChapter> checkBookUpdate(Book book);

        void removeBook(int id);

        void cacheBook();

        void setOrCancelTop(int flag,int id);

        void updateBook(int id, ContentValues v);

        void deleteCache(int collect_id);
    }
}
