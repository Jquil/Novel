package com.wong.novel.mvp.presenter;

import android.util.Log;

import com.wong.novel.bean.Column;
import com.wong.novel.constant.App;
import com.wong.novel.db.NovelDB;
import com.wong.novel.db.TB_Column;
import com.wong.novel.mvp.contract.ColumnContract;
import com.wong.novel.mvp.model.ColumnModel;
import com.wong.novel.util.RxExt;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ColumnPresenter extends CommonPresenter<ColumnContract.View,ColumnContract.Model> implements ColumnContract.Presenter{

    private static final String TAG = "ColumnPresenter";


    @Override
    protected ColumnContract.Model onCreateModel() {
        return new ColumnModel();
    }

    @Override
    public void getColumns() {

        // 先从数据库查
        RxExt.ss(TB_Column.get(NovelDB.getInstance().getReadableDatabase()),mView,mModel,(Object o1) -> {
            List<Column> data1 = (List<Column>) o1;
            if (data1.size() != 0){
                mView.setColumns(data1);
            }
            else{
                // 找不到，网络获取
                RxExt.ss(mModel.getColumns(),mView,mModel,(Object o2) -> {
                    List<Column> data2 = (List<Column>) o2;
                    App.mExecutors.execute(() -> {
                        TB_Column.insert(NovelDB.getInstance().getWritableDatabase(),data2);
                    });
                    mView.setColumns(data2);
                });
            }
        });

    }
}
