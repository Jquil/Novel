package com.wong.novel.adapter;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wong.novel.R;
import com.wong.novel.bean.Book;
import com.wong.novel.constant.Constant;
import com.wong.novel.db.TB_Collect;

import java.util.List;

public class CollectAdapter extends BaseQuickAdapter<Book, BaseViewHolder> {

    private static final String TAG = "CollectAdapter";

    public CollectAdapter(int layoutResId, @Nullable List<Book> data) {
        super(layoutResId, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, Book item) {

        //Log.d(TAG,item.name + " >> " + item.isUpdateChapter);

        helper.setText(R.id.book_name,item.name);
        helper.setText(R.id.book_update_chapter,item.update_chapter);

        helper.getView(R.id.iv_top).setVisibility(item.order == TB_Collect.ORDER ? View.VISIBLE : View.GONE);
        helper.getView(R.id.iv_chapter).setVisibility(item.isUpdateChapter ? View.VISIBLE : View.GONE);
        Glide.with(mContext)
             .load(Constant.site + item.img)
             .apply(new RequestOptions()
                            .error(R.mipmap.none)
                            .placeholder(R.mipmap.none))
             .into((ImageView) helper.getView(R.id.book_img));
    }


}
