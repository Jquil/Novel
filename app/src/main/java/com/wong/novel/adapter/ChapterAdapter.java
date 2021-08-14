package com.wong.novel.adapter;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wong.novel.R;
import com.wong.novel.bean.Chapter;

import java.util.List;

public class ChapterAdapter extends BaseQuickAdapter<Chapter, BaseViewHolder> {

    private Context mContext;

    public ChapterAdapter(Context context,int layoutResId, @Nullable List<Chapter> data) {
        super(layoutResId, data);
        mContext = context;
    }


    @Override
    protected void convert(BaseViewHolder helper, Chapter item) {
        ((TextView) helper.getView(R.id.item_tv)).setTextColor(item.selected ? Color.parseColor("#D81B60") : mContext.getResources().getColor(R.color.textColor));
        helper.setText(R.id.item_tv,item.title);
        helper.setTag(R.id.item_tv,Integer.valueOf(item.index) - 1);
    }


}
