package com.wong.novel.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wong.novel.bean.Column;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class TB_Column {

    /*  |-----------|
        | cm_id     | 主键
        | cm_name   | 栏目名称
        | cm_src    | 栏目地址
    */

    public static final String TB_NAME = "tb_column";

    private static final String COLUMN_NAME = "cm_name",
                                COLUMN_SRC  = "cm_src";

    public static final String TB_COLUMN = "create table " + TB_NAME + "(" +
            "cm_id Integer primary key autoincrement," +
            "cm_name text," +
            "cm_src text" +
            ")";


    public static void insert(SQLiteDatabase db,List<Column> data){
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


    public static Observable<List<Column>> get(SQLiteDatabase db){
        List<Column> data = new ArrayList<>();
        Cursor cursor = db.query(TB_NAME,null,null,null,null,null,null);
        Column column;
        if (cursor != null && cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                column = new Column();
                column.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                column.src  = cursor.getString(cursor.getColumnIndex(COLUMN_SRC));
                data.add(column);
                cursor.moveToNext();
            }
            cursor.close();
        }

        Observable<List<Column>> o = Observable.create((ObservableEmitter<List<Column>> emitter) -> {
            emitter.onNext(data);
            emitter.onComplete();
        });

        return o;
    }
}
