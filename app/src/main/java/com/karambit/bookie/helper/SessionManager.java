package com.karambit.bookie.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.karambit.bookie.model.User;

/**
 * Created by orcan on 11/13/16.
 */

public class SessionManager {

    private static User mUser;

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
        DBHandler dbHandler = DBHandler.getInstance(context);
        dbHandler.resetLovedGenres(getCurrentUser(context));
        dbHandler.deleteAllMessages();
        dbHandler.deleteCurrentUser();
        changeLoginStatus(context, false);
        mUserDetails = null;
        mUser = null;
    }

    public static void login(Context context, User.Details userDetails) {
        DBHandler dbHandler = DBHandler.getInstance(context);
        dbHandler.insertCurrentUser(userDetails);
        changeLoginStatus(context, true);
        mUserDetails = userDetails;
        mUser = userDetails.getUser();
    }

    public static void updateCurrentUserFromDB(Context context){
        DBHandler dbHandler = DBHandler.getInstance(context);
        mUserDetails = dbHandler.getCurrentUserDetails();
        mUser = dbHandler.getCurrentUser();
    }

    public static User.Details getCurrentUserDetails(Context context) {
        if (mUserDetails == null){
            mUserDetails = DBHandler.getInstance(context).getCurrentUserDetails();
            return mUserDetails;
        }else {
            return mUserDetails;
        }
    }

    public static User getCurrentUser(Context context) {
        if (mUser == null){
            mUser = DBHandler.getInstance(context).getCurrentUser();
            return mUser;
        }else {
            return mUser;
        }
    }

    public static boolean isLovedGenresSelectedLocal(Context context) {
        DBHandler dbHandler = DBHandler.getInstance(context);
        return dbHandler.isLovedGenresSelected(getCurrentUser(context));
    }
}
