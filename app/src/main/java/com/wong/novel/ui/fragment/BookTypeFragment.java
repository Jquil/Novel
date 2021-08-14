package com.wong.novel.ui.fragment;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wong.novel.R;
import com.wong.novel.adapter.TypeAdapter;
import com.wong.novel.base.BaseMVPFragment;
import com.wong.novel.bean.Type;
import com.wong.novel.constant.Constant;
import com.wong.novel.mvp.contract.TypeContract;
import com.wong.novel.mvp.presenter.TypePresenter;
import com.wong.novel.ui.activity.ListActivity;
import com.wong.novel.widget.MultipleStatusView;
import com.wong.novel.widget.RVItemDecoration;

import java.util.List;

public class BookTypeFragment extends BaseMVPFragment<TypeContract.View,TypeContract.Presenter> implements TypeContract.View {

    private static final String TAG = "BookTypeFragment";

    private RecyclerView mRV;

    private TypeAdapter  mTypeAdapter;

    private Type mType;

    @Override
    protected TypeContract.Presenter onCreatePresenter() {
        return new TypePresenter();
    }


    @Override
    protected int attachLayoutRes() {
        return R.layout.layout_rv;
    }


    @Override
    protected void initView(View view) {
        super.initView(view);
        mRV          = view.findViewById(R.id.rv);
        mTypeAdapter = new TypeAdapter(R.layout.item_tv,null);
        mRV.setLayoutManager(new LinearLayoutManager(getContext()));
        mRV.setAdapter(mTypeAdapter);
        mRV.addItemDecoration(new RVItemDecoration(getContext(),LinearLayoutManager.VERTICAL));
        mTypeAdapter.bindToRecyclerView(mRV);
        mTypeAdapter.setOnItemClickListener(clickListener);

    }

    @Override
    protected void lazyLoad() {
        showLoading();
        mPresenter.getType();
    }


    @Override
    public void setType(List<Type> data) {
        if (!isFirstLoadComplete){
            isFirstLoadComplete = true;
            hideLoading();
        }
        mTypeAdapter.addData(data);
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

    private BaseQuickAdapter.OnItemClickListener clickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            mType = mTypeAdapter.getItem(position);
            ListActivity.go(getContext(), Constant.key_type,mType.title,mType.url);
        }
    };
}
