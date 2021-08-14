package com.wong.novel.constant;

import android.app.Application;
import android.util.Log;


import androidx.lifecycle.ViewModelProviders;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {

    public static App instance;

    public static final String TAG = "App";

    public static ExecutorService mExecutors;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mExecutors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}
