package com.karambit.bookie.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.karambit.bookie.database.DBManager;
import com.karambit.bookie.model.User;

import java.util.Calendar;

/**
 * Created by orcan on 11/13/16.
 */

public class SessionManager {

    private static User.Details mUserDetails;

    private static String mLocationText;

    public static final String TAG = SessionManager.class.getSimpleName();

    private static final String NAME_SHARED_PREFERENCES = "bookie_general_sp";

    private static final String IS_LOGGED_IN = "is_logged_in";
    private static final String LAST_LOCATION_REMINDER = "last_reminded";
    private static final String LAST_LOGGED_EMAIL = "last_logged_email";

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
        dbManager.Threaded(dbManager.getLovedGenreDataSource().cResetGenres(getCurrentUser(context)));
        dbManager.Threaded(dbManager.getMessageDataSource().cDeleteAllMessages());
        dbManager.Threaded(dbManager.getUserDataSource().cDeleteUser());
        dbManager.Threaded(dbManager.getNotificationDataSource().cDeleteAllNotifications());
        dbManager.Threaded(dbManager.getSearchUserDataSource().cDeleteAllUsers());
        dbManager.Threaded(dbManager.getSearchBookDataSource().cDeleteAllBooks());
        changeLoginStatus(context, false);
        mLocationText = null;
        mUserDetails = null;
    }

    public static void login(Context context, User.Details userDetails) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        dbManager.Threaded(dbManager.getUserDataSource().cSaveUser(userDetails));
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

    public static void saveEmailAddress(Context context, String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LAST_LOGGED_EMAIL, email);
        editor.apply();
        editor.commit();
    }

    public static String getLastEmailAddress(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LAST_LOGGED_EMAIL, "");
    }

    public static boolean isLovedGenresSelectedLocal(Context context) {
        DBManager dbManager = new DBManager(context);
        dbManager.open();
        boolean result =  dbManager.getLovedGenreDataSource().isGenresSelected(getCurrentUser(context));
        return result;
    }

    public static String getLocationText() {
        return mLocationText;
    }

    public static void setLocationText(String locationText) {
        mLocationText = locationText;
    }

    public static Calendar getLastLocationReminderTime(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        long timeInMillis = sharedPreferences.getLong(LAST_LOCATION_REMINDER, -1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar;
    }

    public static void notifyLocationReminded(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(LAST_LOCATION_REMINDER, Calendar.getInstance().getTimeInMillis());
        editor.apply();
        editor.commit();
    }
}
