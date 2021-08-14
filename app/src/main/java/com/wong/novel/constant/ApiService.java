package com.wong.novel.constant;

import com.wong.novel.bean.Book;
import com.wong.novel.bean.Chapter;
import com.wong.novel.bean.Column;
import com.wong.novel.bean.Content;
import com.wong.novel.bean.Recommend;
import com.wong.novel.bean.Type;
import com.wong.novel.bean.UpdateChapter;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/getColumn")
    Observable<List<Column>> getColumn();

    @GET("/getType")
    Observable<List<Type>> getType();

    @GET("/getRecommendBook")
    Observable<List<Recommend>> getRecommendBook();

    @GET("/search")
    Observable<List<Book>> search(@Query("key") String key, @Query("page") int page);

    @GET("/getBook")
    Observable<Book> getBookInfo(@Query("book_src") String book_src);

    @GET("/getChapters")
    Observable<List<Chapter>> getChapterList(@Query("book_src") String book_src);

    @GET("/getBookByType")
    Observable<List<Book>> getTypeBookList(@Query("type_url") String type_url,@Query("page") int page);

    @GET("/getBookByColumn")
    Observable<List<Book>> getColumnBookList(@Query("column_src") String column_src,@Query("page") int page);

    @GET("/getContent")
    Observable<Content> getContent(@Query("chapter_url") String chapter_url);

    @GET("/checkUpdateChapter")
    Observable<UpdateChapter> getUpdateChapter(@Query("book_src") String book_src);
}
