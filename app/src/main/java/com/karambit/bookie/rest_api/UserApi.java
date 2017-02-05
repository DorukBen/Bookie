package com.karambit.bookie.rest_api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * This interface contains all the user api methods used by retrofit
 *
 * Created by orcan on 11/12/16.
 */

public interface UserApi {

    @FormUrlEncoded
    @POST("login.php")
    Call<ResponseBody> login(@Field("email") String email,
                             @Field("password") String password);

    @FormUrlEncoded
    @POST("register.php")
    Call<ResponseBody> register(@Field("name_surname") String nameSurname,
                                @Field("email") String email,
                                @Field("password") String password);

    @FormUrlEncoded
    @POST("profile_page_api.php")
    Call<ResponseBody> getUserProfilePageArguments(@Field("email") String email,
                                @Field("password") String password,
                                @Field("user_id") int anotherUserId);
}
