package com.karambit.bookie.helper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.karambit.bookie.adapter.MessageAdapter;
import com.karambit.bookie.model.Message;
import com.karambit.bookie.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

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

        db.execSQL(
                "CREATE TABLE " + MESSAGE_TABLE_NAME + " (" +
                        MESSAGE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        MESSAGE_COLUMN_TEXT + " TEXT NOT NULL, " +
                        MESSAGE_COLUMN_FROM_USER_ID + " INTEGER NOT NULL, " +
                        MESSAGE_COLUMN_TO_USER_ID + " INTEGER NOT NULL, " +
                        MESSAGE_COLUMN_IS_DELETED + " INTEGER NOT DEFAULT 0, " +
                        MESSAGE_COLUMN_STATE + " INTEGER NOT NULL, " +
                        MESSAGE_COLUMN_CREATED_AT + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );

        db.execSQL(
                "CREATE TABLE " + MESSAGE_USER_TABLE_NAME + " (" +
                        MESSAGE_USER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        MESSAGE_USER_COLUMN_NAME + " TEXT NOT NULL, " +
                        MESSAGE_USER_COLUMN_IMAGE_URL + " TEXT, " +
                        MESSAGE_USER_COLUMN_THUMBNAIL_URL + " TEXT, " +
                        MESSAGE_USER_COLUMN_LATITUDE + " DOUBLE, " +
                        MESSAGE_USER_COLUMN_LONGITUDE + " DOUBLE)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LG_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_USER_TABLE_NAME);
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
        db.beginTransaction();
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

        db.setTransactionSuccessful();
        db.endTransaction();


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
        db.beginTransaction();
        Cursor res = db.rawQuery("SELECt * FROM " + USER_TABLE_NAME, null);
        res.moveToFirst();
        User user = new User(res.getInt(res.getColumnIndex(USER_COLUMN_ID)),
                res.getString(res.getColumnIndex(USER_COLUMN_NAME)),
                res.getString(res.getColumnIndex(USER_COLUMN_IMAGE_URL)),
                res.getString(res.getColumnIndex(USER_COLUMN_THUMBNAIL_URL)),
                res.getDouble(res.getColumnIndex(USER_COLUMN_LATITUDE)),
                res.getDouble(res.getColumnIndex(USER_COLUMN_LONGITUDE)));
        res.close();

        db.setTransactionSuccessful();
        db.endTransaction();


        if (db.isOpen()){
            db.close();
        }
        return user;
    }

    public User.Details getCurrentUserDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
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

        db.setTransactionSuccessful();
        db.endTransaction();

        if (db.isOpen()){
            db.close();
        }
        return details;
    }

    public void updateCurrentUserLocation(double latitude, double longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        ContentValues cv = new ContentValues();
        cv.put(USER_COLUMN_LATITUDE, latitude);
        cv.put(USER_COLUMN_LONGITUDE, longitude);

        db.update(USER_TABLE_NAME, cv, USER_COLUMN_ID + "=" + SessionManager.getCurrentUser(mContent).getID(), null);

        db.setTransactionSuccessful();
        db.endTransaction();

        if (db.isOpen()){
            db.close();
        }
    }

    public int deleteCurrentUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        int result = db.delete(USER_TABLE_NAME, null, null);

        db.setTransactionSuccessful();
        db.endTransaction();

        if (db.isOpen()){
            db.close();
        }
        return result;
    }

    public boolean insertLovedGenres(User user, Integer[] lovedGenreCodes) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
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
        db.setTransactionSuccessful();
        db.endTransaction();

        if (db.isOpen()){
            db.close();
        }

        Log.i(TAG, "Loved Genres inserted");
        return true;
    }

    public int[] getLovedGenres(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
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
            db.setTransactionSuccessful();
            db.endTransaction();

            if (db.isOpen()){
                db.close();
            }
        }

        return lovedGenres;
    }

    public Integer[] getLovedGenresAsInt(User user) {

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
        db.beginTransaction();
        db.delete(LG_TABLE_NAME, LG_COLUMN_USER_ID + " = " + user.getID(), null);

        Log.i(TAG, "Loved Genres reset");
        db.setTransactionSuccessful();
        db.endTransaction();
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

    public boolean insertMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGE_COLUMN_TEXT, message.getText());
        contentValues.put(MESSAGE_COLUMN_FROM_USER_ID, message.getSender().getID());
        contentValues.put(MESSAGE_COLUMN_TO_USER_ID, message.getReceiver().getID());
        contentValues.put(MESSAGE_COLUMN_IS_DELETED, 0);
        contentValues.put(MESSAGE_COLUMN_STATE, message.getState().ordinal());

        boolean result = db.insert(MESSAGE_TABLE_NAME, null, contentValues) > 0;

        db.setTransactionSuccessful();
        db.endTransaction();
        if (db.isOpen()){
            db.close();
        }

        if (result) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Message> getConversationMessages (Integer anotherUserID) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String notDeletedString = "0";
        Cursor res = db.rawQuery("SELECT * FROM " + MESSAGE_TABLE_NAME +
                " WHERE (" + MESSAGE_COLUMN_FROM_USER_ID + " = " + anotherUserID + " OR " + MESSAGE_COLUMN_TO_USER_ID +
                " = " + anotherUserID + ") AND " + MESSAGE_COLUMN_IS_DELETED + " <> " + notDeletedString + " LIMIT 1", null);
        res.moveToFirst();

        ArrayList<Message> messages = new ArrayList<>();

        try {
            while (res.moveToNext()) {
                Message message;

                Calendar calendar = new GregorianCalendar();
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY", Locale.getDefault());
                    java.util.Date dt = sdf.parse(res.getString(res.getColumnIndex(MESSAGE_COLUMN_CREATED_AT))); //replace 4 with the column index
                    calendar.setTime(dt);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID)) == getCurrentUser().getID()){
                    message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                            res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                            getCurrentUser(),
                            getMessageUser(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_TO_USER_ID))),
                            calendar,
                            Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
                }else{
                    message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                            res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                            getMessageUser(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID))),
                            getCurrentUser(),
                            calendar,
                            Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
                }

                messages.add(message);
            }
        } finally {

            res.close();
            db.setTransactionSuccessful();
            db.endTransaction();

            if (db.isOpen()){
                db.close();
            }
        }

        return messages;
    }

    public void deleteMessage(Integer messageID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        String deletedString = "1";
        ContentValues cv = new ContentValues();
        cv.put(MESSAGE_COLUMN_IS_DELETED, deletedString);

        db.update(MESSAGE_TABLE_NAME, cv, MESSAGE_COLUMN_ID + "=" + messageID, null);

        db.setTransactionSuccessful();
        db.endTransaction();

        if (db.isOpen()){
            db.close();
        }
    }

    public Integer[] getLastFromUserIds () {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String notDeletedString = "0";
        Cursor res = db.rawQuery("SELECT " + MESSAGE_COLUMN_FROM_USER_ID + " FROM " + MESSAGE_TABLE_NAME +
                " WHERE " + MESSAGE_COLUMN_IS_DELETED + " <> " + notDeletedString + " GROUP BY " +
                MESSAGE_COLUMN_FROM_USER_ID , null);
        res.moveToFirst();

        Integer[] lastFromUserIds = new Integer[res.getCount()];
        int i = 0;
        try {
            while (res.moveToNext()) {
                lastFromUserIds[i++] = res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID));
            }
        } finally {

            res.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            if (db.isOpen()){
                db.close();
            }
        }

        return lastFromUserIds;
    }

    public Integer[] getLastToUserIds () {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String notDeletedString = "0";
        Cursor res = db.rawQuery("SELECT " + MESSAGE_COLUMN_TO_USER_ID + " FROM " + MESSAGE_TABLE_NAME +
                " WHERE " + MESSAGE_COLUMN_IS_DELETED + " <> " + notDeletedString + " GROUP BY " +
                MESSAGE_COLUMN_TO_USER_ID , null);
        res.moveToFirst();

        Integer[] lastToUserIds = new Integer[res.getCount()];
        int i = 0;
        try {
            while (res.moveToNext()) {
                lastToUserIds[i++] = res.getInt(res.getColumnIndex(MESSAGE_COLUMN_TO_USER_ID));
            }
        } finally {

            res.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            if (db.isOpen()){
                db.close();
            }
        }

        return lastToUserIds;
    }

    public Message getLastMessage (int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        String notDeletedString = "0";
        Cursor res = db.rawQuery("SELECt * FROM " + MESSAGE_TABLE_NAME + " WHERE (" + MESSAGE_COLUMN_FROM_USER_ID + " = " + userID +
                " OR " + MESSAGE_COLUMN_TO_USER_ID + " = " + userID + ") AND " + MESSAGE_COLUMN_IS_DELETED + " <> " + notDeletedString + " LIMIT 1", null);
        res.moveToFirst();


        Calendar calendar = new GregorianCalendar();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY", Locale.getDefault());
            java.util.Date dt = sdf.parse(res.getString(res.getColumnIndex(MESSAGE_COLUMN_CREATED_AT))); //replace 4 with the column index
            calendar.setTime(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Message message;
        if (res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID)) == getCurrentUser().getID()){
            message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                    res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                    getCurrentUser(),
                    getMessageUser(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_TO_USER_ID))),
                    calendar,
                    Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
        }else{
            message = new Message(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_ID)),
                    res.getString(res.getColumnIndex(MESSAGE_COLUMN_TEXT)),
                    getMessageUser(res.getInt(res.getColumnIndex(MESSAGE_COLUMN_FROM_USER_ID))),
                    getCurrentUser(),
                    calendar,
                    Message.State.values()[res.getInt(res.getColumnIndex(MESSAGE_COLUMN_STATE))]);
        }

        res.close();

        db.setTransactionSuccessful();
        db.endTransaction();


        if (db.isOpen()){
            db.close();
        }
        return message;
    }

    public ArrayList<Message> getLastMessages () {

        Integer[] lastFromIds = getLastFromUserIds();
        Integer[] lastToIds = getLastToUserIds();

        ArrayList<Integer> lastIdsArray = new ArrayList<>();

        for (Integer x : lastFromIds){
            if (!lastIdsArray.contains(x) && x != getCurrentUser().getID())
                lastIdsArray.add(x);
        }

        for (Integer x : lastToIds){
            if (!lastIdsArray.contains(x) && x != getCurrentUser().getID())
                lastIdsArray.add(x);
        }

        ArrayList<Message> messages = new ArrayList<>();
        for (Integer id : lastIdsArray){
            messages.add(getLastMessage(id));
        }

        return messages;
    }

    public boolean insertMessageUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGE_USER_COLUMN_ID, user.getID());
        contentValues.put(MESSAGE_USER_COLUMN_NAME, user.getName());
        contentValues.put(MESSAGE_USER_COLUMN_IMAGE_URL, user.getImageUrl());
        contentValues.put(MESSAGE_USER_COLUMN_THUMBNAIL_URL, user.getThumbnailUrl());
        contentValues.put(MESSAGE_USER_COLUMN_LATITUDE, user.getLatitude());
        contentValues.put(MESSAGE_USER_COLUMN_LONGITUDE, user.getLongitude());

        boolean result = db.insert(MESSAGE_USER_TABLE_NAME, null, contentValues) > 0;

        db.setTransactionSuccessful();
        db.endTransaction();

        if (db.isOpen()){
            db.close();
        }

        if (result) {
            return true;
        } else {
            return false;
        }
    }

    public User getMessageUser(int userID) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
        Cursor res = db.rawQuery("SELECt * FROM " + MESSAGE_USER_TABLE_NAME + " WHERE " + MESSAGE_USER_COLUMN_ID + " = " + userID, null);
        res.moveToFirst();
        User user = new User(res.getInt(res.getColumnIndex(MESSAGE_USER_COLUMN_ID)),
                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_NAME)),
                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_IMAGE_URL)),
                res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_THUMBNAIL_URL)),
                res.getDouble(res.getColumnIndex(MESSAGE_USER_COLUMN_LATITUDE)),
                res.getDouble(res.getColumnIndex(MESSAGE_USER_COLUMN_LONGITUDE)));
        res.close();

        db.setTransactionSuccessful();
        db.endTransaction();


        if (db.isOpen()){
            db.close();
        }
        return user;
    }

    public int deleteMessageUser(Integer userID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        int result = db.delete(MESSAGE_USER_TABLE_NAME, MESSAGE_USER_COLUMN_ID + " = ?", new String[] { userID.toString() });

        db.setTransactionSuccessful();
        db.endTransaction();

        if (db.isOpen()){
            db.close();
        }
        return result;
    }
}