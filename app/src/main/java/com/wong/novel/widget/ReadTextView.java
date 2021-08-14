package com.wong.novel.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.wong.novel.R;
import com.wong.novel.constant.Constant;
import com.wong.novel.util.Mode;
import com.wong.novel.util.SP;

public class ReadTextView extends TextView implements Mode {

    private static final String TAG = "ReadTextView";

    private Context mContext;

    private int mPadding = 45;

    private boolean isReset;

    private Paint mPaint;

    private String mTitle;

    public ReadTextView(Context context,String title) {
        super(context);
        mContext = context;
        mTitle   = title;
        init();
    }


    public ReadTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    private void init(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mContext.getResources().getColor(R.color.textColor));
        mPaint.setTextSize(32);
        setPadding(mPadding,mPadding,mPadding,mPadding);

        if (SP.getInstance().getBoolean(Constant.key_night,false)){
            setBackgroundColor(mContext.getResources().getColor(R.color.background));
        }
        else{
            setBackgroundResource(R.drawable.paper);
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!isReset){
            reset();
            isReset = true;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText(mTitle,25,35,mPaint);
    }


    private void reset(){
        CharSequence text = getText();
        if (TextUtils.isEmpty(text))
            return;
        CharSequence newText = text.subSequence(0,getCharNum());
        setText(newText.toString());
        if (Call != null){
            Call.getTextLength(newText.length());
        }
    }


    private int getCharNum(){
        onPreDraw();
        Layout layout = getLayout();
        return layout.getLineEnd(getLineNum(layout));
    }


    private int getLineNum(Layout layout){
        int topOfLast = getHeight() - getPaddingTop() - getPaddingBottom() - getLineHeight();
        return layout.getLineForVertical(topOfLast);
    }


    public interface Call{
        void getTextLength(int length);
    }


    private Call Call;


    public void setCall(ReadTextView.Call call) {
        Call = call;
    }

    @Override
    public void change(boolean isNight) {
        if (isNight){
            setTextColor(Color.parseColor("#c1c1c1"));
            setBackgroundColor(mContext.getResources().getColor(R.color.background));
        }
        else{
            setTextColor(Color.parseColor("#726e62"));
            setBackgroundResource(R.drawable.paper);
        }
    }


    public static class ReadTextMeasureView extends View{

        private static final String TAG = "ReadTextMeasureView";

        private Context mContext;

        private String  mContent;

        private TextPaint mPaint;

        private int       mWidth,mHeight,mLayoutWidth,mLayoutHeight,mPadding;

        private float     mSize;

        private Bitmap    mBitmap;

        private Canvas    mCanvas;

        private Layout    mLayout;

        public ReadTextMeasureView(Context context,String content,float size) {
            super(context);
            mContext = context;
            mContent = content;
            mSize    = size;
            init();
        }


        public ReadTextMeasureView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }


        private void init(){
            mPadding      = 45;
            mSize         = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,mSize,mContext.getResources().getDisplayMetrics());
            mWidth        = mContext.getResources().getDisplayMetrics().widthPixels;
            mHeight       = mContext.getResources().getDisplayMetrics().heightPixels;
            mLayoutWidth  = mWidth  - (mPadding * 2);
            mLayoutHeight = mHeight - (mPadding * 4);

            mPaint = new TextPaint();
            mPaint.setTextSize(mSize);

            mBitmap = Bitmap.createBitmap(mLayoutWidth,mLayoutHeight, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mLayout = new StaticLayout(mContent,mPaint,mLayoutWidth, Layout.Alignment.ALIGN_NORMAL,1.0f,0.0f,false);
            mLayout.draw(mCanvas);

            //Log.d(TAG,mLayoutWidth + " << -- >>" + mLayoutHeight);
        }


        public int getLength(){
            int topOfLast = mLayoutHeight;
            int lineNum   = mLayout.getLineForVertical(topOfLast);
            int charNum   = mLayout.getLineEnd(lineNum);
            return charNum;
            //Log.d(TAG,"CharNum => " + charNum);
        }

    }

}
