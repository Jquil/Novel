package com.wong.novel.bean;

import java.io.Serializable;
import java.util.List;

public class MyList<T> implements Serializable {

    public List<T> data;

    public MyList(List<T> data) {
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }
}
