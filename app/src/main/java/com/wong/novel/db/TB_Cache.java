package com.wong.novel.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wong.novel.bean.Content;
import com.wong.novel.util.RxExt;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class TB_Cache {

    /*  |----------------------|
        | cache_id             | 主键
        | book_id              | 书籍ID
        | cache_chapter_index  | 缓存章节序号
        | cache_chapter_title  | 缓存章节标题
        | cache_chapter_content| 缓存章节正文
    */

    public static final String TB_NAME = "tb_cache";

    public static final String  COLUMN_CACHE_ID         = "cache_id",
                                COLUMN_BOOK_COLLECT_ID  = "book_collect_id",
                                COLUMN_CHAPTER_INDEX    = "chapter_position",
                                COLUMN_CHAPTER_TITLE    = "chapter_title",
                                COLUMN_CHAPTER_CONTENT  = "chapter_content";

    public static String TB_CACHE = "create table " + TB_NAME + "(" +
            ""+COLUMN_CACHE_ID       +" Integer primary key autoincrement," +
            ""+COLUMN_BOOK_COLLECT_ID+" Integer," +
            ""+COLUMN_CHAPTER_INDEX  +" Integer," +
            ""+COLUMN_CHAPTER_TITLE  +" text," +
            ""+COLUMN_CHAPTER_CONTENT+" text" +
            ")";


    public static void insert(SQLiteDatabase db, ContentValues values){
        db.insert(TB_NAME,null,values);
    }


    public static Observable<List<Content>> query(SQLiteDatabase db, String selection, String[] args,String order,String limit){
        Cursor cursor = db.query(TB_NAME,null,selection,args,null,null,order,limit);
        if (cursor == null)
            return null;

        List<Content> data = new ArrayList<>();
        Content content;
        if (cursor.moveToFirst()){
            while (!cursor.isAfterLast()){
                content = new Content();
                content.index   = cursor.getInt(cursor.getColumnIndex(COLUMN_CHAPTER_INDEX));
                content.title   = cursor.getString(cursor.getColumnIndex(COLUMN_CHAPTER_TITLE));
                content.content = cursor.getString(cursor.getColumnIndex(COLUMN_CHAPTER_CONTENT));
                data.add(content);
                cursor.moveToNext();
            }
        }

        return RxExt.create(data);
    }


    public static void delete(SQLiteDatabase db,String selection,String[] args){
        db.delete(TB_NAME,selection,args);
    }
}
