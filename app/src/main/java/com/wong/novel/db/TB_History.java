package com.wong.novel.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wong.novel.bean.History;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class TB_History {

    /*  |--------------|
        | ht_id        | 主键
        | ht_key       | 搜索关键字
    */

    public static final String TB_NAME = "tb_history";


    private static final String COLUMN_ID  = "ht_id",
                                COLUMN_KEY = "ht_key";

    public static final String TB_HISTORY = "create table " + TB_NAME + "(" +
            "ht_id Integer primary key autoincrement," +
            "ht_key text" +
            ")";

    public static Observable<Integer> insert(SQLiteDatabase db,String key){
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY,key);
        long l = db.insert(TB_NAME,null,values);

        Observable<Integer> o = Observable.create((ObservableEmitter<Integer> emitter) -> {
            emitter.onNext(Integer.valueOf(String.valueOf(l)));
            emitter.onComplete();
        });

        return o;
    }


    public static Observable<List<History>> get(SQLiteDatabase db){
        List<History> data = new ArrayList<>();
        History history;
        Cursor cursor = db.query(TB_NAME,null,null,null,null,null,null);
        if (cursor != null && cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                history = new History();
                history.id  = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                history.key = cursor.getString(cursor.getColumnIndex(COLUMN_KEY));
                data.add(history);
                cursor.moveToNext();
            }
        }

        Observable<List<History>> o = Observable.create((ObservableEmitter<List<History>> emitter) -> {
            emitter.onNext(data);
            emitter.onComplete();
        });
        return o;
    }


    public static void deleteAll(SQLiteDatabase db){
        db.delete(TB_NAME,null,null);
    }


    public static void deleteById(SQLiteDatabase db,int id){
        db.delete(TB_NAME,COLUMN_ID + "=?",new String[]{ String.valueOf(id) });
    }
}
