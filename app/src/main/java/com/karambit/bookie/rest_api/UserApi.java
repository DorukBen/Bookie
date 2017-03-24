package com.karambit.bookie.rest_api;

import android.support.annotation.Nullable;

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
    @POST("AddLovedGenre")
    Call<ResponseBody> setLovedGenres(@Field("email") String email,
                                      @Field("password") String password,
                                      @Field("genreCodes") String lovedGenres);

    @GET("UserProfilePageComponents")
    Call<ResponseBody> getUserProfilePageComponents(@Query("email") String email,
                                                    @Query("password") String password,
                                                    @Query("userID") int anotherUserId);

    @FormUrlEncoded
    @POST("UpdateUserDetails")
    Call<ResponseBody> updateUserDetails(@Field("email") String email,
                                      @Field("password") String password,
                                      @Field("name") String name,
                                      @Field("bio") String bio,
                                      @Field("latitude") double latitude,
                                      @Field("longitude") double longitude);

    @FormUrlEncoded
    @POST("UpdateUserDetails")
    Call<ResponseBody> updateUserDetails(@Field("email") String email,
                                         @Field("password") String password,
                                         @Field("name") String name,
                                         @Field("bio") String bio);

    @FormUrlEncoded
    @POST("Feedback")
    Call<ResponseBody> uploadFeedBack (@Field("email") String email,
                                         @Field("password") String password,
                                         @Field("feedback") String name);

    @FormUrlEncoded
    @POST("UserValidation")
    Call<ResponseBody> isPasswordCorrect (@Field("email") String email,
                                       @Field("password") String password,
                                       @Field("givenPassword") String givenPassword);

    @FormUrlEncoded
    @POST("UserChangePassword")
    Call<ResponseBody> uploadNewPassword (@Field("email") String email,
                                          @Field("password") String password,
                                          @Field("newPassword") String newPassword);

    @FormUrlEncoded
    @POST("ReportUser")
    Call<ResponseBody> uploadUserReport (@Field("email") String email,
                                         @Field("password") String password,
                                         @Field("userID") int userId,
                                         @Field("reportCode") int reportCode,
                                         @Field("reportInfo") String reportInfo);

    @FormUrlEncoded
    @POST("BlockUser")
    Call<ResponseBody> uploadUserBlock (@Field("email") String email,
                                         @Field("password") String password,
                                         @Field("userID") int userId);

    @FormUrlEncoded
    @POST("DeleteMessage")
    Call<ResponseBody> deleteMessages (@Field("email") String email,
                                        @Field("password") String password,
                                        @Field("messageIDs") String messageIDs);
}
