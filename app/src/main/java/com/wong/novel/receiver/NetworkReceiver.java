package com.wong.novel.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wong.novel.base.BaseActivity;
import com.wong.novel.util.NetworkUtil;

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (context instanceof BaseActivity){
            ((BaseActivity) context).checkNetWork(NetworkUtil.isOK());
        }
    }
}
