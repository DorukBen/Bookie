package com.karambit.bookie.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * General class for all database process
 * including:
 *           user table ->
 *                          Table contains current user for login and logout process
 *           loved genres table ->
 *                          Table contains genre id's which user liked
 *           message table ->
 *                          Table contains all messages
 *           message user table ->
 *                          Table contains users currently in a conversation
 *
 * Created by doruk on 12.11.2016.
 */
public class DBHandler extends SQLiteOpenHelper {

    public static final String TAG = DBHandler.class.getSimpleName();

    private static final String DATABASE_NAME = "Bookie.db";

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

    private static final String LG_TABLE_NAME = "loved_genre";
    private static final String LG_COLUMN_ID = "loved_genre_id";
    private static final String LG_COLUMN_USER_ID = "user_id";
    private static final String LG_COLUMN_GENRE_CODE = "genre_code";

    private static final String MESSAGE_TABLE_NAME = "message";
    private static final String MESSAGE_COLUMN_ID = "message_id";
    private static final String MESSAGE_COLUMN_TEXT = "text";
    private static final String MESSAGE_COLUMN_FROM_USER_ID = "from_user_id";
    private static final String MESSAGE_COLUMN_TO_USER_ID = "to_user_id";
    private static final String MESSAGE_COLUMN_IS_DELETED = "is_deleted";
    private static final String MESSAGE_COLUMN_STATE = "state";
    private static final String MESSAGE_COLUMN_CREATED_AT = "created_at";

    private static final String MESSAGE_USER_TABLE_NAME = "message_user";
    private static final String MESSAGE_USER_COLUMN_ID = "user_id";
    private static final String MESSAGE_USER_COLUMN_NAME = "name";
    private static final String MESSAGE_USER_COLUMN_IMAGE_URL = "image_url";
    private static final String MESSAGE_USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String MESSAGE_USER_COLUMN_LATITUDE = "latitude";
    private static final String MESSAGE_USER_COLUMN_LONGITUDE = "longitude";

    private static final String NOTIFICATION_TABLE_NAME = "notification";
    private static final String NOTIFICATION_COLUMN_ID = "notification_id";
    private static final String NOTIFICATION_COLUMN_BOOK_ID = "book_id";
    private static final String NOTIFICATION_COLUMN_USER_ID = "user_id";
    private static final String NOTIFICATION_COLUMN_TYPE = "type";
    private static final String NOTIFICATION_COLUMN_SEEN = "seen";
    private static final String NOTIFICATION_COLUMN_CREATED_AT = "created_at";

    private static final String NOTIFICATION_USER_TABLE_NAME = "notification_user";
    private static final String NOTIFICATION_USER_COLUMN_ID = "user_id";
    private static final String NOTIFICATION_USER_COLUMN_NAME = "name";
    private static final String NOTIFICATION_USER_COLUMN_IMAGE_URL = "image_url";
    private static final String NOTIFICATION_USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String NOTIFICATION_USER_COLUMN_LATITUDE = "latitude";
    private static final String NOTIFICATION_USER_COLUMN_LONGITUDE = "longitude";

    private static final String NOTIFICATION_BOOK_TABLE_NAME = "notification_book";
    private static final String NOTIFICATION_BOOK_COLUMN_ID = "book_id";
    private static final String NOTIFICATION_BOOK_COLUMN_NAME = "name";
    private static final String NOTIFICATION_BOOK_COLUMN_IMAGE_URL = "image_url";
    private static final String NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String NOTIFICATION_BOOK_COLUMN_AUTHOR = "author";
    private static final String NOTIFICATION_BOOK_COLUMN_STATE = "state";
    private static final String NOTIFICATION_BOOK_COLUMN_GENRE = "genre";
    private static final String NOTIFICATION_BOOK_COLUMN_OWNER_ID = "owner_id";

    private static final String BOOK_USER_TABLE_NAME = "book_user";
    private static final String BOOK_USER_COLUMN_ID = "user_id";
    private static final String BOOK_USER_COLUMN_NAME = "name";
    private static final String BOOK_USER_COLUMN_IMAGE_URL = "image_url";
    private static final String BOOK_USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String BOOK_USER_COLUMN_LATITUDE = "latitude";
    private static final String BOOK_USER_COLUMN_LONGITUDE = "longitude";

    private final Context mContext;

