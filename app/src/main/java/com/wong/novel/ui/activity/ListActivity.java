package com.wong.novel.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wong.novel.R;
import com.wong.novel.adapter.BookAdapter;
import com.wong.novel.base.BaseMVPActivity;
import com.wong.novel.bean.Book;
import com.wong.novel.constant.Constant;
import com.wong.novel.mvp.contract.ListContract;
import com.wong.novel.mvp.presenter.ListPresenter;
import com.wong.novel.widget.MultipleStatusView;
import com.wong.novel.widget.RVItemDecoration;

import java.util.List;

public class ListActivity extends BaseMVPActivity<ListContract.View,ListContract.Presenter> implements ListContract.View{

    private static final String TAG = "ListActivity";

    private Toolbar mToolbar;

    private FloatingActionButton mBtnBackTop;

    private RecyclerView  mRVList;

    private String mTitle,mRequestURL,mKey;

    private BookAdapter   mBookAdapter;

    private Real mReal;

    private Boolean isLoadMore;

    private int mPage,mRequestDataSize;

    private Book mBook;

    private enum Real{
        Column,
        Type,
        Search
    }


    public static void go(Context context, String key, String title, String url){
        Intent intent = new Intent(context,ListActivity.class);
        intent.putExtra(Constant.key,key);
        intent.putExtra(Constant.key_title,title);
        intent.putExtra(Constant.key_url,url);
        context.startActivity(intent);
    }


    @Override
    protected ListContract.Presenter onCreatePresenter() {
        return new ListPresenter();
    }


    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_list;
    }


    @Override
    protected void initData() {

        // Intent getData
        Intent intent = getIntent();
        mKey = intent.getStringExtra(Constant.key);
        if (mKey != null){
            if (mKey.equals(Constant.key_column)){
                mReal = Real.Column;
                mRequestDataSize = 30;
            }
            else if (mKey.equals(Constant.key_type)){
                mReal = Real.Type;
                mRequestDataSize = 15;
            }
            else if (mKey.equals(Constant.key_search)){
                mReal = Real.Search;
                mRequestDataSize = 20;
            }
        }
        mTitle      = intent.getStringExtra(Constant.key_title);
        mRequestURL = intent.getStringExtra(Constant.key_url);


        // Adapter
        mBookAdapter = new BookAdapter(this,R.layout.item_book,null);

        mPage = 1;
        isLoadMore = false;
    }


    @Override
    protected void initView() {
        super.initView();
        mToolbar    = findViewById(R.id.toolbar);
        mRVList     = findViewById(R.id.rv_list);
        mBtnBackTop = findViewById(R.id.btn_top);

        mToolbar.setTitle(mTitle);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // RV
        mRVList.setLayoutManager(new LinearLayoutManager(this));
        mRVList.setAdapter(mBookAdapter);
        mRVList.addItemDecoration(new RVItemDecoration(this,LinearLayoutManager.VERTICAL));
        mBookAdapter.bindToRecyclerView(mRVList);
        mBookAdapter.setOnLoadMoreListener(moreListener,mRVList);
        mBookAdapter.setOnItemClickListener(clickListener);


        // BackToTop
        mBtnBackTop.setOnClickListener((View v) -> {
            if (mBookAdapter.getData().size() != 0)
                mRVList.scrollToPosition(0);
        });
    }


    @Override
    protected void start() {
        showLoading();

        // Request
        switch (mReal){
            case Type:
                mPresenter.getTypeBookList(mRequestURL,mPage);
                break;
            case Column:
                mPresenter.getColumnBookList(mRequestURL,mPage);
                break;
            case Search:
                mPresenter.getSearchData(mTitle,mPage);
                break;
        }
    }


    @Override
    public void setList(List<Book> data) {
        // ColumnSize = 30
        // TypeSize   = 15
        if (!isFirstLoadComplete){
            hideLoading();
            isFirstLoadComplete = true;
        }
        mBookAdapter.addData(data);
        if (isLoadMore){
            mBookAdapter.loadMoreComplete();
            isLoadMore = false;
        }

        if (data.size() < mRequestDataSize){
            mBookAdapter.loadMoreEnd();
        }
    }


    @Override
    public void showLoading() {
        mMultipleStatusView.showLoading();
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void hideLoading() {
        mMultipleStatusView.hideLoading(MultipleStatusView.STATUS_CONTENT);
        mBtnBackTop.setVisibility(View.VISIBLE);
    }


    @Override
    public void showEmpty() {
        mMultipleStatusView.showEmpty();
    }

    @Override
    public void hideEmpty() {

    }

    @Override
    public void showError() {
        mMultipleStatusView.showError();
    }

    @Override
    public void hideError() {

    }

    private BaseQuickAdapter.RequestLoadMoreListener moreListener = new BaseQuickAdapter.RequestLoadMoreListener() {
        @Override
        public void onLoadMoreRequested() {
            isLoadMore = true;
            mPage += 1;
            // Request
            switch (mReal){
                case Type:
                    mPresenter.getTypeBookList(mRequestURL,mPage);
                    break;
                case Column:
                    mPresenter.getColumnBookList(mRequestURL,mPage);
                    break;
                case Search:
                    mPresenter.getSearchData(mTitle,mPage);
                    break;
            }
        }
    };


    private BaseQuickAdapter.OnItemClickListener clickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            mBook = mBookAdapter.getItem(position);
            InfoActivity.go(ListActivity.this,mBook.src);
        }
    };
}
