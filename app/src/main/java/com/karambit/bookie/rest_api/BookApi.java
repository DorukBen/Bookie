package com.karambit.bookie.rest_api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
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
    Call<ResponseBody> addBookRequest(
            @Field("email") String email,
            @Field("password") String password,
            @Field("bookID") int bookId,
            @Field("requesterID") int requesterId, // TODO Server key change ("fromUserID" -> "requesterID")
            @Field("responderID") int responderId, // TODO Server key change ("toUserID" -> "responderID")
            @Field("requestType") int requestType);

    @FormUrlEncoded
    @POST("BookRequest")
    Call<ResponseBody> getBookRequests(
            @Field("email") String email,
            @Field("password") String password,
            @Field("bookID") int bookId);

    @FormUrlEncoded
    @POST("ReportBook")
    Call<ResponseBody> uploadBookReport (@Field("email") String email,
                                         @Field("password") String password,
                                         @Field("bookID") int bookId,
                                         @Field("reportCode") int reportCode,
                                         @Field("reportInfo") String reportInfo);

    @FormUrlEncoded
    @POST("UpdateBookDetails")
    Call<ResponseBody> updateBookDetails (@Field("email") String email,
                                         @Field("password") String password,
                                         @Field("bookID") int bookId,
                                         @Field("bookName") String name,
                                         @Field("author") String author,
                                         @Field("genreCode") int genreCode);

    @FormUrlEncoded
    @POST("SetBookStateLost")
    Call<ResponseBody> setBookStateLost (@Field("email") String email,
                                          @Field("password") String password,
                                          @Field("bookID") int bookId);

}
