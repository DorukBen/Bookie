/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karambit.bookie.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.helper.DBHandler;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.ErrorCodes;
import com.karambit.bookie.rest_api.FcmApi;
import com.karambit.bookie.rest_api.FcmDataTypes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";
    private static final int MESSAGE_NOTIFICATION_ID = 3000;
    private static ArrayList<Message> mNotificationMessages = new ArrayList<>();
    private static ArrayList<Integer> mNotificationUserIds = new ArrayList<>();

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (remoteMessage.getData().containsKey("fcmDataType")){
                if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_SENT_MESSAGE){
                    if (remoteMessage.getData().containsKey("messageID") && remoteMessage.getData().containsKey("message") && remoteMessage.getData().containsKey("sender")){
                        try {
                            JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("sender"));
                            User sender = User.jsonObjectToUser(jsonObject);
                            Message message = new Message(Integer.parseInt(remoteMessage.getData().get("messageID")),
                                    remoteMessage.getData().get("message"),
                                    sender,
                                    SessionManager.getCurrentUser(getApplicationContext()),
                                    Calendar.getInstance(),
                                    Message.State.DELIVERED);

                            sendMessageNotification(message);

                            DBHandler dbHandler = new DBHandler(getApplicationContext());
                            dbHandler.saveMessageToDataBase(message);

                            Intent intent = new Intent("com.karambit.bookie.MESSAGE_RECEIVED");
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("message", message);
                            intent.putExtras(bundle);
                            sendBroadcast(intent);

                            uploadMessageDeliveredStateToServer(message.getID());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_DELIVERED_MESSAGE){
                    if (remoteMessage.getData().containsKey("messageID")){
                        DBHandler dbHandler = new DBHandler(getApplicationContext());
                        dbHandler.updateMessageState(Integer.parseInt(remoteMessage.getData().get("messageID")), Message.State.DELIVERED);

                        Intent intent = new Intent("com.karambit.bookie.MESSAGE_DELIVERED");
                        Bundle bundle = new Bundle();
                        bundle.putInt("message_id", Integer.parseInt(remoteMessage.getData().get("messageID")));
                        intent.putExtras(bundle);
                        sendBroadcast(intent);
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_SEEN_MESSAGE){
                    DBHandler dbHandler = new DBHandler(getApplicationContext());
                    dbHandler.updateMessageState(Integer.parseInt(remoteMessage.getData().get("messageID")), Message.State.SEEN);

                    Intent intent = new Intent("com.karambit.bookie.MESSAGE_SEEN");
                    Bundle bundle = new Bundle();
                    bundle.putInt("message_id", Integer.parseInt(remoteMessage.getData().get("messageID")));
                    intent.putExtras(bundle);
                    sendBroadcast(intent);
                }
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param message FCM message body received.
     */
    private void sendMessageNotification(Message message) {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder;

        if (mNotificationUserIds.size() == 0 || !mNotificationUserIds.contains(message.getSender().getID())){
            mNotificationUserIds.add(message.getSender().getID());
        }
        mNotificationMessages.add(message);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.app_name));
        if (mNotificationUserIds.size() > 1){
            if (mNotificationMessages.size() > 1){
                inboxStyle.setSummaryText(mNotificationMessages.size() + " new messages from " + mNotificationUserIds.size() + " chats");
            }

            for (Message notificationMessage: mNotificationMessages){
                inboxStyle.addLine(notificationMessage.getSender().getName() + ": " + notificationMessage.getText());
            }

            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("message_user", null);
            intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(mNotificationMessages.size() + " new messages from " + mNotificationUserIds.size() + " chats")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setSound(defaultSoundUri)
                    .setAutoCancel(true)
                    .setStyle(inboxStyle)
                    .setPriority(10000)
                    .setContentIntent(pendingIntent);
        } else {
            inboxStyle.setBigContentTitle(message.getSender().getName());
            String notificationMessageText;
            if (mNotificationMessages.size() > 1){
                inboxStyle.setSummaryText(mNotificationMessages.size() + " new messages.");
                notificationMessageText = mNotificationMessages.size() + " new messages.";
            }else{
                inboxStyle.setSummaryText(mNotificationMessages.size() + " new message.");
                notificationMessageText = mNotificationMessages.size() + " new message.";
            }

            for (Message notificationMessage: mNotificationMessages){
                inboxStyle.addLine(notificationMessage.getText());
            }

            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("message_user", message.getSender());
            intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new NotificationCompat.Builder(this)
                    .setContentTitle(message.getSender().getName())
                    .setContentText(notificationMessageText)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setSound(defaultSoundUri)
                    .setAutoCancel(true)
                    .setPriority(10000)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent);
        }
        android.app.Notification notification = builder.build();
        notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;;

        nManager.notify(getString(R.string.app_name), MESSAGE_NOTIFICATION_ID, notification);
    }

    private void uploadMessageDeliveredStateToServer(final int messageId) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);
        String email = SessionManager.getCurrentUserDetails(getApplicationContext()).getEmail();
        String password = SessionManager.getCurrentUserDetails(getApplicationContext()).getPassword();
        final Call<ResponseBody> uploadMessageState = fcmApi.uploadMessageState(email, password, messageId, Message.State.DELIVERED.ordinal());

        uploadMessageState.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null){
                        if (response.body() != null){
                            String json = response.body().string();

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                if (errorCode == ErrorCodes.EMPTY_POST){
                                    Log.e(TAG, "Post is empty. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.MISSING_POST_ELEMENT){
                                    Log.e(TAG, "Post element missing. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.INVALID_REQUEST){
                                    Log.e(TAG, "Invalid request. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.INVALID_EMAIL){
                                    Log.e(TAG, "Invalid email. (Upload Message State Error)");
                                }else if (errorCode == ErrorCodes.UNKNOWN){
                                    Log.e(TAG, "onResponse: errorCode = " + errorCode);
                                }
                            }
                        }else{
                            Log.e(TAG, "Response body is null. (Upload Message State Error)");
                        }
                    }else {
                        Log.e(TAG, "Response object is null. (Upload Message State Error)");
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Upload Message State onFailure: " + t.getMessage());
                uploadMessageDeliveredStateToServer(messageId);
            }
        });
    }
}
