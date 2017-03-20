package com.karambit.bookie.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by doruk on 20.03.2017.
 */

public class DBManager {

    private DBHelper mDbHelper;
    private SQLiteDatabase mSqLiteDatabase;
    private Context mContext;

    //Data Sources for this manager
    private UserDataSource mUserDataSource;
    private LovedGenreDataSource mLovedGenreDataSource;
    private MessageDataSource mMessageDataSource;
    private NotificationDataSource mNotificationDataSource;
    private SearchUserDataSource mSearchUserDataSource;
    private SearchBookDataSource mSearchBookDataSource;

    public DBManager(Context context){
        mDbHelper = new DBHelper(context);
        mContext = context;
    }

    public void open() throws SQLException {
        mSqLiteDatabase = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDbHelper.close();
    }

    public UserDataSource getUserDataSource(){
        if (mUserDataSource == null){
            mUserDataSource = new UserDataSource(mSqLiteDatabase);
        }
        return mUserDataSource;
    }

    public LovedGenreDataSource getLovedGenreDataSource(){
        if (mLovedGenreDataSource == null){
            mLovedGenreDataSource = new LovedGenreDataSource(mSqLiteDatabase);
        }
        return mLovedGenreDataSource;
    }

    public MessageDataSource getMessageDataSource(){
        if (mMessageDataSource == null){
            mMessageDataSource = new MessageDataSource(mSqLiteDatabase);
        }
        return mMessageDataSource;
    }

    public NotificationDataSource getNotificationDataSource(){
        if (mNotificationDataSource == null){
            mNotificationDataSource = new NotificationDataSource(mSqLiteDatabase);
        }
        return mNotificationDataSource;
    }

    public SearchUserDataSource getSearchUserDataSource(){
        if (mSearchUserDataSource == null){
            mSearchUserDataSource = new SearchUserDataSource(mSqLiteDatabase);
        }
        return mSearchUserDataSource;
    }

    public SearchBookDataSource getSearchBookDataSource(){
        if (mSearchBookDataSource == null){
            mSearchBookDataSource = new SearchBookDataSource(mSqLiteDatabase);
        }
        return mSearchBookDataSource;
    }
}
