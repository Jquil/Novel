package com.wong.novel.base;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wong.novel.R;
import com.wong.novel.widget.MultipleStatusView;

public abstract class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment";

    private boolean isViewPrepare,  // 视图是否准备好
                    hasLoadData;    // 是否加载过数据


    protected abstract int attachLayoutRes();

    protected abstract void initView(View view);

    protected abstract void lazyLoad();

    protected MultipleStatusView mMultipleStatusView;

    protected boolean isFirstLoadComplete;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(attachLayoutRes(),null);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewPrepare = true;
        mMultipleStatusView = view.findViewById(R.id.view_multiple_status);
        if (mMultipleStatusView != null){
            mMultipleStatusView.setRetryListener((View v) -> {
                lazyLoad();
            });
        }

        initView(view);
        lazyLoadData();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            lazyLoadData();
        }
    }


    private void lazyLoadData(){
        if (getUserVisibleHint() && isViewPrepare && !hasLoadData){
            lazyLoad();
            hasLoadData = true;
        }
    }
}
