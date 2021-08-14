package com.wong.novel.bean;

import com.squareup.moshi.Json;

public class Content {

    public int index;

    @Json(name = "chapter_title")   public String title;

    @Json(name = "chapter_content") public String content;
}
