package com.karambit.bookie.rest_api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * This interface contains all the user api methods used by retrofit
 *
 * Created by orcan on 11/12/16.
 */

public interface UserApi {

    @FormUrlEncoded
    @POST("UserLogin")
    Call<ResponseBody> login(@Field("email") String email,
                             @Field("password") String password);

    @FormUrlEncoded
    @POST("UserRegister")
    Call<ResponseBody> register(@Field("email") String email,
                                @Field("password") String password,
                                @Field("nameSurname") String nameSurname);

    @FormUrlEncoded
    @POST("addLovedGenre")
    Call<ResponseBody> setLovedGenres(@Field("email") String email,
                                      @Field("password") String password,
                                      @Field("genreCodes") String lovedGenres);

    @GET("UserProfilePageComponents")
    Call<ResponseBody> getUserProfilePageComponents(@Query("email") String email,
                                                    @Query("password") String password,
                                                    @Query("userID") int anotherUserId);
}
