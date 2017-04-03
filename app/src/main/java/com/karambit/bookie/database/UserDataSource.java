package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Created by doruk on 19.03.2017.
 */

public class UserDataSource{

    private static final String TAG = UserDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;

    private static final String USER_TABLE_NAME = "user";
    private static final String USER_COLUMN_ID = "id";
    private static final String USER_COLUMN_NAME = "name";
    private static final String USER_COLUMN_IMAGE_URL = "image_url";
    private static final String USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String USER_COLUMN_LATITUDE = "latitude";
    private static final String USER_COLUMN_LONGITUDE = "longitude";
    private static final String USER_COLUMN_PASSWORD = "password";
    private static final String USER_COLUMN_EMAIL = "email";
    private static final String USER_COLUMN_VERIFIED = "verified";
    private static final String USER_COLUMN_BIO = "bio";
    private static final String USER_COLUMN_BOOK_COUNTER = "book_counter";
    private static final String USER_COLUMN_POINT = "point";
    private static final String USER_COLUMN_SHARED_POINT = "shared_point";

    static final String CREATE_USER_TABLE_TAG = "CREATE TABLE " + USER_TABLE_NAME + " (" +
            USER_COLUMN_ID + " INTEGER PRIMERY KEY, " +
            USER_COLUMN_NAME + " TEXT NOT NULL, " +
            USER_COLUMN_IMAGE_URL + " TEXT, " +
            USER_COLUMN_THUMBNAIL_URL + " TEXT, " +
            USER_COLUMN_LATITUDE + " DOUBLE, " +
            USER_COLUMN_LONGITUDE + " DOUBLE, " +
            USER_COLUMN_PASSWORD + " TEXT NOT NULL, " +
            USER_COLUMN_EMAIL + " TEXT NOT NULL, " +
            USER_COLUMN_VERIFIED + " BIT NOT NULL, " +
            USER_COLUMN_BIO + " TEXT, " +
            USER_COLUMN_BOOK_COUNTER + " INTEGER NOT NULL, " +
            USER_COLUMN_POINT + " INTEGER NOT NULL, " +
            USER_COLUMN_SHARED_POINT + " INTEGER NOT NULL)";

    static final String UPGRADE_USER_TABLE_TAG = "DROP TABLE IF EXISTS " + USER_TABLE_NAME;

    UserDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
    }

    /**
     * Insert user to database.<br>
     *
     * @param id User id. Can access {@link User#getID() User.getID()}<br>
     * @param name  User name and surname. Can access {@link User#getName() User.getName()}<br>
     * @param imageURL User profile image url. Can access {@link User#getImageUrl() User.getImageUrl()}<br>
     * @param thumbnailURL User profile image thumbnail. Can access {@link User#getThumbnailUrl() User.getThumbnailUrl()}<br>
     * @param latitude User's location latitude. Can access {@link User#getLocation()} () User.getLatitude()}<br>
     * @param longitude User's location longitude. Can access {@link User#getLocation()} () User.getLongitude()}<br>
     * @param password User password in md5 format. Can access {@link User.Details#getPassword() User.Details.getPassword()}<br>
     * @param email User email. Can access {@link User.Details#getEmail() User.Details.getEmail()}<br>
     * @param verified Defines user verification state. Can access {@link User.Details#isVerified() User.Details.isVerified()}<br>
     * @param bio User bio. Can access {@link User.Details#getBio() User.Details.getBio()}<br>
     * @param bookCounter Defines delta between user's shared books and took books. Can access {@link User.Details#getBookCounter() User.Details.getBookCounter()}<br>
     * @param point User's point. Can access {@link User.Details#getPoint() User.Details.getPoint()}<br>
     *
     * @return {@link Boolean Boolean} result from executed database query. If query successful returns true else returns false.
     */
    public boolean saveUser(int id, String name, String imageURL, String thumbnailURL, double latitude, double longitude,
                                     String password, String email, boolean verified, String bio, int bookCounter, int point, int sharedPoint) {
        boolean result = false;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_COLUMN_ID, id);
            contentValues.put(USER_COLUMN_NAME, name);
            contentValues.put(USER_COLUMN_IMAGE_URL, imageURL);
            contentValues.put(USER_COLUMN_THUMBNAIL_URL, thumbnailURL);
            contentValues.put(USER_COLUMN_LATITUDE, latitude);
            contentValues.put(USER_COLUMN_LONGITUDE, longitude);
            contentValues.put(USER_COLUMN_PASSWORD, password);
            contentValues.put(USER_COLUMN_EMAIL, email);
            contentValues.put(USER_COLUMN_VERIFIED, verified);
            contentValues.put(USER_COLUMN_BIO, bio);
            contentValues.put(USER_COLUMN_BOOK_COUNTER, bookCounter);
            contentValues.put(USER_COLUMN_POINT, point);
            contentValues.put(USER_COLUMN_SHARED_POINT, sharedPoint);

            result = mSqLiteDatabase.insert(USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (result){
                Log.i(TAG,"User insertion successful.");
            }else{
                Log.e(TAG,"Error occurred during user insertion!");
            }
        }
        return result;
    }
    /**
     * Insert {@link User user} to database.<br>
     *
     * @param user {@link com.karambit.bookie.model.User.Details User.Details} which will be insert.<br>
     *
     * @return {@link Boolean Boolean} result from executed database query. If query successful returns true else returns false.
     */
    public boolean saveUser(User.Details user) {
        boolean result = false;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_COLUMN_ID, user.getUser().getID());
            contentValues.put(USER_COLUMN_NAME, user.getUser().getName());
            contentValues.put(USER_COLUMN_IMAGE_URL, user.getUser().getImageUrl());
            contentValues.put(USER_COLUMN_THUMBNAIL_URL, user.getUser().getThumbnailUrl());
            contentValues.put(USER_COLUMN_LATITUDE, (user.getUser().getLocation() != null) ? user.getUser().getLocation().latitude : null);
            contentValues.put(USER_COLUMN_LONGITUDE, (user.getUser().getLocation() != null) ? user.getUser().getLocation().longitude : null);
            contentValues.put(USER_COLUMN_PASSWORD, user.getPassword());
            contentValues.put(USER_COLUMN_EMAIL, user.getEmail());
            contentValues.put(USER_COLUMN_VERIFIED, user.isVerified());
            contentValues.put(USER_COLUMN_BIO, user.getBio());
            contentValues.put(USER_COLUMN_BOOK_COUNTER, user.getBookCounter());
            contentValues.put(USER_COLUMN_POINT, user.getPoint());
            contentValues.put(USER_COLUMN_SHARED_POINT, user.getSharedPoint());

            result = mSqLiteDatabase.insert(USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (result){
                Log.i(TAG,"User insertion successful.");
            }else{
                Log.e(TAG,"Error occurred during user insertion!");
            }
        }
        return result;
    }

    /**
     * Returns {@link User user} from database.<br>
     *
     * @return {@link User User}.
     */
    public User getUser() {
        User user;
        Cursor res = null;
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);
            res.moveToFirst();
            if (res.getCount() > 0) {
                if (res.isNull(res.getColumnIndex(USER_COLUMN_LATITUDE)) || res.isNull(res.getColumnIndex(USER_COLUMN_LONGITUDE))) {
                    user = new User(res.getInt(res.getColumnIndex(USER_COLUMN_ID)),
                            res.getString(res.getColumnIndex(USER_COLUMN_NAME)),
                            res.getString(res.getColumnIndex(USER_COLUMN_IMAGE_URL)),
                            res.getString(res.getColumnIndex(USER_COLUMN_THUMBNAIL_URL)),
                            null);
                } else {
                    user = new User(res.getInt(res.getColumnIndex(USER_COLUMN_ID)),
                            res.getString(res.getColumnIndex(USER_COLUMN_NAME)),
                            res.getString(res.getColumnIndex(USER_COLUMN_IMAGE_URL)),
                            res.getString(res.getColumnIndex(USER_COLUMN_THUMBNAIL_URL)),
                            new LatLng(res.getDouble(res.getColumnIndex(USER_COLUMN_LATITUDE)), res.getDouble(res.getColumnIndex(USER_COLUMN_LONGITUDE))));
                }
            } else {
                Log.e(TAG, "Can't find user!");
                return null;
            }
        } finally {
            if (res != null) {
                res.close();
            }
        }
        Logger.d("User: " + user.toString());
        return user;
    }

    /**
     * Gets {@link com.karambit.bookie.model.User.Details User.Details} from database.<br>
     *
     * @return {@link com.karambit.bookie.model.User.Details User.Details}
     */
    public User.Details getUserDetails() {
        Cursor res = null;
        User.Details details;
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);
            res.moveToFirst();
            if (res.getCount() > 0){
                User user;

                if (res.isNull(res.getColumnIndex(USER_COLUMN_LATITUDE)) || res.isNull(res.getColumnIndex(USER_COLUMN_LONGITUDE))){
                    user = new User(res.getInt(res.getColumnIndex(USER_COLUMN_ID)),
                            res.getString(res.getColumnIndex(USER_COLUMN_NAME)),
                            res.getString(res.getColumnIndex(USER_COLUMN_IMAGE_URL)),
                            res.getString(res.getColumnIndex(USER_COLUMN_THUMBNAIL_URL)),
                            null);
                }else {
                    user = new User(res.getInt(res.getColumnIndex(USER_COLUMN_ID)),
                            res.getString(res.getColumnIndex(USER_COLUMN_NAME)),
                            res.getString(res.getColumnIndex(USER_COLUMN_IMAGE_URL)),
                            res.getString(res.getColumnIndex(USER_COLUMN_THUMBNAIL_URL)),
                            new LatLng(res.getDouble(res.getColumnIndex(USER_COLUMN_LATITUDE)), res.getDouble(res.getColumnIndex(USER_COLUMN_LONGITUDE))));
                }

                details = user.new Details(res.getString(res.getColumnIndex(USER_COLUMN_PASSWORD)),
                        res.getString(res.getColumnIndex(USER_COLUMN_EMAIL)),
                        res.getInt(res.getColumnIndex(USER_COLUMN_VERIFIED)) > 0,
                        res.getString(res.getColumnIndex(USER_COLUMN_BIO)),
                        res.getInt(res.getColumnIndex(USER_COLUMN_BOOK_COUNTER)),
                        res.getInt(res.getColumnIndex(USER_COLUMN_POINT)),
                        res.getInt(res.getColumnIndex(USER_COLUMN_SHARED_POINT)));
            }else {
                Log.e(TAG, "Can't find user!");
                return null;
            }
        }finally {
            if (res != null){
                res.close();
            }
        }
        Logger.d("User.Details: " + details.toString());
        return details;
    }

    /**
     * Update {@link com.karambit.bookie.model.User.Details User.Details} to database.<br>
     *
     * @param userDetails User.Details which will be insert.<br>
     *
     * @return {@link Boolean Boolean} result from executed database query. If query successful returns true else returns false.
     */
    public boolean updateUserDetails(User.Details userDetails) {
        boolean result = false;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_COLUMN_ID, userDetails.getUser().getID());
            contentValues.put(USER_COLUMN_NAME, userDetails.getUser().getName());
            contentValues.put(USER_COLUMN_IMAGE_URL, userDetails.getUser().getImageUrl());
            contentValues.put(USER_COLUMN_THUMBNAIL_URL, userDetails.getUser().getThumbnailUrl());
            contentValues.put(USER_COLUMN_LATITUDE, (userDetails.getUser().getLocation() != null) ? userDetails.getUser().getLocation().latitude : null);
            contentValues.put(USER_COLUMN_LONGITUDE, (userDetails.getUser().getLocation() != null) ? userDetails.getUser().getLocation().longitude : null);
            contentValues.put(USER_COLUMN_PASSWORD, userDetails.getPassword());
            contentValues.put(USER_COLUMN_EMAIL, userDetails.getEmail());
            contentValues.put(USER_COLUMN_VERIFIED, userDetails.isVerified());
            contentValues.put(USER_COLUMN_BIO, userDetails.getBio());
            contentValues.put(USER_COLUMN_BOOK_COUNTER, userDetails.getBookCounter());
            contentValues.put(USER_COLUMN_POINT, userDetails.getPoint());

            result = mSqLiteDatabase.update(USER_TABLE_NAME, contentValues, null, null) > 0;
        }finally {

            if (result){
                Log.i(TAG,"User update successful.");
            }else{
                Log.e(TAG,"Error occurred during user update!");
            }
        }
        return result;
    }

    /**
     * Updates {@link User user} location.<br>
     *
     * @param latitude {@link User user's} latitude value.<br>
     * @param longitude {@link User user's} longitude value.<br>
     */
    public boolean updateUserLocation(double latitude, double longitude){
        boolean result = false;
        try{
            ContentValues cv = new ContentValues();
            cv.put(USER_COLUMN_LATITUDE, latitude);
            cv.put(USER_COLUMN_LONGITUDE, longitude);

            result = mSqLiteDatabase.update(USER_TABLE_NAME, cv, null, null) > 0;
        }finally {
            Logger.d("User's location updated: lat = " + latitude + " long = " + longitude);
        }
        return result;
    }

    /**
     * Updates {@link User user} profile picture and its thumbnail.<br>
     *
     * @param imageUrl {@link User user's} image url value.<br>
     * @param thumbnailUrl {@link User user's} thumbnail url value.<br>
     */
    public boolean updateUserImage(String imageUrl, String thumbnailUrl){
        boolean result = false;
        try{
            ContentValues cv = new ContentValues();
            cv.put(USER_COLUMN_IMAGE_URL, imageUrl);
            cv.put(USER_COLUMN_THUMBNAIL_URL, thumbnailUrl);

            result = mSqLiteDatabase.update(USER_TABLE_NAME, cv, null, null) > 0;
        }finally {
            Logger.d("User's location updated: imageUrl = " + imageUrl + " thumbnailUrl = " + thumbnailUrl);
        }

        return result;
    }

    /**
     * Updates {@link User user} password.<br>
     *
     * @param password {@link User user's} password string.<br>
     */
    public boolean updateUserPassword(String password){
        boolean result = false;
        try{
            ContentValues cv = new ContentValues();
            cv.put(USER_COLUMN_PASSWORD, password);

            result = mSqLiteDatabase.update(USER_TABLE_NAME, cv, null, null) > 0;
        }finally {
            Logger.d("User's password updated");
        }

        return result;
    }

    /**
     * Deletes {@link User user} from database.<br>
     */
    public boolean deleteUser() {
        boolean result = false;
        try{
            result = mSqLiteDatabase.delete(USER_TABLE_NAME, null, null) > 0;
        }finally {
            Logger.d("User deleted from database.");
        }

        return result;
    }

    //Callable Methods
    public Callable<Boolean> cSaveUser(final int id, final String name, final String imageURL, final String thumbnailURL, final double latitude, final double longitude,
                                       final String password, final String email, final boolean verified, final String bio, final int bookCounter, final int point, final int sharedPoint){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return saveUser(id, name, imageURL, thumbnailURL, latitude, longitude, password, email, verified, bio, bookCounter, point, sharedPoint);
            }
        };
    }

    public Callable<Boolean> cSaveUser(final User.Details userDetails){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return saveUser(userDetails);
            }
        };
    }

    public Callable<Boolean> cUpdateUserDetails(final User.Details userDetails){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateUserDetails(userDetails);
            }
        };
    }

    public Callable<Boolean> cUpdateUserLocation(final double latitude, final double longitude){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateUserLocation(latitude, longitude);
            }
        };
    }

    public Callable<Boolean> cUpdateUserImage(final String imageUrl, final String thumbnailUrl){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateUserImage(imageUrl, thumbnailUrl);
            }
        };
    }

    public Callable<Boolean> cUpdateUserPassword(final String password){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return updateUserPassword(password);
            }
        };
    }

    public Callable<Boolean> cDeleteUser(){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return deleteUser();
            }
        };
    }
}
