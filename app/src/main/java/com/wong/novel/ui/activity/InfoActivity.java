package com.wong.novel.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.wong.novel.R;
import com.wong.novel.adapter.ChapterAdapter;
import com.wong.novel.base.BaseMVPActivity;
import com.wong.novel.bean.Book;
import com.wong.novel.bean.Chapter;
import com.wong.novel.bean.MyList;
import com.wong.novel.constant.Constant;
import com.wong.novel.mvp.contract.InfoContract;
import com.wong.novel.mvp.presenter.InfoPresenter;
import com.wong.novel.ui.fragment.BookSelfFragment;
import com.wong.novel.widget.MultipleStatusView;
import com.wong.novel.widget.RVItemDecoration;

import java.util.List;

public class InfoActivity extends BaseMVPActivity<InfoContract.View,InfoContract.Presenter> implements InfoContract.View {

    private static final String TAG = "InfoActivity";


    private Toolbar mToolbar;

    private TextView mAuthor,
                     mUpdateChapter,
                     mUpdateTime,
                     mType,
                     mDesc;

    private Button   mBtnEdit,
                     mBtnRead,
                     mBtnChapter,
                     mBtnCache;

    private RecyclerView mRVChapter;

    private ImageView mImg;

    private DrawerLayout mDrawLayout;

    private CoordinatorLayout mCoorLayout;

    private ChapterAdapter mChapterAdapter;

    private Boolean isLoadChapterList;

    private boolean isCollect;

    private long mLastTime,mNowTime,mDistance = 3000;

    private int mShowTipNum,mEditDrawable;

    private String mCollectTip,mEditText;

    private Book mBook;

    private List<Chapter> mChapterList;


    public static void go(Context context,String book_src){
        Intent intent = new Intent(context,InfoActivity.class);
        intent.putExtra(Constant.key_src,book_src);
        context.startActivity(intent);
    }


    public static void go(Context context,Book book){
        Intent intent = new Intent(context,InfoActivity.class);
        intent.putExtra(Constant.key_book,book);
        context.startActivity(intent);
    }



