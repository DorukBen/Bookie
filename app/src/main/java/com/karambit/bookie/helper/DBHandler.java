package com.karambit.bookie.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.karambit.bookie.model.Message;
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


    private final Context mContext;

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
        Cursor res = db.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);
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
        Cursor res = db.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);
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

        // getCurrentUser in this class fetch user from database. getCurrentUser in SessionManager fetch user from static User field
        db.update(USER_TABLE_NAME, cv, USER_COLUMN_ID + "=" + SessionManager.getCurrentUser(mContext.getApplicationContext()).getID(), null);

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

//        db.beginTransaction();
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
//        db.setTransactionSuccessful();
//        db.endTransaction();

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
            if (res.getCount() > 0) {
                do {
                    lovedGenres[i++] = res.getInt(res.getColumnIndex(LG_COLUMN_GENRE_CODE));
                } while (res.moveToNext());
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
        SQLiteDatabase db = null;
        boolean result = false;
        try {
            db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
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
        }

        return result;
    }

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
        }
    }

    public void deleteMessage(Message message) {
        deleteMessage(message.getID());
    }

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

    public ArrayList<Message> getLastMessages (ArrayList<User> users, User currentUser) {

        ArrayList<Message> messages = new ArrayList<>();
        for (User user : users){
            messages.add(getLastMessage(user,currentUser));
        }

        return messages;
    }

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
            contentValues.put(MESSAGE_USER_COLUMN_LATITUDE, user.getLatitude());
            contentValues.put(MESSAGE_USER_COLUMN_LONGITUDE, user.getLongitude());

            result = db.insert(MESSAGE_USER_TABLE_NAME, null, contentValues) > 0;
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }
        return result;
    }

    public ArrayList<User> getMessageUsers() {
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
                    User user = new User(res.getInt(res.getColumnIndex(MESSAGE_USER_COLUMN_ID)),
                                         res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_NAME)),
                                         res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_IMAGE_URL)),
                                         res.getString(res.getColumnIndex(MESSAGE_USER_COLUMN_THUMBNAIL_URL)),
                                         res.getDouble(res.getColumnIndex(MESSAGE_USER_COLUMN_LATITUDE)),
                                         res.getDouble(res.getColumnIndex(MESSAGE_USER_COLUMN_LONGITUDE)));

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

    public int deleteMessageUser(Integer userID) {
        SQLiteDatabase db = null;
        int result;
        try{
            db = this.getWritableDatabase();
            db.beginTransaction();

            result = db.delete(MESSAGE_USER_TABLE_NAME, MESSAGE_USER_COLUMN_ID + " = ?", new String[] { userID.toString() });
        }finally {
            if (db != null && db.isOpen()){
                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();
            }
        }

        return result;
    }

    public int deleteMessageUser(User user) {
        return deleteMessageUser(user.getID());
    }

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
    }

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

    public boolean isMessageUserExists(User user) {
        return isMessageUserExists(user.getID());
    }
}