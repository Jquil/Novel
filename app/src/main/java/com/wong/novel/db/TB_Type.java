package com.wong.novel.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wong.novel.bean.Type;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class TB_Type {

    /*  |-----------|
        | tp_id     | 主键
        | tp_name   | 类别名称
        | tp_src    | 类别地址
    */

    public static final String TB_NAME = "tb_type";

    private static final String COLUMN_NAME = "tp_name",
                                COLUMN_SRC  = "tp_src";

    public static final String TB_TYPE = "create table " + TB_NAME + "(" +
            "tp_id Integer primary key autoincrement," +
            "tp_name text," +
            "tp_src text" +
            ")";


    public static void insert(SQLiteDatabase db,List<Type> data){
        if (data == null)
            return;

        int size = data.size();
        ContentValues values;

        for (int i = 0; i < size; i++){
            values = new ContentValues();
            values.put(COLUMN_NAME,data.get(i).title);
            values.put(COLUMN_SRC,data.get(i).url);
            db.insert(TB_NAME,null,values);
            values.clear();
        }
    }


    public static Observable<List<Type>> get(SQLiteDatabase db){
        List<Type> data = new ArrayList<>();
        Type type;
        Cursor cursor = db.query(TB_NAME,null,null,null,null,null,null);
        if (cursor != null && cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                type = new Type();
                type.title = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                type.url   = cursor.getString(cursor.getColumnIndex(COLUMN_SRC));
                data.add(type);
                cursor.moveToNext();
            }
        }

        Observable<List<Type>> o = Observable.create((ObservableEmitter<List<Type>> emitter) -> {
            emitter.onNext(data);
            emitter.onComplete();
        });
        return o;
    }
}
