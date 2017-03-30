package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by doruk on 19.03.2017.
 */

public class NotificationBookDataSource {

    private static final String TAG = NotificationBookDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;
    private NotificationBookUserDataSource mNotificationBookUserDataSource;

    private static final String NOTIFICATION_BOOK_TABLE_NAME = "notification_book";
    private static final String NOTIFICATION_BOOK_COLUMN_ID = "book_id";
    private static final String NOTIFICATION_BOOK_COLUMN_NAME = "name";
    private static final String NOTIFICATION_BOOK_COLUMN_IMAGE_URL = "image_url";
    private static final String NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String NOTIFICATION_BOOK_COLUMN_AUTHOR = "author";
    private static final String NOTIFICATION_BOOK_COLUMN_STATE = "state";
    private static final String NOTIFICATION_BOOK_COLUMN_GENRE = "genre";
    private static final String NOTIFICATION_BOOK_COLUMN_OWNER_ID = "owner_id";

    public static final String CREATE_NOTIFICATION_BOOK_TABLE_TAG = "CREATE TABLE " + NOTIFICATION_BOOK_TABLE_NAME + " (" +
            NOTIFICATION_BOOK_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            NOTIFICATION_BOOK_COLUMN_NAME + " TEXT NOT NULL, " +
            NOTIFICATION_BOOK_COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
            NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
            NOTIFICATION_BOOK_COLUMN_AUTHOR + " TEXT NOT NULL, " +
            NOTIFICATION_BOOK_COLUMN_STATE + " INTEGER NOT NULL, " +
            NOTIFICATION_BOOK_COLUMN_GENRE + " INTEGER NOT NULL, " +
            NOTIFICATION_BOOK_COLUMN_OWNER_ID + " INTEGER NOT NULL)";

    public static final String UPGRADE_NOTIFICATION_BOOK_TABLE_TAG = "DROP TABLE IF EXISTS " + NOTIFICATION_BOOK_TABLE_NAME;

    public NotificationBookDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
        mNotificationBookUserDataSource = new NotificationBookUserDataSource(mSqLiteDatabase);
    }

    /**
     * Insert {@link Book book} to database.<br>
     *
     * @param book {@link Book} which will be inserted
     * @return Returns boolean value if insertion successful returns true else returns false
     */
    public boolean insertBook(Book book) {
        boolean result = false;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_BOOK_COLUMN_ID, book.getID());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_NAME, book.getName());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_IMAGE_URL, book.getImageURL());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL, book.getThumbnailURL());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_AUTHOR, book.getAuthor());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_STATE, book.getState().getStateCode());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_GENRE, book.getGenreCode());
            contentValues.put(NOTIFICATION_BOOK_COLUMN_OWNER_ID, book.getOwner().getID());

            result = mSqLiteDatabase.insert(NOTIFICATION_BOOK_TABLE_NAME, null, contentValues) > 0;
        }finally {
            Logger.d("New Book insertion successful");
        }
        return result;
    }

    /**
     * Checks database for given book's existence. Use before all book insertions.<br>
     *
     * @param book {@link Book Book}
     *
     * @return  boolean value. If {@link Book book} exist returns true else returns false.
     */
    public boolean isBookExists(Book book) {
        Cursor res = null;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + NOTIFICATION_BOOK_TABLE_NAME + " WHERE " + NOTIFICATION_BOOK_COLUMN_ID  + " = " + book.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
        }
    }

    /**
     * Get all notification {@link Book book's} from database.<br>
     *
     * @return All notification {@link Book books's}
     */
    public ArrayList<Book> getAllBooks() {

        ArrayList<User> bookUsers = mNotificationBookUserDataSource.getAllUsers();

        Cursor res = null;
        ArrayList<Book> books = new ArrayList<>();
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + NOTIFICATION_BOOK_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    for (User user: bookUsers){
                        if (user.getID() == res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_OWNER_ID))){
                            Book book = new Book(res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_ID)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_NAME)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_IMAGE_URL)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_THUMBNAIL_URL)),
                                    res.getString(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_AUTHOR)),
                                    Book.State.valueOf(res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_STATE))),
                                    res.getInt(res.getColumnIndex(NOTIFICATION_BOOK_COLUMN_GENRE)),
                                    user);

                            books.add(book);
                        }
                    }
                } while (res.moveToNext());
            }
        }finally {
            if (res != null) {
                res.close();
            }
        }
        return books;
    }

    /**
     * Deletes all {@link Book books} from database.<br>
     */
    public void deleteAllBooks() {
        try{
            mSqLiteDatabase.delete(NOTIFICATION_BOOK_TABLE_NAME, null, null);
        }finally {
            Logger.d("All books deleted from database");
        }
    }
}
