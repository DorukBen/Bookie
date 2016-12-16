package com.karambit.bookie.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.karambit.bookie.model.User;

import java.util.HashMap;

/**
 * Created by doruk on 12.11.2016.
 */
public class DBHandler extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Bookie.db";
    public static final String USER_TABLE_NAME = "user";
    public static final String USER_COLUMN_ID = "id";
    public static final String USER_COLUMN_NAME = "name";
    public static final String USER_COLUMN_IMAGE_URL = "image_url";
    public static final String USER_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    public static final String USER_COLUMN_LATITUDE = "latitude";
    public static final String USER_COLUMN_LONGITUDE = "longitude";
    public static final String USER_COLUMN_PASSWORD = "password";
    public static final String USER_COLUMN_EMAIL = "email";
    public static final String USER_COLUMN_VERIFIED = "verified";
    public static final String USER_COLUMN_BIO = "bio";
    public static final String USER_COLUMN_BOOK_COUNTER = "book_counter";
    public static final String USER_COLUMN_POINT = "point";
    private HashMap hp;

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + USER_TABLE_NAME +
                        " (" + USER_COLUMN_ID + " INTEGER PRIMERY KEY, " +
                        USER_COLUMN_NAME + " TEXT, " +
                        USER_COLUMN_IMAGE_URL + " TEXT, " +
                        USER_COLUMN_THUMBNAIL_URL + " TEXT, " +
                        USER_COLUMN_LATITUDE + " DOUBLE, " +
                        USER_COLUMN_LONGITUDE + " DOUBLE, " +
                        USER_COLUMN_PASSWORD + " TEXT, " +
                        USER_COLUMN_EMAIL + " TEXT, " +
                        USER_COLUMN_VERIFIED + " BIT, " +
                        USER_COLUMN_BIO + " TEXT, " +
                        USER_COLUMN_BOOK_COUNTER + " INTEGER, " +
                        USER_COLUMN_POINT + " INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
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

    public int deleteCurrentUser() {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(USER_TABLE_NAME, null, null);

        if (db.isOpen()){
            db.close();
        }
        return result;
    }
}