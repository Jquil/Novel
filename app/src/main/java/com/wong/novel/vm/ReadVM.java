package com.wong.novel.vm;

import androidx.lifecycle.ViewModel;

import com.wong.novel.bean.Content;

import java.util.ArrayList;
import java.util.List;

public class ReadVM extends ViewModel {

    public List<Content> mContentList = new ArrayList<>();
}
