package com.wong.novel.bean;

import com.squareup.moshi.Json;

import java.io.Serializable;

public class Book implements Serializable {
    public int     collect_id;
    public int     order;
    public int     lastIndex;
    public boolean isUpdateChapter;
    @Json(name = "book_id")             public String id;
    @Json(name = "book_src")            public String src;
    @Json(name = "book_img")            public String img;
    @Json(name = "book_name")           public String name;
    @Json(name = "book_desc")           public String desc;
    @Json(name = "book_author")         public String author;
    @Json(name = "book_type")           public String type;
    @Json(name = "book_update_time")    public String update_time;
    @Json(name = "book_update_chapter") public String update_chapter;
}
