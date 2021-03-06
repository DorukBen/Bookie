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
    public static final String EXTRA_PROFILE_PICTURE_URL = "profile_picture_url";
    public static final String EXTRA_PROFILE_THUMBNAIL_URL = "profile_thumbnail_url";
    public static final String EXTRA_USER = "user";
    public static final String EXTRA_BOOK_PICTURE_URL = "book_picture_url";
    public static final String EXTRA_BOOK_THUMBNAIL_URL = "book_thumbnail_url";
    public static final String EXTRA_NAME_SURNAME = "name_surname";
    public static final String EXTRA_BIO = "bio";
    public static final String EXTRA_LOCATION = "location";

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
    public static final String INTENT_FILTER_PROFILE_PICTURE_CHANGED = "com.karambit.bookie.PROFILE_PICTURE_CHANGED";
    public static final String INTENT_FILTER_BOOK_PICTURE_CHANGED = "com.karambit.bookie.BOOK_PICTURE_CHANGED";
    public static final String INTENT_FILTER_PROFILE_PREFERENCES_CHANGED = "com.karambit.bookie.PROFILE_PREFERENCES_CHANGED";
    public static final String INTENT_FILTER_BOOK_ADDED = "com.karambit.bookie.BOOK_ADDED";
    public static final String INTENT_FILTER_LOCATION_UPDATED = "com.karambit.bookie.LOCATION_UPDATED";
    public static final String INTENT_FILTER_DATABASE_USER_CHANGED = "com.karambit.bookie.DATABASE_USER_CHANGED";
    public static final String INTENT_FILTER_DATABASE_BOOK_CHANGED = "com.karambit.bookie.DATABASE_BOOK_CHANGED";
}
