package com.karambit.bookie.rest_api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by doruk on 24.02.2017.
 */

public interface FcmApi {

    @FormUrlEncoded
    @POST("UserUpdateFcmToken")
    Call<ResponseBody> sendFcmTokenToServer(
            @Field("email") String email,
            @Field("password") String password,
            @Field("token") String fcmToken);

    @FormUrlEncoded
    @POST("SendMessage")
    Call<ResponseBody> sendMessage(
            @Field("email") String email,
            @Field("password") String password,
            @Field("message") String message,
            @Field("toUserID") int toUserID,
            @Field("oldMessageID") int oldMessageID);

    @FormUrlEncoded
    @POST("UpdateMessageState")
    Call<ResponseBody> uploadMessageState(
            @Field("email") String email,
            @Field("password") String password,
            @Field("messageID") int messageID,
            @Field("messageState") int state);
}
