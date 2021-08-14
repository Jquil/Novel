package com.wong.novel.mvp.model;

import com.wong.novel.bean.Type;
import com.wong.novel.http.RetrofitHelper;
import com.wong.novel.mvp.contract.TypeContract;

import java.util.List;

import io.reactivex.Observable;

public class TypeModel extends CommonModel implements TypeContract.Model{


    @Override
    public Observable<List<Type>> getType() {
        return RetrofitHelper.service.getType();
    }
}
