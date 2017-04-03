/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.karambit.bookie.ConversationActivity;
import com.karambit.bookie.MainActivity;
import com.karambit.bookie.R;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.helper.SessionManager;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;
import com.karambit.bookie.rest_api.BookieClient;
import com.karambit.bookie.rest_api.FcmApi;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.karambit.bookie.model.Notification.Type.BOOK_LOST;
import static com.karambit.bookie.model.Notification.Type.BOOK_OWNER_CHANGED;
import static com.karambit.bookie.model.Notification.Type.REQUESTED;

public class BookieFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = BookieFirebaseMessagingService.class.getSimpleName();
    public static final int MESSAGE_NOTIFICATION_ID = 785;
    public static ArrayList<Message> mNotificationMessages = new ArrayList<>();
    public static ArrayList<Integer> mNotificationUserIds = new ArrayList<>();

    public static final int NOTIFICATION_ID = 3785;
    public static ArrayList<Notification> mNotifications = new ArrayList<>();

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Logger.d("Message data payload: ");
            Logger.json(remoteMessage.getData().toString());
            if (remoteMessage.getData().containsKey("fcmDataType")) {
                if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_SENT_MESSAGE && SessionManager.isLoggedIn(getApplicationContext())) {
                    if (remoteMessage.getData().containsKey("messageID") && remoteMessage.getData().containsKey("message") && remoteMessage.getData().containsKey("sender")) {
                        try {
                            JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("sender"));
                            User sender = User.jsonObjectToUser(jsonObject);
                            final Message message = new Message(Integer.parseInt(remoteMessage.getData().get("messageID")),
                                    remoteMessage.getData().get("message"),
                                    sender,
                                    SessionManager.getCurrentUser(this),
                                    Calendar.getInstance(),
                                    Message.State.DELIVERED);

                            if (ConversationActivity.currentConversationUserId != message.getSender().getID()) {
                                sendMessageNotification(message);
                            }

                            DBManager dbManager = new DBManager(getApplicationContext());
                            dbManager.open();
                            dbManager.getMessageDataSource().saveMessage(message, message.getOppositeUser(SessionManager.getCurrentUser(this)));

                            Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_RECEIVED);
                            intent.putExtra(BookieIntentFilters.EXTRA_MESSAGE, message);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                            uploadMessageDeliveredStateToServer(message.getID());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_DELIVERED_MESSAGE && SessionManager.isLoggedIn(getApplicationContext())) {
                    if (remoteMessage.getData().containsKey("messageID")) {

                        DBManager dbManager = new DBManager(getApplicationContext());
                        dbManager.open();
                        dbManager.getMessageDataSource().updateMessageState(Integer.parseInt(remoteMessage.getData().get("messageID")), Message.State.DELIVERED);


                        Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_DELIVERED);
                        intent.putExtra(BookieIntentFilters.EXTRA_MESSAGE_ID, Integer.parseInt(remoteMessage.getData().get("messageID")));
                        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_SEEN_MESSAGE && SessionManager.isLoggedIn(getApplicationContext())) {

                    DBManager dbManager = new DBManager(getApplicationContext());
                    dbManager.open();
                    dbManager.getMessageDataSource().updateMessageState(Integer.parseInt(remoteMessage.getData().get("messageID")), Message.State.SEEN);

                    Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_MESSAGE_SEEN);
                    intent.putExtra(BookieIntentFilters.EXTRA_MESSAGE_ID, Integer.parseInt(remoteMessage.getData().get("messageID")));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_REQUEST_SENT && SessionManager.isLoggedIn(getApplicationContext())) {
                    if (remoteMessage.getData().containsKey("book") && remoteMessage.getData().containsKey("sender")) {
                        try {
                            JSONObject userJsonObject = new JSONObject(remoteMessage.getData().get("sender"));
                            User sender = User.jsonObjectToUser(userJsonObject);

                            JSONObject bookJsonObject = new JSONObject(remoteMessage.getData().get("book"));
                            Book book = Book.jsonObjectToBook(bookJsonObject);


                            final Notification notification = new Notification(REQUESTED,
                                    Calendar.getInstance(),
                                    book,
                                    sender,
                                    false);

                            sendNotification(notification);

                            DBManager dbManager = new DBManager(getApplicationContext());
                            dbManager.open();
                            dbManager.getNotificationDataSource().saveNotificationToDatabase(notification);

                            Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED);
                            intent.putExtra(BookieIntentFilters.EXTRA_NOTIFICATION, notification);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_REQUEST_REJECTED && SessionManager.isLoggedIn(getApplicationContext())) {
                    if (remoteMessage.getData().containsKey("book") && remoteMessage.getData().containsKey("sender")) {
                        try {
                            JSONObject userJsonObject = new JSONObject(remoteMessage.getData().get("sender"));
                            User sender = User.jsonObjectToUser(userJsonObject);

                            JSONObject bookJsonObject = new JSONObject(remoteMessage.getData().get("book"));
                            Book book = Book.jsonObjectToBook(bookJsonObject);


                            final Notification notification = new Notification(Notification.Type.REQUEST_REJECTED,
                                    Calendar.getInstance(),
                                    book,
                                    sender,
                                    false);

                            sendNotification(notification);

                            DBManager dbManager = new DBManager(getApplicationContext());
                            dbManager.open();
                            dbManager.getNotificationDataSource().saveNotificationToDatabase(notification);

                            Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED);
                            intent.putExtra(BookieIntentFilters.EXTRA_NOTIFICATION, notification);

                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_REQUEST_ACCEPTED && SessionManager.isLoggedIn(getApplicationContext())) {
                    if (remoteMessage.getData().containsKey("book") && remoteMessage.getData().containsKey("sender")) {
                        try {
                            JSONObject userJsonObject = new JSONObject(remoteMessage.getData().get("sender"));
                            User sender = User.jsonObjectToUser(userJsonObject);

                            JSONObject bookJsonObject = new JSONObject(remoteMessage.getData().get("book"));
                            Book book = Book.jsonObjectToBook(bookJsonObject);


                            final Notification notification = new Notification(Notification.Type.REQUEST_ACCEPTED,
                                    Calendar.getInstance(),
                                    book,
                                    sender,
                                    false);

                            sendNotification(notification);

                            DBManager dbManager = new DBManager(getApplicationContext());
                            dbManager.open();
                            dbManager.getNotificationDataSource().saveNotificationToDatabase(notification);

                            Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED);
                            intent.putExtra(BookieIntentFilters.EXTRA_NOTIFICATION, notification);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_TRANSACTION_COME_TO_HAND && SessionManager.isLoggedIn(getApplicationContext())) {
                    if (remoteMessage.getData().containsKey("book") && remoteMessage.getData().containsKey("sender")) {
                        try {
                            JSONObject userJsonObject = new JSONObject(remoteMessage.getData().get("sender"));
                            User sender = User.jsonObjectToUser(userJsonObject);

                            JSONObject bookJsonObject = new JSONObject(remoteMessage.getData().get("book"));
                            Book book = Book.jsonObjectToBook(bookJsonObject);


                            final Notification notification = new Notification(BOOK_OWNER_CHANGED,
                                    Calendar.getInstance(),
                                    book,
                                    sender,
                                    false);

                            sendNotification(notification);

                            DBManager dbManager = new DBManager(getApplicationContext());
                            dbManager.open();
                            dbManager.getNotificationDataSource().saveNotificationToDatabase(notification);

                            Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED);
                            intent.putExtra(BookieIntentFilters.EXTRA_NOTIFICATION, notification);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_TRANSACTION_BOOK_LOST && SessionManager.isLoggedIn(getApplicationContext())) {
                    if (remoteMessage.getData().containsKey("book") && remoteMessage.getData().containsKey("sender")) {
                        try {
                            JSONObject userJsonObject = new JSONObject(remoteMessage.getData().get("sender"));
                            User sender = User.jsonObjectToUser(userJsonObject);

                            JSONObject bookJsonObject = new JSONObject(remoteMessage.getData().get("book"));
                            Book book = Book.jsonObjectToBook(bookJsonObject);


                            final Notification notification = new Notification(BOOK_LOST,
                                    Calendar.getInstance(),
                                    book,
                                    sender,
                                    false);

                            sendNotification(notification);

                            DBManager dbManager = new DBManager(getApplicationContext());
                            dbManager.open();
                            dbManager.getNotificationDataSource().saveNotificationToDatabase(notification);

                            Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_BOOK_LOST);
                            intent.putExtra(BookieIntentFilters.EXTRA_NOTIFICATION, notification);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (Integer.parseInt(remoteMessage.getData().get("fcmDataType")) == FcmDataTypes.FCM_DATA_TYPE_USER_VERIFIED && SessionManager.isLoggedIn(getApplicationContext())) {
                    Intent intent = new Intent(BookieIntentFilters.FCM_INTENT_FILTER_USER_VERIFIED);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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

    private void sendMessageNotification(Message message) {
        Bitmap bitmap;
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder;

        if (mNotificationUserIds.size() == 0 || !mNotificationUserIds.contains(message.getSender().getID())) {
            mNotificationUserIds.add(message.getSender().getID());
        }
        mNotificationMessages.add(message);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.app_name));
        if (mNotificationUserIds.size() > 1) {
            if (mNotificationMessages.size() > 1) {
                inboxStyle.setSummaryText(getString(R.string.notification_multiple_messages, mNotificationMessages.size(), mNotificationUserIds.size()));
            }

            for (Message notificationMessage : mNotificationMessages) {
                inboxStyle.addLine(notificationMessage.getSender().getName() + ": " + notificationMessage.getText());
            }

            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(MainActivity.EXTRA_MESSAGE_USER, null);
            bundle.putBoolean(MainActivity.EXTRA_IS_SINGLE_USER, false);
            intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1000, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notification_multiple_messages, mNotificationMessages.size(), mNotificationUserIds.size()))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setSound(defaultSoundUri)
                    .setAutoCancel(true)
                    .setStyle(inboxStyle)
                    .setDefaults(android.app.Notification.DEFAULT_ALL)
                    .setPriority(android.app.Notification.PRIORITY_MAX)
                    .setContentIntent(pendingIntent);
        } else {
            inboxStyle.setBigContentTitle(message.getSender().getName());
            String notificationMessageText;
            if (mNotificationMessages.size() > 1) {
                String notificationString = getString(R.string.notification_new_messages, mNotificationMessages.size());
                inboxStyle.setSummaryText(notificationString);
                notificationMessageText = notificationString;
            } else {
                String notificationString = getString(R.string.notification_new_message, mNotificationMessages.size());
                inboxStyle.setSummaryText(notificationString);
                notificationMessageText = notificationString;
            }

            for (Message notificationMessage : mNotificationMessages) {
                inboxStyle.addLine(notificationMessage.getText());
            }

            Intent intent = new Intent(this, MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(MainActivity.EXTRA_MESSAGE_USER, message.getSender());
            bundle.putBoolean(MainActivity.EXTRA_IS_SINGLE_USER, true);
            intent.putExtras(bundle);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1000, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            try {
                URL url = new URL(message.getSender().getThumbnailUrl());
                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            }

            builder = new NotificationCompat.Builder(this)
                    .setContentTitle(message.getSender().getName())
                    .setContentText(notificationMessageText)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(bitmap)
                    .setSound(defaultSoundUri)
                    .setAutoCancel(true)
                    .setPriority(android.app.Notification.PRIORITY_MAX)
                    .setDefaults(android.app.Notification.DEFAULT_ALL)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent);
        }
        android.app.Notification notification = builder.build();
        notification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
        ;

        nManager.notify(getString(R.string.message_notification), MESSAGE_NOTIFICATION_ID, notification);
    }

    private void sendNotification(Notification notification) {
        mNotifications.add(notification);

        Bitmap bitmap;
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder;

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.app_name));

        String summaryText;
        if (mNotifications.size() > 1) {
            String notificationString = getString(R.string.notification_new_notifications, mNotifications.size());
            inboxStyle.setSummaryText(notificationString);
            summaryText = notificationString;
        } else {
            String notificationString = getString(R.string.notification_new_notification, mNotifications.size());
            inboxStyle.setSummaryText(notificationString);
            switch (notification.getType()) {
                case REQUESTED:
                    summaryText = getString(R.string.x_requested_for_y, notification.getOppositeUser().getName(), notification.getBook().getName());
                    break;

                case BOOK_OWNER_CHANGED:
                    summaryText = getString(R.string.x_now_owned_by_y, notification.getBook().getName(), notification.getOppositeUser().getName());
                    break;

                case BOOK_LOST:
                    summaryText = getString(R.string.x_lost_y, notification.getOppositeUser().getName(), notification.getBook().getName());
                    break;

                case REQUEST_ACCEPTED:
                    summaryText = getString(R.string.x_accepted_your_request_for_y, notification.getOppositeUser().getName(), notification.getBook().getName());
                    break;

                case REQUEST_REJECTED:
                    summaryText = getString(R.string.x_rejected_your_request_for_y, notification.getOppositeUser().getName(), notification.getBook().getName());
                    break;

                default:
                    throw new IllegalArgumentException("Invalid notification type");
            }
        }

        for (Notification tmpNotification : mNotifications) {
            switch (tmpNotification.getType()) {
                case REQUESTED:
                    String requestedString = getString(R.string.x_requested_for_y, tmpNotification.getOppositeUser().getName(), tmpNotification.getBook().getName());
                    inboxStyle.addLine(requestedString);
                    break;

                case BOOK_OWNER_CHANGED:
                    String bookOwnerChangedString = getString(R.string.x_now_owned_by_y, tmpNotification.getBook().getName(), tmpNotification.getOppositeUser().getName());
                    inboxStyle.addLine(bookOwnerChangedString);
                    break;

                case BOOK_LOST:
                    String bookLostString = getString(R.string.x_lost_y, tmpNotification.getOppositeUser().getName(), tmpNotification.getBook().getName());
                    inboxStyle.addLine(bookLostString);
                    break;

                case REQUEST_ACCEPTED:
                    String requestAcceptedString = getString(R.string.x_accepted_your_request_for_y, tmpNotification.getOppositeUser().getName(), tmpNotification.getBook().getName());
                    inboxStyle.addLine(requestAcceptedString);
                    break;

                case REQUEST_REJECTED:
                    String requestRejectedString = getString(R.string.x_rejected_your_request_for_y, tmpNotification.getOppositeUser().getName(), tmpNotification.getBook().getName());
                    inboxStyle.addLine(requestRejectedString);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid notification type");
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MainActivity.EXTRA_NOTIFICATION, notification);
        intent.putExtras(bundle);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 500 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        builder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(summaryText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setStyle(inboxStyle)
                .setPriority(android.app.Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        android.app.Notification appNotification = builder.build();
        appNotification.flags |= android.app.Notification.FLAG_AUTO_CANCEL;
        ;

        nManager.notify(getString(R.string.notification), NOTIFICATION_ID, appNotification);
    }

    private void uploadMessageDeliveredStateToServer(final int messageId) {
        final FcmApi fcmApi = BookieClient.getClient().create(FcmApi.class);

        User.Details currentUserDetails = SessionManager.getCurrentUserDetails(this);

        String email = currentUserDetails.getEmail();
        String password = currentUserDetails.getPassword();
        final Call<ResponseBody> uploadMessageState = fcmApi.uploadMessageState(email, password, messageId, Message.State.DELIVERED.getStateCode());

        Logger.d("uploadMessageState() API called with parameters: \n" +
                     "\temail=" + email + ", \n\tpassword=" + password +
                     ", \n\tmessageID=" + messageId +
                     ", \n\tstate=" + Message.State.DELIVERED.getStateCode());

        uploadMessageState.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {
                    if (response != null) {
                        if (response.body() != null) {
                            String json = response.body().string();

                            Logger.json(json);

                            JSONObject responseObject = new JSONObject(json);
                            boolean error = responseObject.getBoolean("error");

                            if (!error) {

                                Logger.d("Message state uploaded successfully");

                            } else {
                                int errorCode = responseObject.getInt("errorCode");

                                Logger.e("Error true in response: errorCode = " + errorCode);
                            }
                        } else {
                            Logger.e("Response body is null. (Upload Message State Error)");
                        }
                    } else {
                        Logger.e("Response object is null. (Upload Message State Error)");
                    }
                } catch (IOException | JSONException e) {
                    Logger.e("IOException or JSONException caught: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.e("Upload Message State onFailure: " + t.getMessage());
                uploadMessageDeliveredStateToServer(messageId);
            }
        });
    }
}
