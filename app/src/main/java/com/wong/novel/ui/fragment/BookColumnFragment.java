package com.wong.novel.ui.fragment;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wong.novel.R;
import com.wong.novel.adapter.ColumnAdapter;
import com.wong.novel.base.BaseMVPFragment;
import com.wong.novel.bean.Column;
import com.wong.novel.constant.Constant;
import com.wong.novel.mvp.contract.ColumnContract;
import com.wong.novel.mvp.presenter.ColumnPresenter;
import com.wong.novel.ui.activity.ListActivity;
import com.wong.novel.widget.MultipleStatusView;
import com.wong.novel.widget.RVItemDecoration;

import java.util.List;

public class BookColumnFragment extends BaseMVPFragment<ColumnContract.View,ColumnContract.Presenter> implements ColumnContract.View{

    private static final String TAG = "BookColumnFragment";

    private RecyclerView mRV;

    private ColumnAdapter mColumnAdapter;

    private Column mColumn;

    @Override
    protected ColumnContract.Presenter onCreatePresenter() {
        return new ColumnPresenter();
    }


    @Override
    protected int attachLayoutRes() {
        return R.layout.layout_rv;
    }


    @Override
    protected void initView(View view) {
        super.initView(view);
        mRV = view.findViewById(R.id.rv);
        mRV.setLayoutManager(new LinearLayoutManager(getContext()));
        mColumnAdapter = new ColumnAdapter(R.layout.item_tv,null);
        mColumnAdapter.bindToRecyclerView(mRV);
        mColumnAdapter.setOnItemClickListener(onItemClickListener);
        mRV.setAdapter(mColumnAdapter);
        mRV.addItemDecoration(new RVItemDecoration(getContext(),LinearLayoutManager.VERTICAL));
    }

    @Override
    protected void lazyLoad() {
        showLoading();
        mPresenter.getColumns();
    }


    @Override
    public void setColumns(List<Column> data) {
        if (!isFirstLoadComplete){
            isFirstLoadComplete = true;
            hideLoading();
        }
        mColumnAdapter.addData(data);
    }


    @Override
    public void showLoading() {
        if (mMultipleStatusView != null){
            mMultipleStatusView.showLoading();
        }
    }


    @Override
    public void hideLoading() {
        if (mMultipleStatusView != null){
            mMultipleStatusView.hideLoading(MultipleStatusView.STATUS_CONTENT);
        }
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

    private ColumnAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            mColumn =  mColumnAdapter.getData().get(position);
            ListActivity.go(getContext(),Constant.key_column,mColumn.name,mColumn.src);
        }
    };
}
