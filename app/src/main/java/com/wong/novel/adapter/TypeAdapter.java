package com.wong.novel.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wong.novel.R;
import com.wong.novel.bean.Type;

import java.util.List;

public class TypeAdapter extends BaseQuickAdapter<Type, BaseViewHolder> {

    public TypeAdapter(int layoutResId, @Nullable List<Type> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, Type item) {
        helper.setText(R.id.item_tv,item.title);
        helper.setTag(R.id.item_tv,item.url);
    }
}
