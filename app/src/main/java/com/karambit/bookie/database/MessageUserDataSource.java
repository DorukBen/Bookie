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

public class MessageUserDataSource {

    private static final String TAG = MessageUserDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;

    private static final String MESSAGE_USER_TABLE_NAME = "message_user";
    private static final String MESSAGE_USER_COLUMN_ID = "user_id";
    private static final String MESSAGE_USER_COLUMN_NAME = "name";
    private static final String MESSAGE_USER_COLUMN_IMAGE_URL = "image_url";
    private static final String MESSAGE_USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String MESSAGE_USER_COLUMN_LATITUDE = "latitude";
    private static final String MESSAGE_USER_COLUMN_LONGITUDE = "longitude";

    static final String CREATE_MESSAGE_USER_TABLE_TAG = "CREATE TABLE " + MESSAGE_USER_TABLE_NAME + " (" +
            MESSAGE_USER_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            MESSAGE_USER_COLUMN_NAME + " TEXT NOT NULL, " +
            MESSAGE_USER_COLUMN_IMAGE_URL + " TEXT, " +
            MESSAGE_USER_COLUMN_THUMBNAIL_URL + " TEXT, " +
            MESSAGE_USER_COLUMN_LATITUDE + " DOUBLE, " +
            MESSAGE_USER_COLUMN_LONGITUDE + " DOUBLE)";

    static final String UPGRADE_MESSAGE_USER_TABLE_TAG = "DROP TABLE IF EXISTS " + MESSAGE_USER_TABLE_NAME;

    MessageUserDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
    }

    /**
     * Inserts user to database.<br>
     *
     * @param user {@link User User}<br>
     *
     * @return boolean value. If insertion successful returns true else returns false.
     */
    private boolean insertUser(User user) {
        boolean result = false;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(MESSAGE_USER_COLUMN_ID, user.getID());
            contentValues.put(MESSAGE_USER_COLUMN_NAME, user.getName());
            contentValues.put(MESSAGE_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(MESSAGE_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(MESSAGE_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(MESSAGE_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = mSqLiteDatabase.insert(MESSAGE_USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            Logger.d("New User insertion successful");
        }
        return result;
    }

    public boolean saveUser(User user) {
        if (!isUserExists(user.getID())) {
            return insertUser(user);
        } else {
            return false;
        }
    }

    /**
     * Updates given user in database.<br>
     *
     * @param user {@link User User}<br>
     *
     * @return boolean value. If update successful returns true else returns false.
     */
    private boolean updateUser(User user) {
        boolean result = false;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MESSAGE_USER_COLUMN_ID, user.getID());
            contentValues.put(MESSAGE_USER_COLUMN_NAME, user.getName());
            contentValues.put(MESSAGE_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(MESSAGE_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(MESSAGE_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(MESSAGE_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = mSqLiteDatabase.update(MESSAGE_USER_TABLE_NAME, contentValues, MESSAGE_USER_COLUMN_ID + "=" + user.getID(), null) > 0;
        }finally {

            if (result){
                Logger.d("Message user update successful.");
            }else{
                Logger.e("Error occurred during user update!");
            }
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

        if (isUserExists(user.getID())) {
            return updateUser(user);
        } else {
            return false;
        }
    }

    /**
     * Gives all {@link User users} whose have conversation with current user.<br>
     *
     * @return {@link ArrayList Arraylist}<{@link User User}> all users
     */
    public ArrayList<User> getAllUsers() {
        Cursor res = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + MESSAGE_USER_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    User user;
                    if (res.isNull(res.getColumnIndex(MESSAGE_USER_COLUMN_LATITUDE)) || res.isNull(res.getColumnIndex(MESSAGE_USER_COLUMN_LONGITUDE))){
                        user = new User(res.getInt(res.getColumnIndex(MESSAGE_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_THUMBNAIL_URL)),
                                null);
                    }else{
                        user = new User(res.getInt(res.getColumnIndex(MESSAGE_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_THUMBNAIL_URL)),
                                new LatLng(res.getDouble(res.getColumnIndex(MESSAGE_USER_COLUMN_LATITUDE)), res.getDouble(res.getColumnIndex(MESSAGE_USER_COLUMN_LONGITUDE))));
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
     * Checks database for given user id's existence.<br>
     *
     * @param userID {@link User User} id
     *
     * @return  boolean value. If message {@link User user} exist returns true else returns false.
     */
    public boolean isUserExists(int userID) {
        Cursor res = null;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + MESSAGE_USER_TABLE_NAME + " WHERE " + MESSAGE_USER_COLUMN_ID  + " = " + userID, null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
        }
    }

    /**
     * Checks database for given user id's existence.<br>
     *
     * @param user {@link User User}
     *
     * @return  boolean value. If message {@link User user} exist returns true else returns false.
     */
    public boolean isUserExists(User user) {
        Cursor res = null;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + MESSAGE_USER_TABLE_NAME + " WHERE " + MESSAGE_USER_COLUMN_ID  + " = " + user.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
        }
    }

    /**
     * Deletes {@link User user} from database.<br>
     *
     * @param userID {@link User User's} id, ({@link User#getID()})
     */
    public boolean deleteUser(Integer userID) {
        boolean result = false;
        try{
            result = mSqLiteDatabase.delete(MESSAGE_USER_TABLE_NAME, MESSAGE_USER_COLUMN_ID + " = ?", new String[] { userID.toString() }) > 0;
        }finally {
            Logger.d("User deleted from database");
        }

        return result;
    }

    /**
     * Deletes {@link User user} from database.<br>
     *
     * @param user {@link User User}
     */
    public boolean deleteUser(User user) {
        boolean result = false;
        try{
            result = mSqLiteDatabase.delete(MESSAGE_USER_TABLE_NAME, MESSAGE_USER_COLUMN_ID + " = ?", new String[] { ((Integer)user.getID()).toString() }) > 0;
        }finally {
            Logger.d("User deleted from database");
        }

        return result;
    }

    /**
     * Deletes all {@link User users} from database.<br>
     */
    public boolean deleteAllUsers() {
        boolean result = false;
        try{
            result = mSqLiteDatabase.delete(MESSAGE_USER_TABLE_NAME, null, null) > 0;
        }finally {
            Logger.d("All users deleted from database");
        }

        return result;
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
