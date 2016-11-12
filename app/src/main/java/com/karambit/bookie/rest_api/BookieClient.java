package com.karambit.bookie.rest_api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by orcan on 11/12/16.
 */

public class BookieClient {

    public static final String BASE_URL = "http://46.101.171.117/bookie/server/";

    private static Retrofit sRetrofit;

    public static Retrofit getClient() {
        if (sRetrofit == null) {
            sRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return sRetrofit;
    }
}
