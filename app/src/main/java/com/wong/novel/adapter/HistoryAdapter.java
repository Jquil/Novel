package com.wong.novel.adapter;

import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wong.novel.R;
import com.wong.novel.bean.History;

import java.util.List;

public class HistoryAdapter extends BaseQuickAdapter<History, BaseViewHolder> {

    public HistoryAdapter(int layoutResId, @Nullable List<History> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, History item) {
        helper.setText(R.id.tv,item.key);
        helper.setTag(R.id.btn_clear,item.id);

        helper.getView(R.id.btn_clear).setOnClickListener((View v) -> {
            if (Call != null){
                Call.clear(item);
            }
        });
    }

    public Call Call;

    public void setCall(HistoryAdapter.Call call) {
        Call = call;
    }

    public interface Call{
        void clear(History item);
    }


}
