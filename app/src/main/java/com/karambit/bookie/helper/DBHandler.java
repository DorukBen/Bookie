package com.karambit.bookie.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.karambit.bookie.model.User;

/**
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
    private final Context mContent;

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
        mContent = context;
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LG_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertCurrentUser(int id, String name, String imageURL, String thumbnailURL, double latitude, double longitude,
                                     String password, String email, boolean verified, String bio, int bookCounter, int point) {
        SQLiteDatabase db = this.getWritableDatabase();
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

        boolean result = db.insert(USER_TABLE_NAME, null, contentValues) > 0;

        if (db.isOpen()){
            db.close();
        }

        if (result) {
            return true;
        } else {
            return false;
        }
    }

    public boolean insertCurrentUser(User.Details user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COLUMN_ID, user.getUser().getID());
        contentValues.put(USER_COLUMN_NAME, user.getUser().getName());
        contentValues.put(USER_COLUMN_IMAGE_URL, user.getUser().getImageUrl());
        contentValues.put(USER_COLUMN_THUMBNAIL_URL, user.getUser().getThumbnailUrl());
        contentValues.put(USER_COLUMN_LATITUDE, user.getUser().getLatitude());
        contentValues.put(USER_COLUMN_LONGITUDE, user.getUser().getLongitude());
        contentValues.put(USER_COLUMN_PASSWORD, user.getPassword());
        contentValues.put(USER_COLUMN_EMAIL, user.getEmail());
        contentValues.put(USER_COLUMN_VERIFIED, user.isVerified());
        contentValues.put(USER_COLUMN_BIO, user.getBio());
        contentValues.put(USER_COLUMN_BOOK_COUNTER, user.getBookCounter());
        contentValues.put(USER_COLUMN_POINT, user.getPoint());

        boolean result = db.insert(USER_TABLE_NAME, null, contentValues) > 0;

        if (db.isOpen()){
            db.close();
        }

        if (result) {
            return true;
        } else {
            return false;
        }
    }

    public User getCurrentUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECt * FROM " + USER_TABLE_NAME, null);
        res.moveToFirst();
        User user = new User(res.getInt(res.getColumnIndex(USER_COLUMN_ID)),
                res.getString(res.getColumnIndex(USER_COLUMN_NAME)),
                res.getString(res.getColumnIndex(USER_COLUMN_IMAGE_URL)),
                res.getString(res.getColumnIndex(USER_COLUMN_THUMBNAIL_URL)),
                res.getDouble(res.getColumnIndex(USER_COLUMN_LATITUDE)),
                res.getDouble(res.getColumnIndex(USER_COLUMN_LONGITUDE)));
        res.close();

        if (db.isOpen()){
            db.close();
        }
        return user;
    }

    public User.Details getCurrentUserDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECt * FROM " + USER_TABLE_NAME, null);
        res.moveToFirst();
        User user = new User(res.getInt(res.getColumnIndex(USER_COLUMN_ID)),
                res.getString(res.getColumnIndex(USER_COLUMN_NAME)),
                res.getString(res.getColumnIndex(USER_COLUMN_IMAGE_URL)),
                res.getString(res.getColumnIndex(USER_COLUMN_THUMBNAIL_URL)),
                res.getDouble(res.getColumnIndex(USER_COLUMN_LATITUDE)),
                res.getDouble(res.getColumnIndex(USER_COLUMN_LONGITUDE)));


        User.Details details = user.new Details(res.getString(res.getColumnIndex(USER_COLUMN_PASSWORD)),
                res.getString(res.getColumnIndex(USER_COLUMN_EMAIL)),
                res.getInt(res.getColumnIndex(USER_COLUMN_VERIFIED)) > 0,
                res.getString(res.getColumnIndex(USER_COLUMN_BIO)),
                res.getInt(res.getColumnIndex(USER_COLUMN_BOOK_COUNTER)),
                res.getInt(res.getColumnIndex(USER_COLUMN_POINT)));

        res.close();

        if (db.isOpen()){
            db.close();
        }
        return details;
    }

    public void updateCurrentUserLocation(double latitude, double longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(USER_COLUMN_LATITUDE, latitude);
        cv.put(USER_COLUMN_LONGITUDE, longitude);

        db.update(USER_TABLE_NAME, cv, USER_COLUMN_ID + "=" + SessionManager.getCurrentUser(mContent).getID(), null);

        if (db.isOpen()){
            db.close();
        }
    }

    public int deleteCurrentUser() {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(USER_TABLE_NAME, null, null);

        if (db.isOpen()){
            db.close();
        }
        return result;
    }

    public boolean insertLovedGenres(User user, Integer[] lovedGenreCodes) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + LG_TABLE_NAME +
                            " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID(), null);

        if (cursor.getCount() > 0) {
            resetLovedGenres(user);
        }

        cursor.close();

        for (Integer lovedGenreCode : lovedGenreCodes) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(LG_COLUMN_USER_ID, user.getID());
            contentValues.put(LG_COLUMN_GENRE_CODE, lovedGenreCode);

            if (db.insert(LG_TABLE_NAME, null, contentValues) <= 0) {
                return false;
            }
        }

        if (db.isOpen()){
            db.close();
        }

        Log.i(TAG, "Loved Genres inserted");
        return true;
    }

    public int[] getLovedGenres(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + LG_TABLE_NAME +
                                         " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID(), null);
        res.moveToFirst();

        int[] lovedGenres = new int[res.getCount()];
        int i = 0;
        try {
            while (res.moveToNext()) {
                lovedGenres[i++] = res.getInt(res.getColumnIndex(LG_COLUMN_GENRE_CODE));
            }
        } finally {

            res.close();

            if (db.isOpen()){
                db.close();
            }
        }

        return lovedGenres;
    }

    public Integer[] getLovedGenresAsInteger(User user) {

        int[] lovedGenres = getLovedGenres(user);

        Integer[] selectedGenres = new Integer[lovedGenres.length];
        int i = 0;
        for (int value : lovedGenres) {
            selectedGenres[i++] = value;
        }
        return selectedGenres;
    }

    public void resetLovedGenres(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(LG_TABLE_NAME, LG_COLUMN_USER_ID + " = " + user.getID(), null);

        Log.i(TAG, "Loved Genres reset");

        db.close();
    }

    public boolean isLovedGenresSelected(User user) {
        Cursor cursor = null;
        SQLiteDatabase db = null;

        try {
            db = this.getReadableDatabase();
            String countQuery = "SELECT * FROM " + LG_TABLE_NAME + " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID();
            cursor = db.rawQuery(countQuery, null);
            return cursor.getCount() > 0;

        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}