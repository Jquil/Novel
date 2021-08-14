package com.wong.novel.mvp.contract;

import com.wong.novel.bean.Type;
import com.wong.novel.mvp.contract.CommonContract;

import java.util.List;

import io.reactivex.Observable;

public interface TypeContract {

    interface View extends CommonContract.View{
        void setType(List<Type> data);
    }


    interface Presenter extends CommonContract.Presenter<View>{
        void getType();
    }


    interface Model extends CommonContract.Model{
        Observable<List<Type>> getType();
    }
}
