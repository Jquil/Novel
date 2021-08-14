package com.wong.novel.mvp.model;

import com.wong.novel.bean.History;
import com.wong.novel.bean.Recommend;
import com.wong.novel.constant.App;
import com.wong.novel.db.NovelDB;
import com.wong.novel.db.TB_History;
import com.wong.novel.http.RetrofitHelper;
import com.wong.novel.mvp.contract.SearchContract;

import java.util.List;

import io.reactivex.Observable;

public class SearchModel extends CommonModel implements SearchContract.Model {

    @Override
    public Observable<List<Recommend>> getRecommendBook() {
        return RetrofitHelper.service.getRecommendBook();
    }


    @Override
    public Observable<List<History>> getHistoryData() {
        return TB_History.get(NovelDB.getInstance().getReadableDatabase());
    }


    @Override
    public Observable<Integer> addHistoryData(String key) {
        return TB_History.insert(NovelDB.getInstance().getWritableDatabase(),key);
    }


    @Override
    public void deleteHistory(int id) {
        App.mExecutors.execute(() -> {
            TB_History.deleteById(NovelDB.getInstance().getWritableDatabase(),id);
        });
    }

    @Override
    public void deleteAllHistory() {
        App.mExecutors.execute(() -> {
            TB_History.deleteAll(NovelDB.getInstance().getWritableDatabase());
        });
    }
}
