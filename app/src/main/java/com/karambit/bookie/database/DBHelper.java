package com.karambit.bookie.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orhanobut.logger.Logger;

/**
 * General class for creating and upgrading application's database.
 *
 * Created by doruk on 12.11.2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String TAG = DBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "BookieApplication.db";
    private static final int DATABASE_VERSION = 1;

    private static DBHelper mInstance = null;

    public static DBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DBHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(UserDataSource.CREATE_USER_TABLE_TAG);
        db.execSQL(LovedGenreDataSource.CREATE_LOVED_GENRE_TABLE_TAG);
        db.execSQL(MessageDataSource.CREATE_MESSAGE_TABLE_TAG);
        db.execSQL(MessageUserDataSource.CREATE_MESSAGE_USER_TABLE_TAG);
        db.execSQL(NotificationDataSource.CREATE_NOTIFICATION_TABLE_TAG);
        db.execSQL(NotificationUserDataSource.CREATE_NOTIFICATION_USER_TABLE_TAG);
        db.execSQL(NotificationBookDataSource.CREATE_NOTIFICATION_BOOK_TABLE_TAG);
        db.execSQL(NotificationBookUserDataSource.CREATE_NOTIFICATION_BOOK_USER_TABLE_TAG);
        db.execSQL(SearchUserDataSource.CREATE_SEARCH_USER_TABLE_TAG);
        db.execSQL(SearchBookDataSource.CREATE_SEARCH_BOOK_TABLE_TAG);
        db.execSQL(SearchBookUserDataSource.CREATE_SEARCH_BOOK_USER_TABLE_TAG);

        Logger.d("Databases created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(UserDataSource.UPGRADE_USER_TABLE_TAG);
        db.execSQL(LovedGenreDataSource.UPGRADE_LOVED_GENRE_TABLE_TAG);
        db.execSQL(MessageDataSource.UPGRADE_MESSAGE_TABLE_TAG);
        db.execSQL(MessageUserDataSource.UPGRADE_MESSAGE_USER_TABLE_TAG);
        db.execSQL(NotificationDataSource.UPGRADE_NOTIFICATION_TABLE_TAG);
        db.execSQL(NotificationUserDataSource.UPGRADE_NOTIFICATION_USER_TABLE_TAG);
        db.execSQL(NotificationBookDataSource.UPGRADE_NOTIFICATION_BOOK_TABLE_TAG);
        db.execSQL(NotificationBookUserDataSource.UPGRADE_NOTIFICATION_BOOK_USER_TABLE_TAG);
        db.execSQL(SearchUserDataSource.UPGRADE_SEARCH_USER_TABLE_TAG);
        db.execSQL(SearchBookDataSource.UPGRADE_SEARCH_BOOK_TABLE_TAG);
        db.execSQL(SearchBookUserDataSource.UPGRADE_SEARCH_BOOK_USER_TABLE_TAG);

        onCreate(db);
        Logger.d("Databases upgraded.");
    }
}