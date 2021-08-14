package com.wong.novel.adapter;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wong.novel.R;
import com.wong.novel.bean.Book;
import com.wong.novel.constant.Constant;

import java.util.List;

public class BookAdapter extends BaseQuickAdapter<Book, BaseViewHolder> {

    private Context context;

    public BookAdapter(Context context, int layoutResId, @Nullable List<Book> data) {
        super(layoutResId, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, Book item) {

        Glide.with(context)
             .load(Constant.site + item.img)
             .apply(new RequestOptions()
                     .error(R.mipmap.none)
                     .placeholder(R.mipmap.none))
             .into((ImageView) helper.getView(R.id.book_img));


        helper.setText(R.id.book_name,item.name);
        helper.setText(R.id.book_update_chapter,item.update_chapter);
        helper.setText(R.id.book_update_time,item.update_time);
        helper.setText(R.id.book_author,item.author);
    }
}
