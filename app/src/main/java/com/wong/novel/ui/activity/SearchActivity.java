package com.wong.novel.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wong.novel.R;
import com.wong.novel.adapter.HistoryAdapter;
import com.wong.novel.base.BaseMVPActivity;
import com.wong.novel.bean.History;
import com.wong.novel.bean.Recommend;
import com.wong.novel.constant.Constant;
import com.wong.novel.mvp.contract.SearchContract;
import com.wong.novel.mvp.presenter.SearchPresenter;
import com.wong.novel.vm.HistoryVM;
import com.wong.novel.widget.FlowLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseMVPActivity<SearchContract.View,SearchContract.Presenter> implements SearchContract.View{

    private static final String TAG = "SearchActivity";
    private Toolbar           mToolbar;
    private FlowLayout        mFlowLayout;
    private RecyclerView      mRVHistory;
    private SearchView        mSearchView;
    private TextView          mTVTip,
                              mBtnClear;
    private ProgressBar       mPBLoading;
    private ArrayList<String> mTag;
    private List<Recommend>   mRecommends;


    HistoryAdapter    mHistoryAdapter;
    HistoryVM mHistoryVM;

    public static void go(Context context){
        Intent intent = new Intent(context,SearchActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected SearchContract.Presenter onCreatePresenter() {
        return new SearchPresenter();
    }


    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_search;
    }


    @Override
    protected void initView() {
        super.initView();
        mToolbar    = findViewById(R.id.toolbar);
        mFlowLayout = findViewById(R.id.view_flow);
        mRVHistory  = findViewById(R.id.rv_history);
        mTVTip      = findViewById(R.id.tv_tip);
        mBtnClear   = findViewById(R.id.btn_clear);
        mPBLoading  = findViewById(R.id.pb_loading);
    }


    @Override
    protected void initData() {
        mTag = new ArrayList<>();
        mHistoryVM = ViewModelProviders.of(this).get(HistoryVM.class);
    }


    @Override
    protected void start() {
        mToolbar.setTitle(R.string.key_search);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHistoryAdapter = new HistoryAdapter(R.layout.item_history,mHistoryVM.histories);
        mRVHistory.setLayoutManager(new LinearLayoutManager(this));
        mRVHistory.setAdapter(mHistoryAdapter);
        mHistoryAdapter.bindToRecyclerView(mRVHistory);

        mPresenter.getHistoryData();
        mPresenter.getRecommendBook();


        // 跳转到该热门书籍详情页
        mFlowLayout.setItemClickListener((View view,int position) -> {
            //Log.d(TAG,mRecommends.get(position).src);
            if (mRecommends.get(position) != null)
                InfoActivity.go(SearchActivity.this,mRecommends.get(position).src);
        });


        // 点击搜索关键字跳转
        mHistoryAdapter.setOnItemClickListener((BaseQuickAdapter adapter, View view, int position) -> {
            History history = mHistoryAdapter.getItem(position);
            if (history == null)
                return;
            ListActivity.go(this,Constant.key_search,history.key,null);
        });


        // 删除搜索关键字
        mHistoryAdapter.setCall((History item) -> {
            mPresenter.deleteHistory(item.id);
            mHistoryVM.histories.remove(item);
            mHistoryAdapter.notifyDataSetChanged();
            if (mHistoryAdapter.getData().size() == 0){
                mTVTip.setVisibility(View.VISIBLE);
            }
        });


        // 清除所有
        mBtnClear.setOnClickListener((View v) -> {
            if (mHistoryVM.histories.size() == 0)
                return;

            mPresenter.deleteAllHistory();
            mHistoryVM.histories.clear();
            mHistoryAdapter.notifyDataSetChanged();
            mTVTip.setVisibility(View.VISIBLE);
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setQueryHint("发现更多书籍");
        mSearchView.setOnQueryTextListener(onQueryTextListener);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void setRecommendBook(List<Recommend> data) {
        mRecommends = data;
        int size = data.size();
        for (int i = 0; i < size; i++){
            mTag.add(data.get(i).name);
        }
        mPBLoading.setVisibility(View.GONE);
        mFlowLayout.setItem(mTag);
    }


    @Override
    public void setHistoryData(List<History> data) {
        if (data.size() == 0){
            mTVTip.setVisibility(View.VISIBLE);
        }
        mHistoryVM.histories.addAll(data);
    }


    @Override
    public void addHistoryData(History history) {
        if (mTVTip.getVisibility() == View.VISIBLE){
            mTVTip.setVisibility(View.GONE);
        }
        mHistoryVM.histories.add(history);
        mHistoryAdapter.notifyDataSetChanged();
    }


    @Override
    public void showLoading() {

    }


    @Override
    public void hideLoading() {

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

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mPresenter.addHistoryData(query);
            ListActivity.go(SearchActivity.this, Constant.key_search,query,null);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

}
