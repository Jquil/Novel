package com.wong.novel.mvp.contract;

import com.wong.novel.bean.Column;

import java.util.List;

import io.reactivex.Observable;

public interface ColumnContract {

    interface View extends CommonContract.View{
        void setColumns(List<Column> data);
    }


    interface Presenter extends CommonContract.Presenter<View>{
        void getColumns();
    }


    interface Model extends CommonContract.Model{
        Observable<List<Column>> getColumns();
    }
}
