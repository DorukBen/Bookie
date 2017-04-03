package com.karambit.bookie.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;

import java.util.concurrent.Callable;

/**
 * Created by doruk on 20.03.2017.
 */

public class DBManager {

    private DBHelper mDbHelper;
    private SQLiteDatabase mSqLiteDatabase;

    //Data Sources for this manager
    private UserDataSource mUserDataSource;
    private LovedGenreDataSource mLovedGenreDataSource;
    private MessageDataSource mMessageDataSource;
    private NotificationDataSource mNotificationDataSource;
    private SearchUserDataSource mSearchUserDataSource;
    private SearchBookDataSource mSearchBookDataSource;
    private SearchBookUserDataSource mSearchBookUserDataSource;
    private MessageUserDataSource mMessageUserDataSource;
    private NotificationUserDataSource mNotificationUserDataSource;
    private NotificationBookDataSource mNotificationBookDataSource;
    private NotificationBookUserDataSource mNotificationBookUserDataSource;


    public DBManager(Context context){
        mDbHelper = DBHelper.getInstance(context);
    }

    public void open() throws SQLException {
        if (mSqLiteDatabase == null){
            mSqLiteDatabase = mDbHelper.getWritableDatabase();
        }
    }

    public void close() {
        mDbHelper.close();
    }

    public void Threaded(final Callable func){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    func.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    public SearchBookUserDataSource getSearchBookUserDataSource(){
        if (mSearchBookUserDataSource == null){
            mSearchBookUserDataSource = new SearchBookUserDataSource(mSqLiteDatabase);
        }
        return mSearchBookUserDataSource;
    }

    public MessageUserDataSource getMessageUserDataSource(){
        if (mMessageUserDataSource == null){
            mMessageUserDataSource = new MessageUserDataSource(mSqLiteDatabase);
        }
        return mMessageUserDataSource;
    }

    public NotificationUserDataSource getNotificationUserDataSource() {
        if (mNotificationUserDataSource == null){
            mNotificationUserDataSource = new NotificationUserDataSource(mSqLiteDatabase);
        }
        return mNotificationUserDataSource;
    }

    public NotificationBookUserDataSource getNotificationBookUserDataSource() {
        if (mNotificationBookUserDataSource == null){
            mNotificationBookUserDataSource = new NotificationBookUserDataSource(mSqLiteDatabase);
        }
        return mNotificationBookUserDataSource;
    }

    public NotificationBookDataSource getNotificationBookDataSource() {
        if (mNotificationBookDataSource == null) {
            mNotificationBookDataSource = new NotificationBookDataSource(mSqLiteDatabase);
        }
        return mNotificationBookDataSource;
    }

    public void checkAndUpdateAllUsers(User user) {
        Threaded(getMessageUserDataSource().cCheckAndUpdateUser(user));
        Threaded(getNotificationBookUserDataSource().cCheckAndUpdateUser(user));
        Threaded(getNotificationUserDataSource().cCheckAndUpdateUser(user));
        Threaded(getSearchUserDataSource().cCheckAndUpdateUser(user));
        Threaded(getSearchBookUserDataSource().cCheckAndUpdateUser(user));
    }

    public void checkAndUpdateAllBooks(Book book) {
        Threaded(getSearchBookDataSource().cCheckAndUpdateBook(book));
        Threaded(getNotificationBookDataSource().cCheckAndUpdateBook(book));
    }
}
