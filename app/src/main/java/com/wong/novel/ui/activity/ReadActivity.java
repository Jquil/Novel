package com.wong.novel.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wong.novel.R;
import com.wong.novel.adapter.ChapterAdapter;
import com.wong.novel.base.BaseMVPActivity;
import com.wong.novel.bean.Book;
import com.wong.novel.bean.Chapter;
import com.wong.novel.bean.Content;
import com.wong.novel.bean.MyList;
import com.wong.novel.constant.Constant;
import com.wong.novel.mvp.contract.ReadContract;
import com.wong.novel.mvp.presenter.ReadPresenter;
import com.wong.novel.ui.dialog.CommonDialog;
import com.wong.novel.ui.fragment.BookSelfFragment;
import com.wong.novel.util.SP;
import com.wong.novel.vm.ReadVM;
import com.wong.novel.widget.BaseReadView;
import com.wong.novel.widget.FinalReadView;
import com.wong.novel.widget.MultipleStatusView;
import com.wong.novel.widget.MyFinalReadView;
import com.wong.novel.widget.MyReadView;
import com.wong.novel.widget.RVItemDecoration;
import com.wong.novel.widget.ReadView;

import java.util.ArrayList;
import java.util.List;

public class ReadActivity extends BaseMVPActivity<ReadContract.View,ReadContract.Presenter> implements ReadContract.View, View.OnClickListener {

    private static final String TAG = "ReadActivity";

    private View mViewOutside;

    private MyFinalReadView mReadView;

    private Book mBook;

    private TextView    mTV_BookName,
                        mBtn_List,
                        mBtn_Text,
                        mBtn_Cache,
                        mBtn_Mode;

    private Dialog mLoadingDialog,
                   mReadInfoDialog;

    private RecyclerView   mRV_Chapter;

    private ChapterAdapter mChapterAdapter;

    private DrawerLayout mDrwaer;

    private Chapter mChapter;

    private List<Chapter> mChapterList;

    private Content mContent;

    private int mIndex,mLastIndex,mFirstIndex;

    private boolean isNight;

    private ReadVM mReadVM;

    public static void go(Context context, Book book){
        Intent intent = new Intent(context,ReadActivity.class);
        intent.putExtra(Constant.key_book,book);
        context.startActivity(intent);
    }


    public static void go(Context context, Book book, MyList data){
        Intent intent = new Intent(context,ReadActivity.class);
        intent.putExtra(Constant.key_book,book);
        intent.putExtra(Constant.key_list,data);
        context.startActivity(intent);
    }


    public static void go(Context context,Book book,MyList data,int index){
        Intent intent = new Intent(context,ReadActivity.class);
        intent.putExtra(Constant.key_book,book);
        intent.putExtra(Constant.key_list,data);
        intent.putExtra(Constant.key_index,index);
        context.startActivity(intent);
    }


    @Override
    protected ReadContract.Presenter onCreatePresenter() {
        return new ReadPresenter();
    }


    @Override
    protected int attachLayoutRes() {

        /* 全屏，隐藏状态栏 */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        return R.layout.activity_read;
    }


    @Override
    protected void initData() {
        isNight         = SP.getInstance().getBoolean(Constant.key_night,false);
        mLastIndex      = -1;
        mChapterAdapter = new ChapterAdapter(this,R.layout.item_tv,null);
        mLoadingDialog  = CommonDialog.max_dialog(this,R.layout.dialog_loading);
        mReadInfoDialog = CommonDialog.max_dialog(this,R.layout.dialog_read);
        mReadVM         = ViewModelProviders.of(this).get(ReadVM.class);
    }


