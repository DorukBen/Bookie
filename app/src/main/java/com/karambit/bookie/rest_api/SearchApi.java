package com.karambit.bookie.rest_api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by doruk on 2.02.2017.
 */

public interface SearchApi {
    @FormUrlEncoded
    @POST("Search")
    Call<ResponseBody> getSearchResults(
            @Field("email") String email,
            @Field("password") String password,
            @Field("searchString") String searchString,
            @Field("searchGenre") int searchGenre,
            @Field("searchPressed") boolean searchPressed);
}
