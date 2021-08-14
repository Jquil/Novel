package com.wong.novel.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.wong.novel.constant.App;

public class NetworkUtil {

    public static boolean isOK(){
        ConnectivityManager cm = (ConnectivityManager) App.instance.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null){
            return info.isConnected();
        }
        return false;
    }
}
