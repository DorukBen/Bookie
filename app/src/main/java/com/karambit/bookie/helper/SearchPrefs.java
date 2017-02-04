package com.karambit.bookie.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by doruk on 4.02.2017.
 */

public class SearchPrefs {

    private static final String NAME_SHARED_PREFERENCES = "bookie_search_page_sp";
    //0 if string search, 1 if genre search, -1 if not searched before
    private static final String LAST_SEARCH_TYPE = "last_search_type";
    private static final int LAST_SEARCH_STRING = 0;
    private static final int LAST_SEARCH_GENRE = 1;
    private static final int NOT_SEARCHED_BEFORE = -1;

    private static final String LAST_SEARCHED_GENRE_CODE = "last_searched_genre_code";
    private static final String LAST_SEARCHED_STRING = "last_searched_string";

    private int mLastSearchedGenreCode = -1;

    public static void changeLastSearch(Context context, int genreCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_SEARCH_TYPE, LAST_SEARCH_GENRE);
        editor.putInt(LAST_SEARCHED_GENRE_CODE, genreCode);
        editor.apply();
        editor.commit();
    }

    public static void changeLastSearch(Context context, String string) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LAST_SEARCH_TYPE, LAST_SEARCH_STRING);
        editor.putString(LAST_SEARCHED_STRING, string);
        editor.apply();
        editor.commit();
    }

    public static boolean isSearchedBefore(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(LAST_SEARCH_TYPE, -1) != NOT_SEARCHED_BEFORE;
    }

    public static boolean isLastSearchGenreCode(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(LAST_SEARCH_TYPE, -1) == LAST_SEARCH_GENRE;
    }

    public static int getLastSearchedGenre(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(LAST_SEARCHED_GENRE_CODE, -1);
    }

    public static String getLastSearchedString(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LAST_SEARCHED_STRING, "");
    }
}
