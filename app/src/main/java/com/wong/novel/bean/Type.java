package com.wong.novel.bean;

import com.squareup.moshi.Json;

public class Type {
    @Json(name = "type_url")   public String url;
    @Json(name = "type_title") public String title;
}
