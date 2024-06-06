package com.hypenet.realestaterehman.utils.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClintNyckel {
    public static Retrofit retrofit;
    public static final String BASE_URL = "https://www.nyckel.com/";
    public static RetrofitClintNyckel instance;
    private RetrofitClintNyckel(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static synchronized RetrofitClintNyckel getInstance(){
        if (retrofit == null){
            instance = new RetrofitClintNyckel();
            return  instance;
        }
        return instance;
    }
    public static Api getApi(){
        return retrofit.create(Api.class);
    }
}
