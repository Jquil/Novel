package com.wong.novel.ui.fragment;

import android.app.Dialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wong.novel.R;
import com.wong.novel.adapter.CollectAdapter;
import com.wong.novel.base.BaseMVPFragment;
import com.wong.novel.bean.Book;
import com.wong.novel.db.TB_Collect;
import com.wong.novel.mvp.contract.SelfContract;
import com.wong.novel.mvp.presenter.SelfPresenter;
import com.wong.novel.ui.activity.ReadActivity;
import com.wong.novel.ui.dialog.CommonDialog;
import com.wong.novel.vm.CollectVM;
import com.wong.novel.widget.MultipleStatusView;

import java.util.ArrayList;
import java.util.List;

public class BookSelfFragment extends BaseMVPFragment<SelfContract.View,SelfContract.Presenter> implements SelfContract.View, View.OnClickListener {

    private static final String TAG = "BookSelfFragment";

    private static CollectVM mCollectVM;

    private RecyclerView   mRVCollect;

    private CollectAdapter mCollectAdapter;

    private Dialog mBottomDialog;

    private TextView mTV_Dialog_Name;

    private Button  mBtn_Dialog_Remove,
                    mBtn_Dialog_Cache,
                    mBtn_Dialog_Order,
                    mBtn_Dialog_Cancel;

    private Book mBook;

    private int mItemPosition,mUpdateSize,mCollectId;

    private boolean isNull;

    private View mHeaderView;

    private List<Book> mUpdateBooks;

    @Override
    protected SelfContract.Presenter onCreatePresenter() {
        return new SelfPresenter();
    }


    @Override
    protected int attachLayoutRes() {
        return R.layout.layout_rv;
    }


    @Override
    protected void initView(View view) {
        super.initView(view);
        mRVCollect = view.findViewById(R.id.rv);
    }


