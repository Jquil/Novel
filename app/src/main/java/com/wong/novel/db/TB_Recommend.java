package com.wong.novel.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wong.novel.bean.Column;
import com.wong.novel.bean.Recommend;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class TB_Recommend {

    /*  |-----------|
        | rc_id     | 主键
        | rc_name   | 书籍名称
        | rc_src    | 书籍地址
    */

    public static final String TB_NAME = "tb_recommend";

    private static final String COLUMN_NAME = "rc_name",
                                COLUMN_SRC  = "rc_src";

    public static final String TB_RECOMMEND = "create table " + TB_NAME + "(" +
            "rc_id Integer primary key autoincrement," +
            "rc_name text," +
            "rc_src text" +
            ")";


    public static void insert(SQLiteDatabase db, List<Recommend> data){
        if (data == null)
            return;

        int size = data.size();
        ContentValues values;

        for (int i = 0; i < size; i++){
            values = new ContentValues();
            values.put(COLUMN_NAME,data.get(i).name);
            values.put(COLUMN_SRC,data.get(i).src);
            db.insert(TB_NAME,null,values);
            values.clear();
        }
    }


    public static Observable<List<Recommend>> get(SQLiteDatabase db){
        List<Recommend> data = new ArrayList<>();
        Cursor cursor = db.query(TB_NAME,null,null,null,null,null,null);
        Recommend recommend;
        if (cursor != null && cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                recommend = new Recommend();
                recommend.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                recommend.src  = cursor.getString(cursor.getColumnIndex(COLUMN_SRC));
                data.add(recommend);
                cursor.moveToNext();
            }
            cursor.close();
        }

        Observable<List<Recommend>> o = Observable.create((ObservableEmitter<List<Recommend>> emitter) -> {
            emitter.onNext(data);
            emitter.onComplete();
        });

        return o;
    }


    public static void delete(SQLiteDatabase db){
        db.delete(TB_NAME,null,null);
    }
}