    // TODO Draconian synchronization (Synchronized singleton) {@see http://stackoverflow.com/a/11165926}
    // TODO getApplicationContext() in constructor

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + USER_TABLE_NAME + " (" +
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
                        USER_COLUMN_POINT + " INTEGER NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE " + LG_TABLE_NAME + " (" +
                        LG_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        LG_COLUMN_USER_ID + " INTEGER NOT NULL, " +
                        LG_COLUMN_GENRE_CODE + " INTEGER NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE " + MESSAGE_TABLE_NAME + " (" +
                        MESSAGE_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        MESSAGE_COLUMN_TEXT + " TEXT NOT NULL, " +
                        MESSAGE_COLUMN_FROM_USER_ID + " INTEGER NOT NULL, " +
                        MESSAGE_COLUMN_TO_USER_ID + " INTEGER NOT NULL, " +
                        MESSAGE_COLUMN_IS_DELETED + " INTEGER NOT NULL DEFAULT 0, " +
                        MESSAGE_COLUMN_STATE + " INTEGER NOT NULL, " +
                        MESSAGE_COLUMN_CREATED_AT + " LONG NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE " + MESSAGE_USER_TABLE_NAME + " (" +
                        MESSAGE_USER_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        MESSAGE_USER_COLUMN_NAME + " TEXT NOT NULL, " +
                        MESSAGE_USER_COLUMN_IMAGE_URL + " TEXT, " +
                        MESSAGE_USER_COLUMN_THUMBNAIL_URL + " TEXT, " +
                        MESSAGE_USER_COLUMN_LATITUDE + " DOUBLE, " +
                        MESSAGE_USER_COLUMN_LONGITUDE + " DOUBLE)"
        );

        db.execSQL(
                "CREATE TABLE " + NOTIFICATION_TABLE_NAME + " (" +
                        NOTIFICATION_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        NOTIFICATION_COLUMN_BOOK_ID + " INTEGER NOT NULL, " +
                        NOTIFICATION_COLUMN_USER_ID + " INTEGER NOT NULL, " +
                        NOTIFICATION_COLUMN_TYPE + " INTEGER NOT NULL, " +
                        NOTIFICATION_COLUMN_SEEN + " INTEGER NOT NULL, " +
                        NOTIFICATION_COLUMN_CREATED_AT + " LONG NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE " + NOTIFICATION_USER_TABLE_NAME + " (" +
                        NOTIFICATION_USER_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        NOTIFICATION_USER_COLUMN_NAME + " TEXT NOT NULL, " +
                        NOTIFICATION_USER_COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                        NOTIFICATION_USER_COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
                        NOTIFICATION_USER_COLUMN_LATITUDE + " DOUBLE, " +
                        NOTIFICATION_USER_COLUMN_LONGITUDE + " DOUBLE)"
        );

        db.execSQL(
                "CREATE TABLE " + NOTIFICATION_BOOK_TABLE_NAME + " (" +
                        NOTIFICATION_BOOK_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        NOTIFICATION_BOOK_COLUMN_NAME + " TEXT NOT NULL, " +
                        NOTIFICATION_BOOK_COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                        NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
                        NOTIFICATION_BOOK_COLUMN_AUTHOR + " TEXT NOT NULL, " +
                        NOTIFICATION_BOOK_COLUMN_STATE + " INTEGER NOT NULL, " +
                        NOTIFICATION_BOOK_COLUMN_GENRE + " INTEGER NOT NULL, " +
                        NOTIFICATION_BOOK_COLUMN_OWNER_ID + " INTEGER NOT NULL)"
        );

        db.execSQL(
                "CREATE TABLE " + BOOK_USER_TABLE_NAME + " (" +
                        BOOK_USER_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        BOOK_USER_COLUMN_NAME + " TEXT NOT NULL, " +
                        BOOK_USER_COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                        BOOK_USER_COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
                        BOOK_USER_COLUMN_LATITUDE + " DOUBLE, " +
                        BOOK_USER_COLUMN_LONGITUDE + " DOUBLE)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LG_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NOTIFICATION_BOOK_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BOOK_USER_TABLE_NAME);
        onCreate(db);
    }

    /**
     * Insert current user to database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
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
    public boolean insertCurrentUser(int id, String name, String imageURL, String thumbnailURL, double latitude, double longitude,
                                     String password, String email, boolean verified, String bio, int bookCounter, int point) {
        SQLiteDatabase db = null;
        boolean result = false;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

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

            result = db.insert(USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }

            if (result){
                Log.i(TAG,"Current user insertion successful");
            }else{
                Log.e(TAG,"Error occurred during user insertion");
            }
        }
        return result;
    }

    /**
     * Insert current user to database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user User.Details which will be insert.<br>
     *
     * @return {@link Boolean Boolean} result from executed database query. If query successful returns true else returns false.
     */
    public boolean insertCurrentUser(User.Details user) {
        SQLiteDatabase db = null;
        boolean result = false;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

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

            result = db.insert(USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }

            if (result){
                Log.i(TAG,"Current user insertion successful");
            }else{
                Log.e(TAG,"Error occurred during user insertion");
            }
        }
       return result;
    }

    /**
     * Gets current user from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @return Current User.
     */
    public User getCurrentUser() {
        SQLiteDatabase db = null;
        Cursor res = null;
        User user;
        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);
            res.moveToFirst();
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

        }finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return user;
    }

    /**
     * Gets current user details from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @return Current {@link com.karambit.bookie.model.User.Details User.Details}
     */
    public User.Details getCurrentUserDetails() {
        SQLiteDatabase db = null;
        Cursor res = null;
        User.Details details;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);
            res.moveToFirst();
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
                    res.getInt(res.getColumnIndex(USER_COLUMN_POINT)));
        }finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return details;
    }

    /**
     * Updates current users location.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param latitude Current user's latitude value. Can access {@link User User}.getLatitude()<br>
     * @param longitude Current user's longitude value. Can access {@link User User}.getLongitude()<br>
     */
    public void updateCurrentUserLocation(double latitude, double longitude){
        SQLiteDatabase db = null;

        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(USER_COLUMN_LATITUDE, latitude);
            cv.put(USER_COLUMN_LONGITUDE, longitude);

            // getCurrentUser in this class fetch user from database. getCurrentUser in SessionManager fetch user from static User field
            db.update(USER_TABLE_NAME, cv, USER_COLUMN_ID + "=" + SessionManager.getCurrentUser(mContext.getApplicationContext()).getID(), null);
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Current user's location updated");
        }
    }

    /**
     * Update current user to database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param userDetails User.Details which will be insert.<br>
     *
     * @return {@link Boolean Boolean} result from executed database query. If query successful returns true else returns false.
     */
    public boolean updateCurrentUser(User.Details userDetails) {
        SQLiteDatabase db = null;
        boolean result = false;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();

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

            result = db.update(USER_TABLE_NAME, contentValues, USER_COLUMN_ID + "=" + SessionManager.getCurrentUser(mContext).getID(), null) > 0;
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }

            if (result){
                Log.i(TAG,"Current user insertion successful");
            }else{
                Log.e(TAG,"Error occurred during user insertion");
            }
        }
        return result;
    }

    /**
     * Deletes current user from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     */
    public void deleteCurrentUser() {
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();

            db.delete(USER_TABLE_NAME, null, null);
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Current user deleted from database");
        }
    }

    /**
     * Insert loved genres to database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Current {@link User User}<br>
     * @param lovedGenreCodes {@link Integer Integer}[] loved genre codes<br>
     *
     * @return boolean value. If loved genres insertion successful return true else returns false.
     */
    public boolean insertLovedGenres(User user, Integer[] lovedGenreCodes) {
        SQLiteDatabase db = null;
        Cursor res = null;

        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + LG_TABLE_NAME +
                    " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID(), null);

            if (res.getCount() > 0) {
                resetLovedGenres(user);
            }

            res.close();

            for (Integer lovedGenreCode : lovedGenreCodes) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(LG_COLUMN_USER_ID, user.getID());
                contentValues.put(LG_COLUMN_GENRE_CODE, lovedGenreCode);

                if (db.insert(LG_TABLE_NAME, null, contentValues) <= 0) {
                    return false;
                }
            }
        }finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Loved Genres inserted");
        }

        return true;
    }

    /**
     * Gets loved genres from database for given {@link User user}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Current {@link User user}<br>
     *
     * @return int[] loved genre code array.
     */
    public int[] getLovedGenres(User user) {
        SQLiteDatabase db = null;
        Cursor res = null;
        int[] lovedGenres;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + LG_TABLE_NAME +
                    " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID(), null);
            res.moveToFirst();

            lovedGenres = new int[res.getCount()];
            int i = 0;
            if (res.getCount() > 0) {
                do {
                    lovedGenres[i++] = res.getInt(res.getColumnIndex(LG_COLUMN_GENRE_CODE));
                } while (res.moveToNext());
            }
        } finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }

        return lovedGenres;
    }

    /**
     * Gets loved genres from database for given {@link User user}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Current {@link User user}<br>
     *
     * @return {@link Integer Integer}[] loved genre code array.
     */
    public Integer[] getLovedGenresAsInt(User user) {

        int[] lovedGenres = getLovedGenres(user);

        Integer[] selectedGenres = new Integer[lovedGenres.length];
        int i = 0;
        for (int value : lovedGenres) {
            selectedGenres[i++] = value;
        }
        return selectedGenres;
    }

    /**
     * Resets loved genres from database for given {@link User user}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Current {@link User user}<br>
     */
    public void resetLovedGenres(User user) {
        SQLiteDatabase db = null;
        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            db.delete(LG_TABLE_NAME, LG_COLUMN_USER_ID + " = " + user.getID(), null);
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Loved Genres reset");
        }
    }

    /**
     * Checks database for inserted loved genres.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Current {@link User user}<br>
     *
     * @return boolean value. If database have any genre code returns true else false.
     */
    public boolean isLovedGenresSelected(User user) {
        SQLiteDatabase db = null;
        Cursor res = null;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            String countQuery = "SELECT * FROM " + LG_TABLE_NAME + " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID();
            res = db.rawQuery(countQuery, null);
            return res.getCount() > 0;

        } finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * Insert message to database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param message New {@link Message message}<br>
     *
     * @return boolean value. If insertion completed successfully returns true else false.
     */
    public boolean insertMessage(Message message) {
        SQLiteDatabase db = null;
        boolean result = false;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MESSAGE_COLUMN_ID, message.getID());
            contentValues.put(MESSAGE_COLUMN_TEXT, message.getText());
            contentValues.put(MESSAGE_COLUMN_FROM_USER_ID, message.getSender().getID());
            contentValues.put(MESSAGE_COLUMN_TO_USER_ID, message.getReceiver().getID());
            contentValues.put(MESSAGE_COLUMN_IS_DELETED, 0);
            contentValues.put(MESSAGE_COLUMN_CREATED_AT, message.getCreatedAt().getTimeInMillis());
            contentValues.put(MESSAGE_COLUMN_STATE, message.getState().ordinal());

            result = db.insert(MESSAGE_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "New message insertion successful");
        }

        return result;
    }

    /**
     * Updates {@link com.karambit.bookie.model.Message.State}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param messageId {@link Message} id
     * @param state {@link com.karambit.bookie.model.Message.State}
     */
    public void updateMessageState(int messageId, Message.State state){
        SQLiteDatabase db = null;

        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_STATE, state.ordinal());

            db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + messageId, null);
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message state updated");
        }
    }

    /**
     * Updates {@link com.karambit.bookie.model.Message.State}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param message {@link Message}
     * @param state {@link com.karambit.bookie.model.Message.State}
     */
    public void updateMessageState(Message message, Message.State state){
        SQLiteDatabase db = null;

        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_STATE, state.ordinal());

            db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + message.getID(), null);
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message state updated");
        }
    }

    /**
     * Updates {@link Message} id.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param oldMessageId Old {@link Message} id
     * @param newMessageId New {@link Message} id
     */
    public void updateMessageId(int oldMessageId, int newMessageId){
        SQLiteDatabase db = null;

        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_ID, newMessageId);

            db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + oldMessageId, null);
        }finally {
            if (db != null && db.isOpen()) {
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message id updated");
        }
    }

    /**
     * Deletes message from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param messageID {@link Message Message} id<br>
     */
    public void deleteMessage(Integer messageID) {
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + messageID, null);
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message deleted from database");
        }
    }

    /**
     * Deletes message from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param message {@link Message Message}<br>
     */
    public void deleteMessage(Message message) {
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + message.getID(), null);
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message deleted from database");
        }
    }

    /**
     * Gets user's all messages from database which current user have conversation.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param anotherUser Another {@link User user} which have conversation with current {@link User user}<br>
     * @param currentUser Current {@link User user}<br>
     *
     * @return {@link ArrayList Arraylist}<{@link Message Message}> all conversation messages
     */
    public ArrayList<Message> getConversationMessages (User anotherUser, User currentUser) {
        SQLiteDatabase db = null;
        Cursor res = null;
        ArrayList<Message> messages = new ArrayList<>();

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            String deletedString = "1";
            res = db.rawQuery("SELECT * FROM " + MESSAGE_TABLE_NAME +
                    " WHERE (" + MESSAGE_COLUMN_FROM_USER_ID + " = " + anotherUser.getID() + " OR " + MESSAGE_COLUMN_TO_USER_ID +
                    " = " + anotherUser.getID() + ") AND " + MESSAGE_COLUMN_IS_DELETED + " <> " + deletedString + " ORDER BY " + MESSAGE_COLUMN_CREATED_AT + " DESC", null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    Calendar calendar = Calendar.getInstance();
                    long time = res.getLong(res.getColumnIndex(MESSAGE_COLUMN_CREATED_AT)); //replace 4 with the column index
                    calendar.setTimeInMillis(time);

                    Message message;
                    if (res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID)) == SessionManager.getCurrentUser(mContext.getApplicationContext()).getID()) {
                        message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                                              res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                                              currentUser,
                                              anotherUser,
                                              calendar,
                                              Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
                    } else {
                        message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                                              res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                                              anotherUser,
                                              currentUser,
                                              calendar,
                                              Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
                    }
                    messages.add(message);

                } while (res.moveToNext());
            }
        } finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }

        return messages;
    }

    /**
     * Gets last message between another {@link User user} and current {@link User user}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param anotherUser Another {@link User user} which have conversation with current {@link User user}<br>
     * @param currentUser Current {@link User user}<br>
     *
     * @return {@link Message Message} Last {@link Message message} between given users.
     */
    public Message getLastMessage (User anotherUser, User currentUser) {
        SQLiteDatabase db = null;
        Cursor res = null;
        Message message;
        try {
            db = this.getReadableDatabase();

            db.beginTransaction();
            String deletedString = "1";
            res = db.rawQuery("SELECT * FROM " + MESSAGE_TABLE_NAME +
                    " WHERE (" + MESSAGE_COLUMN_FROM_USER_ID + " = " + anotherUser.getID() + " OR " + MESSAGE_COLUMN_TO_USER_ID +
                    " = " + anotherUser.getID() + ") AND " + MESSAGE_COLUMN_IS_DELETED + " <> " + deletedString + " ORDER BY " + MESSAGE_COLUMN_CREATED_AT + " DESC "
                    + "LIMIT 1", null);
            res.moveToFirst();

            Calendar calendar = Calendar.getInstance();
            long time = res.getLong(res.getColumnIndex(MESSAGE_COLUMN_CREATED_AT)); //replace 4 with the column index
            calendar.setTimeInMillis(time);


            if (res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID)) == SessionManager.getCurrentUser(mContext.getApplicationContext()).getID()){
                message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                        res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                        currentUser,
                        anotherUser,
                        calendar,
                        Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
            }else{
                message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                        res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                        anotherUser,
                        currentUser,
                        calendar,
                        Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
            }
        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return message;
    }

    /**
     * Gives last messages in all conversations current user have.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param users {@link ArrayList Arraylist}<{@link User User}> all {@link User users} whose have conversation with current {@link User user}<br>
     * @param currentUser Current {@link User user}<br>
     *
     * @return {@link ArrayList Arraylist}<{@link Message Message}> last messages in all conversations
     */
    public ArrayList<Message> getLastMessages (ArrayList<User> users, User currentUser) {

        ArrayList<Message> messages = new ArrayList<>();
        for (User user : users){
            messages.add(getLastMessage(user,currentUser));
        }

        return messages;
    }

    /**
     * Checks database and finds minimum id from message table.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @return int value. Lowest {@link Message message} id
     */
    public int getMinimumMessageId(){
        SQLiteDatabase db = null;
        Cursor res = null;
        int lastMessageId = 0;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT " + MESSAGE_COLUMN_ID + " FROM " + MESSAGE_TABLE_NAME +
                    " ORDER BY " + MESSAGE_COLUMN_ID + " ASC " +
                    " LIMIT 1", null);
            res.moveToFirst();

            lastMessageId = res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID));

        } finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }

        return lastMessageId;
    }

    /**
     * Counts unseen messages for given {@link User user}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param oppositeUser {@link User} which have conversation with phone user
     * @return Total unseen message count for given user
     */
    public int getUnseenMessageCount(User oppositeUser){
        SQLiteDatabase db = null;
        Cursor res = null;
        String queryVariableString = "totalCount";
        int unseenMessageCount = 0;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT COUNT(*) AS " + queryVariableString + " FROM " + MESSAGE_TABLE_NAME +
                    " WHERE " + MESSAGE_COLUMN_FROM_USER_ID + " = " + oppositeUser.getID() +
                    " AND " + MESSAGE_COLUMN_STATE + " = " + Message.State.DELIVERED.ordinal(), null);
            res.moveToFirst();

            unseenMessageCount = res.getInt(res.getColumnIndex(queryVariableString));

        } finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }

        return unseenMessageCount;
    }

    /**
     * Counts all unseen messages.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @return Total unseen message count
     */
    public int getTotalUnseenMessageCount(){
        SQLiteDatabase db = null;
        Cursor res = null;
        String queryVariableString = "totalCount";
        int totalUnseenMessageCount = 0;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT COUNT(*) AS " + queryVariableString + " FROM " + MESSAGE_TABLE_NAME +
                    " WHERE " + MESSAGE_COLUMN_FROM_USER_ID + " <> " + SessionManager.getCurrentUser(mContext.getApplicationContext()) +
                    " AND " + MESSAGE_COLUMN_STATE + " = " + Message.State.DELIVERED.ordinal(), null);
            res.moveToFirst();

            totalUnseenMessageCount = res.getInt(res.getColumnIndex(queryVariableString));

        } finally {
            if (res != null){
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }

        return totalUnseenMessageCount;
    }

    /**
     * Inserts message user to database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Message {@link User user}<br>
     *
     * @return boolean value. If insertion successful returns true else returns false.
     */
    public boolean insertMessageUser(User user) {
        SQLiteDatabase db = null;
        boolean result = false;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MESSAGE_USER_COLUMN_ID, user.getID());
            contentValues.put(MESSAGE_USER_COLUMN_NAME, user.getName());
            contentValues.put(MESSAGE_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(MESSAGE_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(MESSAGE_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(MESSAGE_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = db.insert(MESSAGE_USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "New Message User insertion successful");
        }
        return result;
    }

    /**
     * Gives all message {@link User users} whose have conversation with current user.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @return {@link ArrayList Arraylist}<{@link User User}> all message users
     */
    public ArrayList<User> getAllMessageUsers() {
        SQLiteDatabase db = null;
        Cursor res = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + MESSAGE_USER_TABLE_NAME, null);
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
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return users;
    }

    /**
     * Checks database for given user id's existence.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param userID Message {@link User user} id
     *
     * @return  boolean value. If message {@link User user} exist returns true else returns false.
     */
    public boolean isMessageUserExists(int userID) {
        SQLiteDatabase db = null;
        Cursor res = null;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + MESSAGE_USER_TABLE_NAME + " WHERE " + USER_COLUMN_ID  + " = " + userID, null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * Checks database for given user id's existence.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Message {@link User user}
     *
     * @return  boolean value. If message {@link User user} exist returns true else returns false.
     */
    public boolean isMessageUserExists(User user) {
        SQLiteDatabase db = null;
        Cursor res = null;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + MESSAGE_USER_TABLE_NAME + " WHERE " + USER_COLUMN_ID  + " = " + user.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * Deletes message {@link User user} from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param userID Message {@link User user's} id, ({@link User#getID()})
     */
    public void deleteMessageUser(Integer userID) {
        SQLiteDatabase db = null;
        int result;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();

            db.delete(MESSAGE_USER_TABLE_NAME, MESSAGE_USER_COLUMN_ID + " = ?", new String[] { userID.toString() });
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message User deleted from database");
        }
    }

    /**
     * Deletes message {@link User user} from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Message {@link User user}
     */
    public void deleteMessageUser(User user) {
        SQLiteDatabase db = null;
        int result;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();

            db.delete(MESSAGE_USER_TABLE_NAME, MESSAGE_USER_COLUMN_ID + " = ?", new String[] { ((Integer)user.getID()).toString() });
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message User deleted from database");
        }
    }

    /**
     * Deletes message {@link User user's} all conversation messages from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param userID Message {@link User user} id
     */
    public void deleteMessageUsersConversation(Integer userID){
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_FROM_USER_ID + "=" + userID + " OR " +
                    MESSAGE_COLUMN_TO_USER_ID + " = " + userID, null);
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message users conversation messages deleted from database");
        }
    }

    /**
     * Deletes message {@link User user's} all conversation messages from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Message {@link User user}
     */
    public void deleteMessageUsersConversation(User user){
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            String deletedString = "1";
            ContentValues cv = new ContentValues();
            cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

            db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_FROM_USER_ID + "=" + user.getID() + " OR " +
                    MESSAGE_COLUMN_TO_USER_ID + " = " + user.getID(), null);
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "Message users conversation messages deleted from database");
        }
    }

    /**
     * Deletes all messages and message users from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     */
    public void deleteAllMessages() {
        SQLiteDatabase db = null;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();

            db.delete(MESSAGE_USER_TABLE_NAME, null, null);
            db.delete(MESSAGE_TABLE_NAME, null, null);

        } finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        Log.i(TAG, "All Message Users and Messages deleted from database");
    }

    public void saveNotificationToDatabase(Notification notification){
        if (!isBookUserExists(notification.getBook().getOwner())){
            insertBookUser(notification.getBook().getOwner());
        }
        if (!isNotificationBookExists(notification.getBook())){
            insertNotificationBook(notification.getBook());
        }
        if (!isNotificationUserExists(notification.getOppositeUser())){
            insertNotificationUser(notification.getOppositeUser());
        }

        insertNotification(notification);
    }

    /**
     * Insert notification to database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param notification {@link Notification} which will be inserted
     * @return Returns int boolean value if insertion successful returns true else returns false
     */
    public boolean insertNotification(Notification notification) {
        SQLiteDatabase db = null;
        boolean result = false;
        int messageSeen = 1;
        int messageUnseen = 0;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_COLUMN_BOOK_ID, notification.getBook().getID());
            contentValues.put(NOTIFICATION_COLUMN_USER_ID, notification.getOppositeUser().getID());
            contentValues.put(NOTIFICATION_COLUMN_TYPE, notification.getType().getTypeCode());
            if (notification.isSeen()){
                contentValues.put(NOTIFICATION_COLUMN_SEEN, messageSeen);
            }else{
                contentValues.put(NOTIFICATION_COLUMN_SEEN, messageUnseen);
            }
            contentValues.put(NOTIFICATION_COLUMN_CREATED_AT, notification.getCreatedAt().getTimeInMillis());

            result = db.insert(NOTIFICATION_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "New Notification insertion successful");
        }
        return result;
    }

    /**
     * get all {@link Notification notification's} from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param allNotificationUsers All notification users. Use {@link DBHandler#getAllNotificationUsers()}
     * @param allNotificationBooks All notification books. Use {@link DBHandler#getAllNotificationBooks(ArrayList)}
     * @return Returns all notifications from database
     */
    public ArrayList<Notification> getAllNotifications(ArrayList<User> allNotificationUsers, ArrayList<Book> allNotificationBooks) {
        SQLiteDatabase db = null;
        Cursor res = null;
        int seenString = 1;
        ArrayList<Notification> notifications = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + NOTIFICATION_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    User oppositeUser = null;
                    for (User user: allNotificationUsers){
                        if (user.getID() == res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_USER_ID))){
                            oppositeUser = user;
                        }
                    }

                    Book notificationBook = null;
                    for (Book book: allNotificationBooks){
                        if (book.getID() == res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_BOOK_ID))){
                            notificationBook = book;
                        }
                    }

                    Calendar calendar = Calendar.getInstance();
                    long time = res.getLong(res.getColumnIndex(NOTIFICATION_COLUMN_CREATED_AT)); //replace 4 with the column index
                    calendar.setTimeInMillis(time);

                    Notification notification;
                    if (res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_SEEN)) == seenString){
                        notification = new Notification(Notification.Type.valueOf(res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_TYPE))),
                                calendar,
                                notificationBook,
                                oppositeUser,
                                true);
                    }else {
                        notification = new Notification(Notification.Type.valueOf(res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_TYPE))),
                                calendar,
                                notificationBook,
                                oppositeUser,
                                false);
                    }


                    notifications.add(notification);
                } while (res.moveToNext());
            }
        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return notifications;
    }

    /**
     * Insert notification {@link Book book} to database.<br>
     *
     *     Before any {@link Book book} insertion use {@link DBHandler#isNotificationBookExists(Book)}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param book {@link Book} which will be inserted
     * @return Returns boolean value if insertion successful returns true else returns false
     */
    public boolean insertNotificationBook(Book book) {
        SQLiteDatabase db = null;
        boolean result = false;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_BOOK_COLUMN_ID, book.getID());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_NAME, book.getName());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_IMAGE_URL, book.getImageURL());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL, book.getThumbnailURL());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_AUTHOR, book.getAuthor());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_STATE, book.getState().getStateCode());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_GENRE, book.getGenreCode());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_OWNER_ID, book.getOwner().getID());

            result = db.insert(NOTIFICATION_BOOK_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "New Notification Book insertion successful");
        }
        return result;
    }

    /**
     * Checks database for given book's existence. Use before all notification book insertions.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param book Message {@link Book book}
     *
     * @return  boolean value. If message {@link Book book} exist returns true else returns false.
     */
    public boolean isNotificationBookExists(Book book) {
        SQLiteDatabase db = null;
        Cursor res = null;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + NOTIFICATION_BOOK_TABLE_NAME + " WHERE " + NOTIFICATION_BOOK_COLUMN_ID  + " = " + book.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * Get all notification {@link Book book's} from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param bookUsers All book {@link User user's}. Use {@link DBHandler#getAllBookUsers()}
     * @return All notification {@link Book books's}
     */
    public ArrayList<Book> getAllNotificationBooks(ArrayList<User> bookUsers) {
        SQLiteDatabase db = null;
        Cursor res = null;
        ArrayList<Book> books = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + NOTIFICATION_BOOK_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    for (User user: bookUsers){
                        if (user.getID() == res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_OWNER_ID))){
                            Book book = new Book(res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_ID)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_NAME)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_IMAGE_URL)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_AUTHOR)),
                                    Book.State.valueOf(res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_STATE))),
                                    res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_GENRE)),
                                    user);

                            books.add(book);
                        }
                    }
                } while (res.moveToNext());
            }
        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return books;
    }

    /**
     * Insert notification user to database.<br>
     *
     *     Before any {@link User user} insertion use {@link DBHandler#isNotificationUserExists(User)}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user {@link User} which will be inserted
     * @return Returns boolean value if insertion successful returns true else returns false
     */
    public boolean insertNotificationUser(User user) {
        SQLiteDatabase db = null;
        boolean result = false;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_USER_COLUMN_ID, user.getID());
            contentValues.put(NOTIFICATION_USER_COLUMN_NAME, user.getName());
            contentValues.put(NOTIFICATION_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(NOTIFICATION_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(NOTIFICATION_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(NOTIFICATION_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = db.insert(NOTIFICATION_USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "New Notification User insertion successful");
        }
        return result;
    }

    /**
     * Checks database for given user's existence. Use before all notification user insertions.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Notification {@link User user}
     *
     * @return  boolean value. If message {@link User user} exist returns true else returns false.
     */
    public boolean isNotificationUserExists(User user) {
        SQLiteDatabase db = null;
        Cursor res = null;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + NOTIFICATION_USER_TABLE_NAME + " WHERE " + NOTIFICATION_USER_COLUMN_ID  + " = " + user.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * Get all notification {@link User user's} from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @return All notification {@link User user's}
     */
    public ArrayList<User> getAllNotificationUsers() {
        SQLiteDatabase db = null;
        Cursor res = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + NOTIFICATION_USER_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    User user;
                    if (res.isNull(res.getColumnIndex(NOTIFICATION_USER_COLUMN_LATITUDE)) || res.isNull(res.getColumnIndex(NOTIFICATION_USER_COLUMN_LONGITUDE))){
                        user = new User(res.getInt(res.getColumnIndex(NOTIFICATION_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(NOTIFICATION_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(NOTIFICATION_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(NOTIFICATION_USER_COLUMN_THUMBNAIL_URL)),
                                null);
                    }else {
                        user = new User(res.getInt(res.getColumnIndex(NOTIFICATION_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(NOTIFICATION_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(NOTIFICATION_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(NOTIFICATION_USER_COLUMN_THUMBNAIL_URL)),
                                new LatLng(res.getDouble(res.getColumnIndex(NOTIFICATION_USER_COLUMN_LATITUDE)), res.getDouble(res.getColumnIndex(NOTIFICATION_USER_COLUMN_LONGITUDE)))
                                );
                    }


                    users.add(user);
                } while (res.moveToNext());
            }
        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return users;
    }

    /**
     * Insert book user to database.<br>
     *
     *     Before any {@link User user} insertion use {@link DBHandler#isBookUserExists(User)}.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user {@link User} which will be inserted
     * @return Returns boolean value if insertion successful returns true else returns false
     */
    public boolean insertBookUser(User user) {
        SQLiteDatabase db = null;
        boolean result = false;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(BOOK_USER_COLUMN_ID, user.getID());
            contentValues.put(BOOK_USER_COLUMN_NAME, user.getName());
            contentValues.put(BOOK_USER_COLUMN_IMAGE_URL, user.getImageUrl());
            contentValues.put(BOOK_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
            contentValues.put(BOOK_USER_COLUMN_LATITUDE, (user.getLocation() != null) ? user.getLocation().latitude : null);
            contentValues.put(BOOK_USER_COLUMN_LONGITUDE, (user.getLocation() != null) ? user.getLocation().longitude : null);

            result = db.insert(BOOK_USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
            Log.i(TAG, "New Book User insertion successful");
        }
        return result;
    }

    /**
     * Checks database for given user's existence. Use before all book user insertions.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @param user Book {@link User user}
     *
     * @return  boolean value. If message {@link User user} exist returns true else returns false.
     */
    public boolean isBookUserExists(User user) {
        SQLiteDatabase db = null;
        Cursor res = null;

        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + BOOK_USER_TABLE_NAME + " WHERE " + BOOK_USER_COLUMN_ID  + " = " + user.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
    }

    /**
     * Get all book {@link User user's} from database.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>
     *
     * @return All notification {@link User user's}
     */
    public ArrayList<User> getAllBookUsers() {
        SQLiteDatabase db = null;
        Cursor res = null;
        ArrayList<User> users = new ArrayList<>();
        try {
            db = this.getReadableDatabase();
            db.beginTransaction();
            res = db.rawQuery("SELECT * FROM " + BOOK_USER_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    User user;
                    if (res.isNull(res.getColumnIndex(BOOK_USER_COLUMN_LATITUDE)) || res.isNull(res.getColumnIndex(BOOK_USER_COLUMN_LONGITUDE))){
                        user = new User(res.getInt(res.getColumnIndex(BOOK_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(BOOK_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(BOOK_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(BOOK_USER_COLUMN_THUMBNAIL_URL)),
                                null);
                    }else {
                        user = new User(res.getInt(res.getColumnIndex(BOOK_USER_COLUMN_ID)),
                                res.getString(res.getColumnIndex(BOOK_USER_COLUMN_NAME)),
                                res.getString(res.getColumnIndex(BOOK_USER_COLUMN_IMAGE_URL)),
                                res.getString(res.getColumnIndex(BOOK_USER_COLUMN_THUMBNAIL_URL)),
                                new LatLng(res.getDouble(res.getColumnIndex(BOOK_USER_COLUMN_LATITUDE)), res.getDouble(res.getColumnIndex(BOOK_USER_COLUMN_LONGITUDE)))
                                );
                    }


                    users.add(user);
                } while (res.moveToNext());
            }
        }finally {
            if (res != null) {
                res.close();
            }
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return users;
    }
}