package com.wong.novel.widget.behavior;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ScaleDownBehavior extends FloatingActionButton.Behavior {

    private static final String TAG = "ScaleDownBehavior";

    private boolean isHide;

    public ScaleDownBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {
        //Log.d(TAG,axes + " --- " + ViewCompat.SCROLL_AXIS_VERTICAL);
        return  axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);

        if (dyConsumed > 0 && !isHide){
            //child.hide();
            child.animate()
                    .scaleY(0)
                    .scaleX(0)
                    .setDuration(200)
                    .start();
            isHide = true;
        }

        else if (dyConsumed < 0 && isHide){
            //child.show();
            child.animate()
                    .scaleY(1)
                    .scaleX(1)
                    .setDuration(200)
                    .start();
            isHide = false;
        }
    }
}
