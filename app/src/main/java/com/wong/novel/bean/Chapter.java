package com.wong.novel.bean;

import com.squareup.moshi.Json;

import java.io.Serializable;

public class Chapter implements Serializable {

    public boolean selected;

    @Json(name = "chapter_index") public String index;

    @Json(name = "chapter_title") public String title;

    @Json(name = "chapter_url")   public String url;
}
