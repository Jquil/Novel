package com.wong.novel.mvp.contract;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.History;
import com.wong.novel.bean.Recommend;

import java.util.List;

import io.reactivex.Observable;

public interface SearchContract {

    interface View extends CommonContract.View{

        void setRecommendBook(List<Recommend> data);

        void setHistoryData(List<History> data);

        void addHistoryData(History history);
    }


    interface Presenter extends CommonContract.Presenter<View>{

        void getRecommendBook();

        void getHistoryData();

        void addHistoryData(String key);

        void deleteHistory(int id);

        void deleteAllHistory();
    }

    interface Model extends CommonContract.Model{

        Observable<List<Recommend>> getRecommendBook();

        Observable<List<History>>   getHistoryData();

        Observable<Integer> addHistoryData(String key);

        void deleteHistory(int id);

        void deleteAllHistory();

    }
}
