package com.wong.novel.bean;

import com.squareup.moshi.Json;

public class Recommend {

    @Json(name = "book_src")  public String src;
    @Json(name = "book_name") public String name;
}
