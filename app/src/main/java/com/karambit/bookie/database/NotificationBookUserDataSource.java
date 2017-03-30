package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by doruk on 19.03.2017.
 */

public class NotificationBookUserDataSource {

    private static final String TAG = NotificationBookUserDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;

    private static final String NOTIFICATION_BOOK_USER_TABLE_NAME = "notification_book_user";
    private static final String NOTIFICATION_BOOK_USER_COLUMN_ID = "user_id";
    private static final String NOTIFICATION_BOOK_USER_COLUMN_NAME = "name";
    private static final String NOTIFICATION_BOOK_USER_COLUMN_IMAGE_URL = "image_url";
    private static final String NOTIFICATION_BOOK_USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String NOTIFICATION_BOOK_USER_COLUMN_LATITUDE = "latitude";
    private static final String NOTIFICATION_BOOK_USER_COLUMN_LONGITUDE = "longitude";

    public static final String CREATE_NOTIFICATION_BOOK_USER_TABLE_TAG = "CREATE TABLE " + NOTIFICATION_BOOK_USER_TABLE_NAME + " (" +
            NOTIFICATION_BOOK_USER_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            NOTIFICATION_BOOK_USER_COLUMN_NAME + " TEXT, " +
            NOTIFICATION_BOOK_USER_COLUMN_IMAGE_URL + " TEXT, " +
            NOTIFICATION_BOOK_USER_COLUMN_THUMBNAIL_URL + " TEXT, " +
            NOTIFICATION_BOOK_USER_COLUMN_LATITUDE + " DOUBLE, " +
            NOTIFICATION_BOOK_USER_COLUMN_LONGITUDE + " DOUBLE)";

    public static final String UPGRADE_NOTIFICATION_BOOK_USER_TABLE_TAG = "DROP TABLE IF EXISTS " + NOTIFICATION_BOOK_USER_TABLE_NAME;

    public NotificationBookUserDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
    }

    /**
     * Insert user to database.<br>
     *
     * @param user {@link User} which will be inserted
     * @return Returns boolean value if insertion successful returns true else returns false
     */
    public boolean insertUser(User user) {
        boolean result = false;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_BOOK_USER_COLUMN_ID, user.getID());
            contentValues.put(NOTIFICATION_BOOK_USER_COLUMN_NAME, user.getName());
            contentValues.put(NOTIFICATION_BOOK_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(NOTIFICATION_BOOK_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(NOTIFICATION_BOOK_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(NOTIFICATION_BOOK_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = mSqLiteDatabase.insert(NOTIFICATION_BOOK_USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            Logger.d("New User insertion successful");
        }
        return result;
    }

    /**
     * Checks database for given user's existence. Use before all user insertions.<br>
     *
     * @param user {@link User User}
     *
     * @return  boolean value. If message {@link User user} exist returns true else returns false.
     */
    public boolean isUserExists(User user) {
        Cursor res = null;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + NOTIFICATION_BOOK_USER_TABLE_NAME + " WHERE " + NOTIFICATION_BOOK_USER_COLUMN_ID + " = " + user.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
        }
    }

    /**
     * Get all {@link User users} from database.<br>
     *
     * @return All {@link User User's}
     */
    public ArrayList<User> getAllUsers() {
        Cursor res = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + NOTIFICATION_BOOK_USER_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    User user;
                    if (res.isNull(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_LATITUDE)) || res.isNull(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_LONGITUDE))){
                        user = new User(res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_THUMBNAIL_URL)),
                                null);
                    }else {
                        user = new User(res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_THUMBNAIL_URL)),
                                new LatLng(res.getDouble(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_LATITUDE)), res.getDouble(res.getColumnIndex(NOTIFICATION_BOOK_USER_COLUMN_LONGITUDE)))
                        );
                    }


                    users.add(user);
                } while (res.moveToNext());
            }
        }finally {
            if (res != null) {
                res.close();
            }
        }
        return users;
    }

    /**
     * Deletes all {@link User users} from database.<br>
     */
    public void deleteAllUsers() {
        try{
            mSqLiteDatabase.delete(NOTIFICATION_BOOK_USER_TABLE_NAME, null, null);
        }finally {
            Logger.d("All users deleted from database");
        }
    }
}
