package com.wong.novel.base;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.wong.novel.R;
import com.wong.novel.constant.Constant;
import com.wong.novel.receiver.NetworkReceiver;
import com.wong.novel.util.NetworkUtil;
import com.wong.novel.util.SP;
import com.wong.novel.widget.MultipleStatusView;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     *  Activity(View) 只与Presenter打交道
     * */
    private static final String TAG = "BaseActivity";

    protected abstract int attachLayoutRes();

    protected abstract void initData();

    protected abstract void initView();

    protected abstract void start();

    protected MultipleStatusView mMultipleStatusView;

    protected View mNetWorkTipView;

    protected WindowManager mWM;

    protected WindowManager.LayoutParams mWMLP;

    protected boolean isRegister;

    protected NetworkReceiver mNetworkReceiver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(attachLayoutRes());
        getDelegate().setLocalNightMode(SP.getInstance().getBoolean(Constant.key_night,false) ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        initData();
        initTipView();
        initView();
        start();
    }


    private void initTipView(){
        mNetWorkTipView = getLayoutInflater().inflate(R.layout.layout_network_tip,null);
        mWM   = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mWMLP = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                               ViewGroup.LayoutParams.WRAP_CONTENT,
                                               WindowManager.LayoutParams.TYPE_APPLICATION,
                                               WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                               PixelFormat.TRANSPARENT);
        mWMLP.gravity = Gravity.TOP;
        mWMLP.x = 0;
        mWMLP.y = 0;

    }


    public void checkNetWork(boolean isOk){
        if (isOk){
            if (mNetWorkTipView.getParent() != null){
                mWM.removeView(mNetWorkTipView);
            }
        }
        else{
            if (mNetWorkTipView.getParent() == null){
                mWM.addView(mNetWorkTipView,mWMLP);
            }

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // 注册网络变化广播：>> "android.net.conn.CONNECTIVITY_CHANGE"
        if (!isRegister) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            mNetworkReceiver = new NetworkReceiver();
            registerReceiver(mNetworkReceiver, filter);
            isRegister = true;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetworkReceiver != null){
            if (mNetWorkTipView != null && mNetWorkTipView.getParent() != null){
                mWM.removeView(mNetWorkTipView);
            }
            unregisterReceiver(mNetworkReceiver);
        }
    }


    @Override
    public void finish() {
        super.finish();
    }


    // 主要处理"返回"
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // 将Fragment逐个踢出栈
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
