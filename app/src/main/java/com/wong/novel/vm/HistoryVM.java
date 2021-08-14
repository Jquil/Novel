package com.wong.novel.vm;

import androidx.lifecycle.ViewModel;

import com.wong.novel.bean.History;

import java.util.ArrayList;
import java.util.List;

public class HistoryVM extends ViewModel {

    public List<History> histories = new ArrayList<>();
}
