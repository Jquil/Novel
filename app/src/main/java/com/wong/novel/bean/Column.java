package com.wong.novel.bean;

import com.squareup.moshi.Json;

public class Column {
    @Json(name = "column_name") public String name;
    @Json(name = "column_src")  public String src;
}
