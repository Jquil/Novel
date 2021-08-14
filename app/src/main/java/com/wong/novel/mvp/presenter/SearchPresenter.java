package com.wong.novel.mvp.presenter;

import android.util.Log;

import com.wong.novel.bean.History;
import com.wong.novel.bean.Recommend;
import com.wong.novel.constant.App;
import com.wong.novel.constant.Constant;
import com.wong.novel.db.NovelDB;
import com.wong.novel.db.TB_Recommend;
import com.wong.novel.mvp.contract.SearchContract;
import com.wong.novel.mvp.model.SearchModel;
import com.wong.novel.util.DateUtil;
import com.wong.novel.util.RxExt;
import com.wong.novel.util.SP;

import java.util.List;

public class SearchPresenter extends CommonPresenter<SearchContract.View,SearchContract.Model> implements SearchContract.Presenter{

    private static final String TAG = "SearchPresenter";

    private boolean isCommon;

    @Override
    protected SearchContract.Model onCreateModel() {
        return new SearchModel();
    }


    @Override
    public void getRecommendBook() {
        // 每周一 从新获取一次
        /* 判断是否为星期一，
             1. 是星期一，取出更新日期
                1.1 更新日期和今天相等就不用获取
                1.2 不相等，就删掉数据表记录，从新获取，放进数据表
             2. 不是星期一，就从数据库拿
                2.1 数据表有记录，返回
                2.2 没有记录，就网络获取，放进数据表
        *  */
        isCommon = false;
        if (DateUtil.getWhatDay(2)){
            String mDate = DateUtil.getDate();
            String mLastDate = SP.getInstance().getString(Constant.key_date,null);
            if (mLastDate == null || !mDate.equals(mLastDate)){
                TB_Recommend.delete(NovelDB.getInstance().getWritableDatabase());
                SP.getInstance().set(Constant.key_date,mDate);
                RxExt.ss(mModel.getRecommendBook(),mView,mModel,(Object o) -> {
                    List<Recommend> data = (List<Recommend>) o;
                    App.mExecutors.execute(() -> {
                        TB_Recommend.insert(NovelDB.getInstance().getWritableDatabase(),data);
                    });
                    mView.setRecommendBook(data);
                });
            }
            else{
                isCommon = true;
            }
        }
        else{
            isCommon = true;
        }


        // isCommon 表示共同的操作：从数据表拿数据，如果没有就网络获取
        if (isCommon){
            RxExt.ss(TB_Recommend.get(NovelDB.getInstance().getReadableDatabase()),mView,mModel,(Object o1) -> {
                List<Recommend> data1 = (List<Recommend>) o1;
                if (data1.size() != 0){
                    mView.setRecommendBook(data1);
                }
                else{
                    RxExt.ss(mModel.getRecommendBook(),mView,mModel,(Object o2) -> {
                        List<Recommend> data2 = (List<Recommend>) o2;
                        App.mExecutors.execute(() -> {
                            TB_Recommend.insert(NovelDB.getInstance().getWritableDatabase(),data2);
                        });
                        mView.setRecommendBook(data2);
                    });
                }
            });
        }
    }


    @Override
    public void getHistoryData() {
        RxExt.ss(mModel.getHistoryData(),mView,mModel,(Object o) -> {
            mView.setHistoryData((List<History>) o);
        });
    }


    @Override
    public void addHistoryData(String key) {
        RxExt.ss(mModel.addHistoryData(key),mView,mModel,(Object o) -> {
            History history = new History();
            history.id  = (int) o;
            history.key = key;
            mView.addHistoryData(history);
        });
    }


    @Override
    public void deleteHistory(int id) {
        mModel.deleteHistory(id);
    }


    @Override
    public void deleteAllHistory() {
        mModel.deleteAllHistory();
    }
}
