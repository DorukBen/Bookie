package com.karambit.bookie.rest_api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by orcan on 11/16/16.
 */

public interface BookApi {

    @FormUrlEncoded
    @POST("insert_book.php")
    Call<ResponseBody> insertBook(@Field("email") String email,
                                  @Field("password") String password,
                                  @Field("book_name") String bookName,
                                  @Field("book_state") int bookState,
                                  @Field("author") String author,
                                  @Field("genre_code") int genreCode,
                                  @Field("added_by") int addedBy,
                                  @Field("book_picture_url") String bookPictureUrl,
                                  @Field("book_picture_thumbnail_url") String thumbnailUrl);
}
