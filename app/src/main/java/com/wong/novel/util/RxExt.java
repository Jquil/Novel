package com.wong.novel.util;

import android.util.Log;
import android.widget.Toast;

import com.wong.novel.base.IModel;
import com.wong.novel.base.IView;
import com.wong.novel.constant.App;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class RxExt {

    private static final String TAG = "RxExt";

    public static <T> void ss(Observable<T> observable, IView view, IModel model, final Ext ext){
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<T>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        model.addDisposable(d);
                        // 判断网络状态
                    }

                    @Override
                    public void onNext(T t) {
                        if (ext != null){
                            ext.call(t);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,e.toString());
                        Toast.makeText(App.instance.getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        view.showError();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public static <T> Observable<T> create(T data){
        Observable o = Observable.create((ObservableEmitter<T> emitter) -> {
            emitter.onNext(data);
            emitter.onComplete();
        });

        return o;
    }

    public interface Ext<T>{
        void call(T t);
    }

}