    @Override
    protected void initView() {
        super.initView();

        mDrwaer      = findViewById(R.id.layout_drawer);
        mReadView    = findViewById(R.id.view_read);
        mRV_Chapter  = findViewById(R.id.rv_chapter);
        mViewOutside = mReadInfoDialog.findViewById(R.id.view_outside);
        mTV_BookName = mReadInfoDialog.findViewById(R.id.book_name);
        mBtn_List    = mReadInfoDialog.findViewById(R.id.btn_list);
        mBtn_Text    = mReadInfoDialog.findViewById(R.id.btn_text);
        mBtn_Cache   = mReadInfoDialog.findViewById(R.id.btn_cache);
        mBtn_Mode    = mReadInfoDialog.findViewById(R.id.btn_mode);

        if (isNight){
            mBtn_Mode.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.ic_sun_24dp),null,null);
            mBtn_Mode.setText(R.string.key_sun);
        }

        // RV
        mRV_Chapter.setAdapter(mChapterAdapter);
        mRV_Chapter.setLayoutManager(new LinearLayoutManager(this));
        mRV_Chapter.addItemDecoration(new RVItemDecoration(this,LinearLayoutManager.VERTICAL));
        mChapterAdapter.bindToRecyclerView(mRV_Chapter);
        mChapterAdapter.setOnItemClickListener(itemClickListener);

        // Drawer
        mDrwaer.addDrawerListener(drawerListener);

        // Other Listener
        mTV_BookName.setOnClickListener(this);
        mBtn_List   .setOnClickListener(this);
        mBtn_Mode   .setOnClickListener(this);
        mViewOutside.setOnClickListener(this);
        mBtn_Text   .setOnClickListener(this);
        mBtn_Cache  .setOnClickListener(this);

        // BackPressed Finish
        mLoadingDialog.setCancelable(false);
        mLoadingDialog.setOnKeyListener((DialogInterface dialog, int keyCode, KeyEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
                finish();
            }
            return false;
        });
    }


    @Override
    protected void start() {
        Intent intent = getIntent();

        // Book
        if (intent.getSerializableExtra(Constant.key_book) != null){
            mBook            = (Book) intent.getSerializableExtra(Constant.key_book);
            mTV_BookName.setText(mBook.name);
        }

        // ChapterList
        if (intent.getSerializableExtra(Constant.key_list) != null){
            try {
                MyList<Chapter> myList = (MyList<Chapter>) intent.getSerializableExtra(Constant.key_list);
                if (myList != null){
                    mChapterList = myList.getData();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        // ChapterIndex
        mFirstIndex = intent.getIntExtra(Constant.key_index,-1);
        if (mFirstIndex == -1){
            mFirstIndex = mBook.lastIndex;
        }

        showLoading();
        mPresenter.getContent(mBook.collect_id,mBook.lastIndex);
        if (mChapterList != null){
            setChapterList(mChapterList);
        }
        else{
            mPresenter.getChapterList(mBook.src);
        }
    }


    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.book_name:
                finish();
                break;

            case R.id.btn_list:
                mReadInfoDialog.dismiss();
                mDrwaer.openDrawer(Gravity.START);
                break;

            case R.id.view_outside:
                if (mReadInfoDialog != null)
                    mReadInfoDialog.dismiss();
                break;

            case R.id.btn_cache:

            case R.id.btn_text:
                Toast.makeText(this,"暂不支持该功能，抱歉~",Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_mode:
                mReadInfoDialog.dismiss();
                isNight = isNight ? false : true;
                getDelegate().setLocalNightMode(isNight ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                SP.getInstance().set(Constant.key_night,isNight);
                mBtn_Mode.setText(isNight ? R.string.key_sun : R.string.key_night);
                mBtn_Mode.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(isNight ? R.drawable.ic_sun_24dp : R.drawable.ic_night_24dp),null,null);
                break;
        }
    }


    @Override
    public void setContent(Content content,int index) {
        content.index = index;
        if (getContentForIndex(index) == null){
            addContent(content);
        }
        mReadView.setContent(content);
    }


    @Override
    public void setPreContent(Content content,int index) {
        content.index = index;
        if (getContentForIndex(index) == null){
            addContent(content);
        }
        mReadView.setPreContent(content);
    }


    @Override
    public void setNextContent(Content content,int index) {
        content.index = index;
        if (getContentForIndex(index) == null){
            addContent(content);
        }
        mReadView.setNextContent(content);
    }


    @Override
    public void setContentList(List<Content> data) {
        mReadVM.mContentList.addAll(data);
    }


    @Override
    public void setChapterList(List<Chapter> data) {
        mChapterAdapter.addData(data);
        mReadView.setMaxIndex(data.size());

        // 这时候再初始化ReadView回调
        mReadView.setCall(new MyFinalReadView.Call() {
            @Override
            public void click() {
                if (mReadInfoDialog != null)
                    mReadInfoDialog.show();
            }

            @Override
            public void loading() {
                showLoading();
            }


            @Override
            public void complete() {
                hideLoading();
            }


            @Override
            public void loadFirstChapter() {
                mChapter = mChapterAdapter.getItem(mFirstIndex);
                if (mChapter == null)
                    return;
                mContent = getContentForIndex(mFirstIndex);
                if (mContent == null){
                    mPresenter.getContent(mChapter.url,mFirstIndex);
                }
                else{
                    setContent(mContent,mFirstIndex);
                }
            }

            @Override
            public void loadChapter(int flag,int index) {
                // 退出，保存当前阅读的章节序列，页码
                mChapter = mChapterAdapter.getItem(index);
                mContent = getContentForIndex(index);
                if (mChapter == null)
                    return;

                switch (flag){

                    case BaseReadView.FLAG_PRE:
                        mIndex = mReadView.getIndex();
                        scroll();
                        if (mContent != null){
                            setPreContent(mContent,index);
                        }
                        else{
                            mPresenter.getPreContent(mChapter.url,index);
                        }
                        break;

                    case BaseReadView.FLAG_NEXT:
                        if (mContent != null){
                            setNextContent(mContent,index);
                        }
                        else{
                            mPresenter.getNextContent(mChapter.url, index);
                        }
                        break;
                }
            }

        });
        mReadView.initCall();
    }


    @Override
    public void showLoading() {
        if (mLoadingDialog != null){
            if (mLoadingDialog.isShowing())
                return;
            mLoadingDialog.show();
        }
    }


    @Override
    public void hideLoading() {
        if (mLoadingDialog != null){
            mLoadingDialog.dismiss();
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


    @Override
    protected void onDestroy() {
        mLoadingDialog.dismiss();
        if (mBook.collect_id != 0){
            mPresenter.deleteAllContent(mBook.collect_id);
            List<Content> list = new ArrayList<>();
            list.add(mReadView.getContent());
            list.add(mReadView.getPreContent());
            list.add(mReadView.getNextContent());
            mPresenter.cacheContent(mBook.collect_id,list);
            mPresenter.keepLastIndex(mBook.collect_id,mReadView.getIndex());
            BookSelfFragment.notifyBookLastReadIndex(mBook.collect_id,mReadView.getIndex());
        }
        super.onDestroy();
    }


    private Content getContentForIndex(int index){
        for (Content c : mReadVM.mContentList){
            if (c.index == index){
                return c;
            }
        }

        return null;
    }


    private void addContent(Content content){
        mReadVM.mContentList.add(content);
    }


    private BaseQuickAdapter.OnItemClickListener itemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @SuppressLint("WrongConstant")
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            mChapter = mChapterAdapter.getItem(position);
            if (mChapter == null)
                return;

            // ... 清空，设置正文和下一文 以及 Item 样式
            //Log.d(TAG,"Position >> " + position);
            mIndex = position;
            changeItemStyle();
            mFirstIndex = mIndex;
            mReadView.reset();
            scroll();
        }
    };


    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            mIndex = mReadView.getIndex();
            changeItemStyle();
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };


    private void changeItemStyle(){

        if (mChapterAdapter == null)
            return;

        if (mIndex != mLastIndex){
            mChapterAdapter.getItem(mIndex).selected = true;
            mChapterAdapter.notifyItemChanged(mIndex);
            if (mLastIndex != -1){
                mChapterAdapter.getItem(mLastIndex).selected = false;
                mChapterAdapter.notifyItemChanged(mLastIndex);
            }
            mLastIndex = mIndex;
        }
    }


    private void scroll(){

        if (mChapterAdapter == null || mRV_Chapter == null)
            return;

        if (mIndex < 5)
            return;

        if (mIndex + 5 >= mChapterAdapter.getData().size())
            return;

        mRV_Chapter.scrollToPosition(mIndex + 5);
    }
}
