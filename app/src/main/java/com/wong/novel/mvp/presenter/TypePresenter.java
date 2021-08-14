package com.wong.novel.mvp.presenter;

import android.util.Log;

import com.wong.novel.bean.Type;
import com.wong.novel.constant.App;
import com.wong.novel.db.NovelDB;
import com.wong.novel.db.TB_Type;
import com.wong.novel.mvp.contract.TypeContract;
import com.wong.novel.mvp.model.TypeModel;
import com.wong.novel.util.RxExt;

import java.util.List;


public class TypePresenter extends CommonPresenter<TypeContract.View,TypeContract.Model> implements TypeContract.Presenter{

    private static final String TAG = "TypePresenter";

    @Override
    protected TypeContract.Model onCreateModel() {
        return new TypeModel();
    }

    @Override
    public void getType() {
        RxExt.ss(TB_Type.get(NovelDB.getInstance().getReadableDatabase()),mView,mModel,(Object o1) -> {
            List<Type> data1 = (List<Type>) o1;
            if (data1.size() != 0){
                mView.setType(data1);
            }
            else{
                RxExt.ss(mModel.getType(),mView,mModel,(Object o2) -> {
                    List<Type> data2 = (List<Type>) o2;
                    App.mExecutors.execute(() -> {
                        TB_Type.insert(NovelDB.getInstance().getWritableDatabase(),data2);
                    });
                    mView.setType(data2);
                });
            }
        });
    }
}
