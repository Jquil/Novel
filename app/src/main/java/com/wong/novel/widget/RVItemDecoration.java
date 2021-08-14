package com.wong.novel.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wong.novel.R;

public class RVItemDecoration extends RecyclerView.ItemDecoration {

    private static final int DividerHeight = 2;

    private Context  mContext;
    private int      mOrientation;
    private int[]    attr;
    private Drawable mDivider;
    private Paint    mPaint;


    public RVItemDecoration(Context context,int orientation) {
        mContext     = context;
        mOrientation = orientation;
        attr         = new int[]{ android.R.attr.listDivider };
        mPaint       = new Paint();
        mDivider     = context.obtainStyledAttributes(attr).getDrawable(0);
        mPaint.setColor(context.getResources().getColor(R.color.decoration));
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        if (mOrientation == LinearLayoutManager.VERTICAL){
            drawVertical(c,parent);
        }
    }


    private void drawVertical(Canvas canvas,RecyclerView parent){
        int left       = parent.getPaddingLeft();
        int right      = parent.getMeasuredWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        int top,bottom;
        RecyclerView.LayoutParams lp;
        for (int i = 0; i < childCount; i++){
            View child = parent.getChildAt(i);
            lp     = (RecyclerView.LayoutParams)child.getLayoutParams();
            top    = child.getBottom() + lp.bottomMargin;
            bottom = top + DividerHeight;
            //mDivider.setBounds(left,top,right,bottom);
            //mDivider.draw(canvas);
            canvas.drawRect(left,top,right,bottom,mPaint);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0,0,0,DividerHeight);
    }
}
