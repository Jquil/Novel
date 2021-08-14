package com.wong.novel.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.wong.novel.R;
import com.wong.novel.base.BaseMVPActivity;
import com.wong.novel.constant.Constant;
import com.wong.novel.db.NovelDB;
import com.wong.novel.mvp.contract.MainContract;
import com.wong.novel.mvp.presenter.MainPresenter;
import com.wong.novel.ui.fragment.BookColumnFragment;
import com.wong.novel.ui.fragment.BookSelfFragment;
import com.wong.novel.ui.fragment.BookTypeFragment;
import com.wong.novel.util.SP;
import com.wong.novel.widget.ViewPagerIndicator;

import java.util.ArrayList;

public class MainActivity extends BaseMVPActivity<MainContract.View,MainContract.Presenter> implements MainContract.View{

    private static final String TAG = "MainActivity";

    Toolbar             mToolbar;
    ViewPager           mVP;
    ViewPagerIndicator  mIndicator;

    ArrayList<String>   mTabs;
    ArrayList<Fragment> mFMList;

    BookSelfFragment    mBookSelfFragment;
    BookColumnFragment  mBookColumnFragment;
    BookTypeFragment    mBookTypeFragment;

    private long mNowTime,
                 mPreTime,
                 mDistance = 3000;

    private boolean mIsNight,
                    mSetMode;

    @Override
    protected MainContract.Presenter onCreatePresenter() {
        return new MainPresenter();
    }


    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_main;
    }


    @Override
    protected void initData() {
        mTabs   = new ArrayList<>();
        mFMList = new ArrayList<>();
        mTabs.add("书架");
        mTabs.add("栏目");
        mTabs.add("分类");

        mBookSelfFragment   = new BookSelfFragment();
        mBookColumnFragment = new BookColumnFragment();
        mBookTypeFragment   = new BookTypeFragment();
        mFMList.add(mBookSelfFragment);
        mFMList.add(mBookColumnFragment);
        mFMList.add(mBookTypeFragment);

        mIsNight = SP.getInstance().getBoolean(Constant.key_night,false);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void initView() {
        super.initView();
        mToolbar   = findViewById(R.id.toolbar);
        mIndicator = findViewById(R.id.view_indicator);
        mVP        = findViewById(R.id.vp);

        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);
        mVP.setOffscreenPageLimit(3);   // 设置了VP缓存界面数量
        mVP.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFMList.get(position);
            }

            @Override
            public int getCount() {
                return mFMList.size();
            }

        });


        mIndicator.setItem(mTabs);
        mIndicator.setupWidthVP(mVP);
    }


    @Override
    protected void start() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main,menu);
        menu.findItem(R.id.action_mode).setIcon(mIsNight ? R.drawable.ic_sun_24dp : R.drawable.ic_night_24dp);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_mode:
                mSetMode = mIsNight ? false : true;
                SP.getInstance().set(Constant.key_night,mSetMode);
                getDelegate().setLocalNightMode(mSetMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                //startActivity(new Intent(this,MainActivity.class));
                //overridePendingTransition(R.anim.alpha_start,R.anim.alpha_start);
                //finish();
                break;

            case R.id.action_search:
                SearchActivity.go(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        mNowTime = System.currentTimeMillis();
        if (mNowTime - mPreTime > mDistance){
            Toast.makeText(this,getString(R.string.tip_out),Toast.LENGTH_SHORT).show();
            mPreTime = mNowTime;
        }
        else{
            super.onBackPressed();
        }
    }


    @Override
    public void showLoading() {
        /* todo */
    }


    @Override
    public void hideLoading() {
        /* todo */
    }


    @Override
    public void showEmpty() {

    }


    @Override
    public void hideEmpty() {

    }


    @Override
    public void showError() {

    }


    @Override
    public void hideError() {

    }
}
