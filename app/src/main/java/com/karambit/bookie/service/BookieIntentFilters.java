package com.karambit.bookie.service;

/**
 * Created by doruk on 12.03.2017.
 */

public class BookieIntentFilters {

    public static final String EXTRA_BOOK = "book";
    public static final String EXTRA_NOTIFICATION = "notification";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_MESSAGE_ID = "message_id";
    public static final String EXTRA_REQUEST = "request";

    public static final String FCM_INTENT_FILTER_MESSAGE_RECEIVED = "com.karambit.bookie.FCM_MESSAGE_RECEIVED";
    public static final String FCM_INTENT_FILTER_MESSAGE_DELIVERED = "com.karambit.bookie.FCM_MESSAGE_DELIVERED";
    public static final String FCM_INTENT_FILTER_MESSAGE_SEEN = "com.karambit.bookie.FCM_MESSAGE_SEEN";
    public static final String FCM_INTENT_FILTER_SENT_REQUEST_RECEIVED = "com.karambit.bookie.FCM_SENT_REQUEST_RECEIVED";
    public static final String FCM_INTENT_FILTER_REJECTED_REQUEST_RECEIVED = "com.karambit.bookie.FCM_REJECTED_REQUEST_RECEIVED";
    public static final String FCM_INTENT_FILTER_ACCEPTED_REQUEST_RECEIVED = "com.karambit.bookie.FCM_ACCEPTED_REQUEST_RECEIVED";
    public static final String FCM_INTENT_FILTER_BOOK_OWNER_CHANGED_RECEIVED = "com.karambit.bookie.FCM_BOOK_OWNER_CHANGED_DATA_RECEIVED";
    public static final String FCM_INTENT_FILTER_BOOK_LOST = "com.karambit.bookie.FCM_BOOK_LOST";
    public static final String FCM_INTENT_FILTER_USER_VERIFIED = "com.karambit.bookie.FCM_USER_VERIFIED";

    public static final String INTENT_FILTER_BOOK_STATE_CHANGED = "com.karambit.bookie.BOOK_STATE_CHANGED";
    public static final String INTENT_FILTER_ACCEPTED_REQUEST = "com.karambit.bookie.ACCEPTED_REQUEST";
    public static final String INTENT_FILTER_REJECTED_REQUEST = "com.karambit.bookie.REJECTED_REQUEST";
    public static final String INTENT_FILTER_BOOK_LOST = "com.karambit.bookie.BOOK_LOST";
    public static final String INTENT_FILTER_BOOK_UPDATED = "com.karambit.bookie.BOOK_UPDATED";

}
