package com.wong.novel.db;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wong.novel.bean.Book;
import com.wong.novel.util.RxExt;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class TB_Collect {

    /*  |----------------------------|
        | cl_id                      | 主键
        | book_id                    | 书籍ID
        | book_src                   | 书籍地址
        | book_img                   | 书籍封面
        | book_name                  | 书籍名称
        | book_update_chapter        | 更新章节
        | book_is_update_chapter     | 是否更新章节(boolean)
        | read_chapter_index         | 最后阅读章节下标
        | read_chapter_position      | 最后阅读章节位置(第几页)
        | book_order                 | 排序  0 > 不排序
                                             1 > 有更新
                                             2 > 置顶
    */

    public static final String TB_NAME = "tb_collect",
                                COLUMN_ID                            = "cl_id",
                                COLUMN_BOOK_ID                       = "book_id",
                                COLUMN_BOOK_SRC                      = "book_src",
                                COLUMN_BOOK_IMG                      = "book_img",
                                COLUMN_BOOK_NAME                     = "book_name",
                                COLUMN_BOOK_UPDATE_CHAPTER           = "book_update_chapter",
                                COLUMN_BOOK_ORDER                    = "book_order",
                                COLUMN_BOOK_IS_UPDATE                = "book_is_update_chapter",
                                COLUMN_READ_CHAPTER_INDEX            = "read_chapter_index",
                                COLUMN_READ_CHAPTER_POSITION         = "read_chapter_position";

    public static final int  COMMON = 0,
                             UPDATE = 1,
                             ORDER  = 2;

    public static final String TB_COLLECT = "create table " + TB_NAME + "(" +
            ""+COLUMN_ID                        +" Integer primary key autoincrement," +
            ""+COLUMN_BOOK_ID                   +" Integer," +
            ""+COLUMN_BOOK_SRC                  +" text," +
            ""+COLUMN_BOOK_IMG                  +" text," +
            ""+COLUMN_BOOK_NAME                 +" text," +
            ""+COLUMN_BOOK_UPDATE_CHAPTER       +" text," +
            ""+COLUMN_BOOK_ORDER                +" Integer," +
            ""+COLUMN_BOOK_IS_UPDATE            +" text," +
            ""+COLUMN_READ_CHAPTER_INDEX        +" Integer," +
            ""+COLUMN_READ_CHAPTER_POSITION     +" Integer" +
            ")";


    public static Observable<Integer> insert(SQLiteDatabase db, Book book){
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_ID,book.id);
        values.put(COLUMN_BOOK_SRC,book.src);
        values.put(COLUMN_BOOK_IMG,book.img);
        values.put(COLUMN_BOOK_NAME,book.name);
        values.put(COLUMN_BOOK_UPDATE_CHAPTER,book.update_chapter);
        values.put(COLUMN_READ_CHAPTER_INDEX,-1);
        values.put(COLUMN_BOOK_ORDER,COMMON);

        long id = db.insert(TB_NAME,null,values);

        Observable<Integer> o = Observable.create((ObservableEmitter<Integer> emitter) -> {
            emitter.onNext(Integer.valueOf(String.valueOf(id)));
            emitter.onComplete();
        });

        return o;
    }


    public static Observable<List<Book>> get(SQLiteDatabase db){
        List<Book> data = new ArrayList<>();
        Book book;
        Cursor cursor = db.query(TB_NAME,null,null,null,null,null,COLUMN_BOOK_ORDER + " DESC");
        if (cursor != null && cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                book = new Book();
                book.collect_id           = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                book.id                   = cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_ID));
                book.img                  = cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_IMG));
                book.name                 = cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_NAME));
                book.src                  = cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_SRC));
                book.update_chapter       = cursor.getString(cursor.getColumnIndex(COLUMN_BOOK_UPDATE_CHAPTER));
                book.order                = cursor.getInt(cursor.getColumnIndex(COLUMN_BOOK_ORDER));
                book.isUpdateChapter      = cursor.getInt(cursor.getColumnIndex(COLUMN_BOOK_IS_UPDATE)) == 1 ? true : false;
                book.lastIndex            = cursor.getInt(cursor.getColumnIndex(COLUMN_READ_CHAPTER_INDEX));
                data.add(book);
                cursor.moveToNext();
            }
        }

        Observable<List<Book>> o = Observable.create((ObservableEmitter<List<Book>> emitter) ->{
            emitter.onNext(data);
            emitter.onComplete();
        });

        return o;
    }


    public static void remove(SQLiteDatabase db,int id){
        db.delete(TB_NAME,COLUMN_ID + " = ?",new String[]{ String.valueOf(id) });
    }


    public static void remove(SQLiteDatabase db,String book_id){
        db.delete(TB_NAME,COLUMN_BOOK_ID + " = ?",new String[]{ book_id });
    }


    public static void update(SQLiteDatabase db,int id,int flag_order){
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_ORDER,flag_order);
        db.update(TB_NAME,values,COLUMN_ID + "=?",new String[]{ String.valueOf(id) });
    }


    public static void update(SQLiteDatabase db,int id,ContentValues values){
        db.update(TB_NAME,values,COLUMN_ID + "=?",new String[]{ String.valueOf(id) });
    }


    public static Observable<Integer> queryOrderTopSize(SQLiteDatabase db) {
        Cursor cursor = db.query(TB_NAME, null, COLUMN_BOOK_ORDER + "=?", new String[]{String.valueOf(ORDER)}, null, null, null);
        Integer size = cursor.getCount();
        Observable<Integer> o = RxExt.create(size);
        return o;
    }
}