    @Override
    protected InfoContract.Presenter onCreatePresenter() {
        return new InfoPresenter();
    }


    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_book;
    }


    @Override
    protected void initData() {
        isLoadChapterList = false;
        mChapterAdapter   = new ChapterAdapter(this,R.layout.item_tv,null);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void initView() {
        super.initView();
        mToolbar       = findViewById(R.id.toolbar);
        mAuthor        = findViewById(R.id.book_author);
        mUpdateChapter = findViewById(R.id.book_update_chapter);
        mUpdateTime    = findViewById(R.id.book_update_time);
        mType          = findViewById(R.id.book_type);
        mBtnEdit       = findViewById(R.id.book_edit);
        mBtnRead       = findViewById(R.id.book_read);
        mImg           = findViewById(R.id.book_img);
        mDesc          = findViewById(R.id.book_desc);
        mRVChapter     = findViewById(R.id.rv);
        mDrawLayout    = findViewById(R.id.layout_drawer);
        mBtnChapter    = findViewById(R.id.btn_chapter);
        mBtnCache      = findViewById(R.id.btn_cache);
        mCoorLayout    = findViewById(R.id.layout_coordinator);

        // Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // RV
        mRVChapter.setLayoutManager(new LinearLayoutManager(this));
        mRVChapter.addItemDecoration(new RVItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRVChapter.setAdapter(mChapterAdapter);
        mChapterAdapter.setOnItemClickListener(itemClickListener);
        mChapterAdapter.bindToRecyclerView(mRVChapter);

        // DrawerListener
        mDrawLayout.addDrawerListener(drawerListener);

        // (Cancel) Collect Book
        mBtnEdit.setOnClickListener((View v) -> {
            mNowTime = System.currentTimeMillis();
            if (mNowTime - mLastTime > mDistance){
                if (isCollect){
                    mPresenter.remove(mBook.id);
                    mCollectTip   = "取消收藏成功~";
                    mEditDrawable = R.drawable.ic_add_big;
                    mEditText     = getString(R.string.key_add);
                    isCollect     = false;
                }
                else{
                    mPresenter.collect(mBook);
                    mCollectTip   = "收藏成功";
                    mEditDrawable = R.drawable.ic_remove_big;
                    mEditText     = getString(R.string.key_remove);
                    isCollect     = true;
                }
                mShowTipNum = 0;
            }
            else{
                if (mShowTipNum > 0)
                    return;
                mCollectTip = "操作太频繁了哟~";
                mShowTipNum += 1;
            }

            mBtnEdit.setText(mEditText);

            mBtnEdit.setCompoundDrawablesWithIntrinsicBounds(null, getDrawable(mEditDrawable),null,null);
            Snackbar.make(mCoorLayout,mCollectTip,Snackbar.LENGTH_LONG).show();
            mLastTime = mNowTime;
        });

        // Read Book
        mBtnRead.setOnClickListener((View v) -> {
            if (mChapterList != null){
                MyList<Chapter> list = new MyList<>(mChapterList);
                ReadActivity.go(this,mBook,list);
            }
            else{
                ReadActivity.go(this,mBook);
            }
        });

        // Open DrawerLayout
        mBtnChapter.setOnClickListener((View v) -> {
            mDrawLayout.openDrawer(GravityCompat.END);
        });

        // Cache Book
        mBtnCache.setOnClickListener((View v) -> {
            Snackbar.make(mCoorLayout,getString(R.string.tip_no_open),Snackbar.LENGTH_LONG).show();
        });

        // Search For Author
        mAuthor.setOnClickListener((View v) -> {
            if (isFirstLoadComplete)
                ListActivity.go(this,Constant.key_search,mAuthor.getText().toString(),null);
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void start() {

        // Intent get Data
        Intent intent = getIntent();
        if (intent.getStringExtra(Constant.key_src) != null){
            showLoading();
            mPresenter.getBookInfo(intent.getStringExtra(Constant.key_src));
        }

        // 已摒弃该入口，更换为上面的入口
        else if (intent.getSerializableExtra(Constant.key_book) != null){
            mBook = (Book) intent.getSerializableExtra(Constant.key_book);
            setBookInfo(mBook);
        }

    }


    @Override
    public void setChapterList(List<Chapter> data) {
        mChapterList = data;
        mChapterAdapter.addData(data);
        isLoadChapterList = true;
        hideLoading();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setBookInfo(Book book) {
        mBook = book;
        //Log.d(TAG,book.name);
        if (!isFirstLoadComplete){
            getSupportActionBar().setTitle(book.name);
            mAuthor       .setText(book.author);
            mUpdateChapter.setText(book.update_chapter);
            mUpdateTime   .setText(book.update_time);
            mType         .setText(book.type);
            mDesc         .setText(book.desc);
            Glide.with(this)
                    .load(Constant.site + book.img)
                    .apply(new RequestOptions()
                            .placeholder(R.mipmap.none)
                            .error(R.mipmap.none))
                    .into(mImg);

            // 判断该书籍是否已被收藏
            if (BookSelfFragment.isExitBook(mBook.id)){
                mBtnEdit.setCompoundDrawablesWithIntrinsicBounds(null,getDrawable(R.drawable.ic_remove_big),null,null);
                mBtnEdit.setText(getString(R.string.key_remove));
                isCollect = true;
            }


            // 允许点击
            mBtnRead   .setEnabled(true);
            mBtnEdit   .setEnabled(true);
            mBtnCache  .setEnabled(true);
            mBtnChapter.setEnabled(true);

            // 设置TextView上下滑动
            mDesc.setMovementMethod(new ScrollingMovementMethod());

            isFirstLoadComplete = true;
        }
        else{
            //Log.d(TAG,mBook.collect_id + "~");
        }
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


    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            if (mBook != null && !isLoadChapterList){
                mPresenter.getChapterList(mBook.src);
            }
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };


    private ChapterAdapter.OnItemClickListener itemClickListener = (BaseQuickAdapter adapter, View view, int position) -> {
        MyList<Chapter> data = new MyList<>(mChapterList);
        ReadActivity.go(this,mBook,data,Integer.valueOf(mChapterAdapter.getItem(position).index)-1);
    };
}
