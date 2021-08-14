package com.wong.novel.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;

import com.wong.novel.R;

public class CommonDialog {

    public static Dialog bottom_dialog(Context context,int layoutRes){
        Dialog dialog = new Dialog(context, R.style.dialog_bottom);
        dialog.setContentView(layoutRes);
        dialog.getWindow().setWindowAnimations(R.style.dialog_bottom_Animation);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().getAttributes().width = context.getResources().getDisplayMetrics().widthPixels;
        return dialog;
    }


    public static Dialog max_dialog(Context context,int layoutRes){
        Dialog dialog = new Dialog(context, R.style.dialog_bottom);
        dialog.setContentView(layoutRes);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().getAttributes().width = context.getResources().getDisplayMetrics().widthPixels;
        return dialog;
    }

    public static Dialog common_dialog(Context context,int layoutRes){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(layoutRes);
        return dialog;
    }
}
