package com.karambit.bookie.rest_api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by orcan on 11/16/16.
 */

public interface BookApi {

    @GET("HomePage")
    Call<ResponseBody> getHomePageBooks(
                                  @Query("email") String email,
                                  @Query("password") String password,
                                  @Query("bookIDs") String fetchedBooks);

    @FormUrlEncoded
    @POST("BookDetails")
    Call<ResponseBody> getBookPageArguments(
            @Field("email") String email,
            @Field("password") String password,
            @Field("bookID") int bookId);

    @FormUrlEncoded
    @POST("BookAddInteraction")
    Call<ResponseBody> addBookInteraction(
            @Field("email") String email,
            @Field("password") String password,
            @Field("bookID") int bookId,
            @Field("interactionType") int interactionType);

    @FormUrlEncoded
    @POST("BookAddRequest")
    Call<ResponseBody> addBookRequests(
            @Field("email") String email,
            @Field("password") String password,
            @Field("bookID") int bookId,
            @Field("fromUserID") int fromUserId,
            @Field("toUserID") int toUserId,
            @Field("requestType") int requestType);

    @FormUrlEncoded
    @POST("BookRequest")
    Call<ResponseBody> getBookRequests(
            @Field("email") String email,
            @Field("password") String password,
            @Field("bookID") int bookId);
}
