package com.wong.novel.base;

import android.util.Log;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseModel implements IModel{

    /**
     *  主要对Disposable作处理
     **/

    private static final String TAG = "BaseModel";

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
        //Log.d(TAG,mCompositeDisposable.size() + "<= size");
    }

    @Override
    public void onDetach() {
        mCompositeDisposable.clear();
        //Log.d(TAG,"Clear~");
        mCompositeDisposable = null;
    }
}