    @Override
    protected void lazyLoad() {
        mUpdateBooks    = new ArrayList<>();
        mCollectVM      = ViewModelProviders.of(this).get(CollectVM.class);
        mCollectAdapter = new CollectAdapter(R.layout.item_collect,mCollectVM.collects);
        mCollectAdapter.bindToRecyclerView(mRVCollect);
        mRVCollect.setLayoutManager(new LinearLayoutManager(getContext()));
        mRVCollect.setAdapter(mCollectAdapter);

        //
        if (mCollectVM.collects.size() == 0){
            showLoading();
            mPresenter.getCollectBooks();
        }

        // Item Long Click -> 显示Dialog
        mCollectAdapter.setOnItemLongClickListener((BaseQuickAdapter adapter, View view, int position) -> {
            if (mBottomDialog == null){
                mBottomDialog      = CommonDialog.bottom_dialog(getContext(),R.layout.dialog_self);
                mTV_Dialog_Name    = mBottomDialog.findViewById(R.id.book_name);
                mBtn_Dialog_Remove = mBottomDialog.findViewById(R.id.btn_remove);
                mBtn_Dialog_Cache  = mBottomDialog.findViewById(R.id.btn_cache);
                mBtn_Dialog_Order  = mBottomDialog.findViewById(R.id.btn_order);
                mBtn_Dialog_Cancel = mBottomDialog.findViewById(R.id.btn_cancel);
                mBtn_Dialog_Cancel.setOnClickListener(this);
                mBtn_Dialog_Order .setOnClickListener(this);
                mBtn_Dialog_Cache .setOnClickListener(this);
                mBtn_Dialog_Remove.setOnClickListener(this);
            }

            mItemPosition = position;
            mBook = mCollectAdapter.getItem(position);

            if (mBook != null){
                mCollectId = mBook.collect_id;
                mTV_Dialog_Name.setText(mBook.name);
                mBtn_Dialog_Order.setText(mBook.order == TB_Collect.ORDER ? R.string.tip_cancel_order : R.string.tip_order);
            }

            mBottomDialog.show();
            return false;
        });

        // Item Click -> Jump to Read
        mCollectAdapter.setOnItemClickListener((BaseQuickAdapter adapter, View view, int position) -> {
            mBook = mCollectAdapter.getItem(position);
            if (mBook.isUpdateChapter){
                mBook.isUpdateChapter = false;
                mPresenter.setReading(mBook.order == TB_Collect.UPDATE,mBook.collect_id);
                if (mBook.order == TB_Collect.ORDER){
                    mCollectAdapter.notifyItemChanged(position);
                }
                else{
                    mCollectAdapter.remove(position);
                    mCollectAdapter.addData(mBook);
                }
            }
            ReadActivity.go(getContext(),mCollectAdapter.getItem(position));
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_cancel:
                if (mBottomDialog != null){
                    mBottomDialog.dismiss();
                }
                break;

            case R.id.btn_remove:
                mBottomDialog.dismiss();

                mCollectAdapter.remove(mItemPosition);
                mCollectVM.collects.remove(mBook);
                mPresenter.removeBook(mBook.collect_id);
                mPresenter.deleteCache(mBook.collect_id);
                if (mCollectAdapter.getItemCount() == 0){
                    showEmpty();
                }
                break;

            case R.id.btn_cache:
                Toast.makeText(getContext(),R.string.tip_no_open,Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_order:
                if (mBook.order == TB_Collect.COMMON){
                    if (mItemPosition != 0){
                        mCollectAdapter.remove(mItemPosition);
                        mCollectAdapter.addData(0,mBook);
                    }
                    mBook.order = TB_Collect.ORDER;
                    mCollectAdapter.notifyItemChanged(0);
                    mPresenter.setOrCancelTop(true,mBook.collect_id);
                }
                else if (mBook.order == TB_Collect.ORDER){
                    mCollectAdapter.remove(mItemPosition);
                    mCollectAdapter.addData(mBook);
                    mBook.order = TB_Collect.COMMON;
                    mCollectAdapter.notifyItemChanged(mCollectAdapter.getData().size()-1);
                    mPresenter.setOrCancelTop(false,mBook.collect_id);
                }
                mBottomDialog.dismiss();
                break;
        }
    }


    @Override
    public void setCollectBooks(List<Book> data) {
        if (data == null ||data.size() == 0 ){
            isNull = true;
        }
        else{
            mCollectVM.collects.addAll(data);
            mCollectAdapter.notifyDataSetChanged();
            mHeaderView = LayoutInflater.from(this.getContext()).inflate(R.layout.layout_header,null);
            mCollectAdapter.addHeaderView(mHeaderView);
            mPresenter.checkBooksUpdate(data);
        }

        if (!isFirstLoadComplete){
            hideLoading();
            isFirstLoadComplete = true;
        }
    }


    @Override
    public void checkUpdateBook(Book book,boolean flag) {
        /*
         *  如果书籍有更新：
         *      1. 本身是置顶的，不改变位置
         *      2. 不是置顶，移动到最后一个置顶书籍的下方
         *      3. 更新完的UpdateBooks是乱序的，所以要从新排序
         *      4. 优先级：置顶&更新 >> 置顶 >> 更新 >> 普通
         *      5. 如果所有书籍都没有更新，那就不用重置
         * */
        mUpdateBooks.add(book);

        if (flag){
            mUpdateSize++;
        }

        if (mUpdateBooks.size() == mCollectVM.collects.size()){
            mCollectAdapter.removeHeaderView(mHeaderView);
            if (mUpdateSize > 0){
                resetCollectList(mUpdateBooks);
            }
        }
    }


    @Override
    public void cacheBook() {

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
            if (isNull){
                mMultipleStatusView.hideLoading(MultipleStatusView.STATUS_EMPTY);
            }
            else{
                mMultipleStatusView.hideLoading(MultipleStatusView.STATUS_CONTENT);
            }
        }
    }


    @Override
    public void showEmpty() {
        if (mMultipleStatusView != null){
            mMultipleStatusView.showEmpty();
        }
    }


    @Override
    public void hideEmpty() {

    }


    @Override
    public void showError() {
        /* BookSelfFragment (暂时)只存在一个网络请求的地方 => 检查更新 */
        if (mHeaderView != null){
            mCollectAdapter.removeHeaderView(mHeaderView);
        }
    }


    @Override
    public void hideError() {

    }


    @Override
    public void onResume() {
        super.onResume();
        if (isFirstLoadComplete && mCollectAdapter != null){
            mCollectAdapter.notifyDataSetChanged();
        }

        if (mCollectAdapter != null && mMultipleStatusView != null){
            if (mCollectAdapter.getData().size() > 0 && mMultipleStatusView.getViewVisible(MultipleStatusView.STATUS_EMPTY) == View.VISIBLE) {
                mMultipleStatusView.hideEmpty(MultipleStatusView.STATUS_CONTENT);
            }

            else if (mCollectAdapter.getItemCount() == 0 && mMultipleStatusView.getViewVisible(MultipleStatusView.STATUS_EMPTY) == View.GONE){
                mMultipleStatusView.showEmpty();
            }
        }
    }


    private void resetCollectList(List<Book> data){
        List<Book> mTops    = new ArrayList<>(),
                   mCommons = new ArrayList<>(),
                   mFinals  = new ArrayList<>();
        for (Book item : data){
            if (item.order == TB_Collect.ORDER){
                if (item.isUpdateChapter){
                    mTops.add(0,item);
                }
                else{
                    mTops.add(item);
                }
            }
            else if (item.order == TB_Collect.UPDATE){
                mCommons.add(0,item);
            }
            else {
                mCommons.add(item);
            }
        }
        mFinals.addAll(mTops);
        mFinals.addAll(mCommons);

        mCollectVM.collects.clear();
        mCollectVM.collects.addAll(mFinals);
        mCollectAdapter.notifyDataSetChanged();
    }


    public static void add(Book book){
        if (mCollectVM != null && book != null){
            mCollectVM.collects.add(book);
        }
    }


    public static boolean isExitBook(String id){
        if (mCollectVM != null){
            return mCollectVM.isExitBook(id);
        }
        return false;
    }


    public static void remove(String book_id){
        if (mCollectVM == null)
            return;

        for (Book book : mCollectVM.collects){
            if (book.id.equals(book_id)){
                mCollectVM.collects.remove(book);
                break;
            }
        }
    }


    public static void notifyBookLastReadIndex(int id,int lastIndex){
        for (Book book : mCollectVM.collects){
            if (book.collect_id == id){
                book.lastIndex = lastIndex;
                break;
            }
        }
    }
}
