package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by doruk on 19.03.2017.
 */

public class SearchBookUserDataSource {

    private static final String TAG = SearchBookUserDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;

    private static final String SEARCH_BOOK_USER_TABLE_NAME = "search_book_user";
    private static final String SEARCH_BOOK_USER_COLUMN_ID = "user_id";
    private static final String SEARCH_BOOK_USER_COLUMN_NAME = "name";
    private static final String SEARCH_BOOK_USER_COLUMN_IMAGE_URL = "image_url";
    private static final String SEARCH_BOOK_USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String SEARCH_BOOK_USER_COLUMN_LATITUDE = "latitude";
    private static final String SEARCH_BOOK_USER_COLUMN_LONGITUDE = "longitude";

    static final String CREATE_SEARCH_BOOK_USER_TABLE_TAG =  "CREATE TABLE " + SEARCH_BOOK_USER_TABLE_NAME + " (" +
            SEARCH_BOOK_USER_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            SEARCH_BOOK_USER_COLUMN_NAME + " TEXT NOT NULL, " +
            SEARCH_BOOK_USER_COLUMN_IMAGE_URL + " TEXT, " +
            SEARCH_BOOK_USER_COLUMN_THUMBNAIL_URL + " TEXT, " +
            SEARCH_BOOK_USER_COLUMN_LATITUDE + " DOUBLE, " +
            SEARCH_BOOK_USER_COLUMN_LONGITUDE + " DOUBLE)";

    static final String UPGRADE_SEARCH_BOOK_USER_TABLE_TAG = "DROP TABLE IF EXISTS " + SEARCH_BOOK_USER_TABLE_NAME;

    SearchBookUserDataSource(SQLiteDatabase database) {
        this.mSqLiteDatabase = database;
    }

    /**
     * Insert user to database.<br>
     *
     * @param user {@link User} which will be inserted
     * @return Returns boolean value if insertion successful returns true else returns false
     */
    private boolean insertUser(User user) {
        boolean result = false;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(SEARCH_BOOK_USER_COLUMN_ID, user.getID());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_NAME, user.getName());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(SEARCH_BOOK_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = mSqLiteDatabase.insert(SEARCH_BOOK_USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            Logger.d("New User insertion successful");
        }
        return result;
    }

    public boolean saveUser(User user) {
        if (!isUserExists(user)) {
            return insertUser(user);
        } else {
            return false;
        }
    }

    /**
     * Updates user in database.<br>
     *
     * @param user {@link User} which will be inserted
     * @return Returns boolean value if update successful returns true else returns false
     */
    private boolean updateUser(User user) {
        boolean result = false;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(SEARCH_BOOK_USER_COLUMN_ID, user.getID());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_NAME, user.getName());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(SEARCH_BOOK_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(SEARCH_BOOK_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = mSqLiteDatabase.update(SEARCH_BOOK_USER_TABLE_NAME, contentValues, SEARCH_BOOK_USER_COLUMN_ID + "=" + user.getID(), null) > 0;
        }finally {
            Logger.d("Search Book User update successful");
        }
        return result;
    }

    /**
     * Checks the user exists. Updates given user in database if its not exist.<br>
     *
     * @param user {@link User User}<br>
     *
     * @return boolean value. If insertion successful returns true else returns false.
     */
    public boolean checkAndUpdateUser(User user) {
        if (isUserExists(user)) {
            return updateUser(user);
        } else {
            return false;
        }
    }

    /**
     * Checks database for given user's existence. Use before all user insertions.<br>
     *
     * @param user {@link User User}
     *
     * @return  boolean value. If {@link User user} exist returns true else returns false.
     */
    public boolean isUserExists(User user) {
        Cursor res = null;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + SEARCH_BOOK_USER_TABLE_NAME + " WHERE " + SEARCH_BOOK_USER_COLUMN_ID + " = " + user.getID(), null);
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
     * @return All {@link User users}
     */
    public ArrayList<User> getAllUsers() {
        Cursor res = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + SEARCH_BOOK_USER_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    User user;
                    if (res.isNull(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_LATITUDE)) || res.isNull(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_LONGITUDE))){
                        user = new User(res.getInt(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_THUMBNAIL_URL)),
                                null);
                    }else {
                        user = new User(res.getInt(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_THUMBNAIL_URL)),
                                new LatLng(res.getDouble(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_LATITUDE)), res.getDouble(res.getColumnIndex(SEARCH_BOOK_USER_COLUMN_LONGITUDE)))
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
        int result;
        try{
            mSqLiteDatabase.delete(SEARCH_BOOK_USER_TABLE_NAME, null, null);
        }finally {
            Logger.d("Users deleted from database");
        }
    }

    //Callable Methods
    public Callable<Boolean> cCheckAndUpdateUser(final User user){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return checkAndUpdateUser(user);
            }
        };
    }
}
