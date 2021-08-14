package com.wong.novel.mvp.model;

import com.wong.novel.bean.Column;
import com.wong.novel.http.RetrofitHelper;
import com.wong.novel.mvp.contract.ColumnContract;

import java.util.List;

import io.reactivex.Observable;


public class ColumnModel extends CommonModel implements ColumnContract.Model {

    @Override
    public Observable<List<Column>> getColumns() {
        return RetrofitHelper.service.getColumn();
    }
}
