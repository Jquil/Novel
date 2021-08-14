package com.wong.novel.db;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.wong.novel.constant.App;

public class NovelDB extends SQLiteOpenHelper {

    private static final String TAG = "NovelDB";

    private static NovelDB mInstance;

    private static final String mDBName = "novel";

    private static final int mVersion = 1;

    public static NovelDB getInstance(){
        if (mInstance == null){
            synchronized (NovelDB.class){
                if (mInstance == null){
                    mInstance = new NovelDB(App.instance.getApplicationContext(),mDBName,null,mVersion);
                }
            }
        }
        return mInstance;
    }


    private NovelDB(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TB_Cache.TB_CACHE);
        db.execSQL(TB_Collect.TB_COLLECT);
        db.execSQL(TB_Column.TB_COLUMN);
        db.execSQL(TB_History.TB_HISTORY);
        db.execSQL(TB_Recommend.TB_RECOMMEND);
        db.execSQL(TB_Type.TB_TYPE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){

        }
    }

}
