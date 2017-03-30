package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

import java.util.Arrays;

/**
 * Created by doruk on 19.03.2017.
 */

public class LovedGenreDataSource {

    private static final String TAG = LovedGenreDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;

    private static final String LG_TABLE_NAME = "loved_genre";
    private static final String LG_COLUMN_ID = "loved_genre_id";
    private static final String LG_COLUMN_USER_ID = "user_id";
    private static final String LG_COLUMN_GENRE_CODE = "genre_code";

    public static final String CREATE_LOVED_GENRE_TABLE_TAG = "CREATE TABLE " + LG_TABLE_NAME + " (" +
            LG_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            LG_COLUMN_USER_ID + " INTEGER NOT NULL, " +
            LG_COLUMN_GENRE_CODE + " INTEGER NOT NULL)";

    public static final String UPGRADE_LOVED_GENRE_TABLE_TAG = "DROP TABLE IF EXISTS " + LG_TABLE_NAME;

    public LovedGenreDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
    }

    /**
     * Insert loved genres to database.<br>
     *
     * @param user {@link User User}<br>
     * @param genreCodes {@link Integer Integer}[] loved genre codes<br>
     *
     * @return boolean value. If loved genres insertion successful return true else returns false.
     */
    public boolean insertGenres(User user, Integer[] genreCodes) {
        try {
            for (Integer lovedGenreCode : genreCodes) {

                ContentValues contentValues = new ContentValues();
                contentValues.put(LG_COLUMN_USER_ID, user.getID());
                contentValues.put(LG_COLUMN_GENRE_CODE, lovedGenreCode);

                if (mSqLiteDatabase.insert(LG_TABLE_NAME, null, contentValues) <= 0) {
                    Log.e(TAG, "Error occurred during loved genre insertion!");
                    return false;
                }
            }
        }finally {
            Logger.d("Loved genres inserted");
        }
        return true;
    }

    /**
     * Gets loved genres from database for given {@link User user}.<br>
     *
     * @param user {@link User User}<br>
     *
     * @return int[] loved genre code array.
     */
    public Integer[] getGenres(User user) {
        Cursor res = null;
        Integer[] lovedGenres;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + LG_TABLE_NAME +
                    " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID(), null);
            res.moveToFirst();

            lovedGenres = new Integer[res.getCount()];
            int i = 0;
            if (res.getCount() > 0) {
                do {
                    lovedGenres[i++] = res.getInt(res.getColumnIndex(LG_COLUMN_GENRE_CODE));
                } while (res.moveToNext());
            } else{
                Log.e(TAG, "Error occurred while fetching loved genres!");
            }
        } finally {
            if (res != null){
                res.close();
            }
        }
        Log.e(TAG, "Loved genres fetched. " + Arrays.toString(lovedGenres));
        return lovedGenres;
    }

    /**
     * Resets loved genres from database for given {@link User user}.<br>
     *
     * @param user {@link User User}<br>
     */
    public void resetGenres(User user) {
        try {
            mSqLiteDatabase.delete(LG_TABLE_NAME, LG_COLUMN_USER_ID + " = " + user.getID(), null);
        }finally {
            Logger.d("Loved Genres reset");
        }
    }

    /**
     * Checks database for inserted loved genres.<br>
     *
     * @param user {@link User User}<br>
     *
     * @return boolean value. If database have any genre code returns true else false.
     */
    public boolean isGenresSelected(User user) {
        Cursor res = null;

        try {
            String countQuery = "SELECT * FROM " + LG_TABLE_NAME + " WHERE " + LG_COLUMN_USER_ID + " = " + user.getID();
            res = mSqLiteDatabase.rawQuery(countQuery, null);
            return res.getCount() > 0;

        } finally {
            if (res != null) {
                res.close();
            }
        }
    }
}
