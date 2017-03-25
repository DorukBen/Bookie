package com.karambit.bookie.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.karambit.bookie.database.DBHelper;
import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.model.User;

/**
 * Created by orcan on 11/13/16.
 */

public class SessionManager {

    private static User.Details mUserDetails;

    public static final String TAG = SessionManager.class.getSimpleName();

    private static final String NAME_SHARED_PREFERENCES = "bookie_general_sp";

    private static final String IS_LOGGED_IN = "is_logged_in";

    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    private static void changeLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.apply();
        editor.commit();
    }

    public static void logout(Context context) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        dbManager.getLovedGenreDataSource().resetGenres(getCurrentUser(context));
        dbManager.getMessageDataSource().deleteAllMessages();
        dbManager.getUserDataSource().deleteUser();
        dbManager.getNotificationDataSource().deleteAllNotifications();
        dbManager.getSearchUserDataSource().deleteAllUsers();
        dbManager.getSearchBookDataSource().deleteAllBooks();
        changeLoginStatus(context, false);
        mUserDetails = null;
    }

    public static void login(Context context, User.Details userDetails) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        dbManager.getUserDataSource().saveUser(userDetails);
        changeLoginStatus(context, true);
        mUserDetails = userDetails;
    }

    public static void updateCurrentUserFromDB(Context context){
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        mUserDetails = dbManager.getUserDataSource().getUserDetails();
    }

    public static void updateCurrentUser(User.Details userDetails){
        mUserDetails = userDetails;
    }

    public static User.Details getCurrentUserDetails(Context context) {
        if (mUserDetails == null){
            DBManager dbManager = new DBManager(context);
            dbManager.open();
            mUserDetails = dbManager.getUserDataSource().getUserDetails();
            return mUserDetails;
        }else {
            return mUserDetails;
        }
    }

    public static User getCurrentUser(Context context) {
        if (mUserDetails == null){
            DBManager dbManager = new DBManager(context);
            dbManager.open();
            mUserDetails = dbManager.getUserDataSource().getUserDetails();
            return mUserDetails.getUser();
        }else {
            return mUserDetails.getUser();
        }
    }

    public static boolean isLovedGenresSelectedLocal(Context context) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        boolean result =  dbManager.getLovedGenreDataSource().isGenresSelected(getCurrentUser(context));
        return result;
    }
}
