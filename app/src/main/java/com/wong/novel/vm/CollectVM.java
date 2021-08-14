package com.wong.novel.vm;

import androidx.lifecycle.ViewModel;

import com.wong.novel.bean.Book;

import java.util.ArrayList;
import java.util.List;

public class CollectVM extends ViewModel {

    public List<Book> collects = new ArrayList<>();


    public boolean isExitBook(String book_id){
        for (Book book : collects){
            if (book.id.equals(book_id)){
                return true;
            }
        }
        return false;
    }
}
