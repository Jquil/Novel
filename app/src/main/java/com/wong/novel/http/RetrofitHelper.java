package com.wong.novel.http;

import com.wong.novel.constant.ApiService;
import com.wong.novel.constant.Constant;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitHelper {

    public static ApiService service = getRetrofit().create(ApiService.class);

    private static final int TIME_OUT = 60;

    private static Retrofit retrofit;

    private static Retrofit getRetrofit(){
        if (retrofit == null){
            synchronized (Retrofit.class){
                if (retrofit == null){
                    retrofit = new Retrofit.Builder()
                                           .baseUrl(Constant.root)
                                           .client(getOkHttpClient())
                                           .addConverterFactory(MoshiConverterFactory.create())
                                           .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                                           .build();
                }
            }
        }
        return retrofit;
    }


    private static OkHttpClient getOkHttpClient(){
        return new OkHttpClient().newBuilder()
                .connectTimeout(TIME_OUT,TimeUnit.SECONDS)
                .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT,TimeUnit.SECONDS)
                .build();
    }
}
