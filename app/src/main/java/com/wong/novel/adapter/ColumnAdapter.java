package com.wong.novel.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wong.novel.R;
import com.wong.novel.bean.Column;

import java.util.List;

public class ColumnAdapter extends BaseQuickAdapter<Column, BaseViewHolder> {

    public ColumnAdapter(int layoutResId, @Nullable List<Column> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, Column item) {
        helper.setText(R.id.item_tv,item.name);
        helper.setTag(R.id.item_tv,item.src);
    }
}
