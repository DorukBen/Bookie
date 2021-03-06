package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.User;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by doruk on 19.03.2017.
 */

public class SearchBookDataSource {

    private static final String TAG = SearchBookDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;
    private SearchBookUserDataSource mSearchBookUserDataSource;

    private static final String SEARCH_BOOK_TABLE_NAME = "search_book";
    private static final String SEARCH_BOOK_COLUMN_ID = "book_id";
    private static final String SEARCH_BOOK_COLUMN_NAME = "name";
    private static final String SEARCH_BOOK_COLUMN_IMAGE_URL = "image_url";
    private static final String SEARCH_BOOK_COLUMN_THUMBNAIL_URL = "thumbnail_url";
    private static final String SEARCH_BOOK_COLUMN_AUTHOR = "author";
    private static final String SEARCH_BOOK_COLUMN_STATE = "state";
    private static final String SEARCH_BOOK_COLUMN_GENRE = "genre";
    private static final String SEARCH_BOOK_COLUMN_OWNER_ID = "owner_id";

    static final String CREATE_SEARCH_BOOK_TABLE_TAG = "CREATE TABLE " + SEARCH_BOOK_TABLE_NAME + " (" +
            SEARCH_BOOK_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            SEARCH_BOOK_COLUMN_NAME + " TEXT NOT NULL, " +
            SEARCH_BOOK_COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
            SEARCH_BOOK_COLUMN_THUMBNAIL_URL + " TEXT NOT NULL, " +
            SEARCH_BOOK_COLUMN_AUTHOR + " TEXT NOT NULL, " +
            SEARCH_BOOK_COLUMN_STATE + " INTEGER NOT NULL, " +
            SEARCH_BOOK_COLUMN_GENRE + " INTEGER NOT NULL, " +
            SEARCH_BOOK_COLUMN_OWNER_ID + " INTEGER NOT NULL)";

    static final String UPGRADE_SEARCH_BOOK_TABLE_TAG = "DROP TABLE IF EXISTS " + SEARCH_BOOK_TABLE_NAME;

    SearchBookDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
        mSearchBookUserDataSource = new SearchBookUserDataSource(mSqLiteDatabase);
    }

    public boolean saveBook(Book book){
        boolean result = false;
        mSearchBookUserDataSource.saveUser(book.getOwner());

        if (!isBookExists(book)){
            result = insertBook(book);
        }

        return result;
    }

    /**
     * Insert {@link Book book} to database.<br>
     *
     * @param book {@link Book} which will be inserted
     * @return Returns boolean value if insertion successful returns true else returns false
     */
    private boolean insertBook(Book book) {
        boolean result = false;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(SEARCH_BOOK_COLUMN_ID, book.getID());
            contentValues.put(SEARCH_BOOK_COLUMN_NAME, book.getName());
            contentValues.put(SEARCH_BOOK_COLUMN_IMAGE_URL, book.getImageURL());
            contentValues.put(SEARCH_BOOK_COLUMN_THUMBNAIL_URL, book.getThumbnailURL());
            contentValues.put(SEARCH_BOOK_COLUMN_AUTHOR, book.getAuthor());
            contentValues.put(SEARCH_BOOK_COLUMN_STATE, book.getState().getStateCode());
            contentValues.put(SEARCH_BOOK_COLUMN_GENRE, book.getGenreCode());
            contentValues.put(SEARCH_BOOK_COLUMN_OWNER_ID, book.getOwner().getID());

            result = mSqLiteDatabase.insert(SEARCH_BOOK_TABLE_NAME, null, contentValues) > 0;
        }finally {
            Logger.d("New Book insertion successful");
        }
        return result;
    }

    /**
     * Updates {@link Book book} to database.<br>
     *
     * @param book {@link Book} which will be updated
     * @return Returns boolean value if update successful returns true else returns false
     */
    private boolean updateBook(Book book) {
        boolean result = false;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(SEARCH_BOOK_COLUMN_ID, book.getID());
            contentValues.put(SEARCH_BOOK_COLUMN_NAME, book.getName());
            contentValues.put(SEARCH_BOOK_COLUMN_IMAGE_URL, book.getImageURL());
            contentValues.put(SEARCH_BOOK_COLUMN_THUMBNAIL_URL, book.getThumbnailURL());
            contentValues.put(SEARCH_BOOK_COLUMN_AUTHOR, book.getAuthor());
            contentValues.put(SEARCH_BOOK_COLUMN_STATE, book.getState().getStateCode());
            contentValues.put(SEARCH_BOOK_COLUMN_GENRE, book.getGenreCode());
            contentValues.put(SEARCH_BOOK_COLUMN_OWNER_ID, book.getOwner().getID());

            result = mSqLiteDatabase.update(SEARCH_BOOK_TABLE_NAME, contentValues, SEARCH_BOOK_COLUMN_ID + "=" + book.getID(), null) > 0;
        }finally {
            Logger.d("New Book update successful");
        }
        return result;
    }

    public boolean checkAndUpdateBook(Book book) {
        if (isBookExists(book)) {
            return updateBook(book);
        } else {
            return false;
        }
    }

    /**
     * Checks database for given book's existence. Use before all book insertions.<br>
     *
     * @param book {@link Book book}
     *
     * @return  boolean value. If message {@link Book book} exist returns true else returns false.
     */
    public boolean isBookExists(Book book) {
        Cursor res = null;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + SEARCH_BOOK_TABLE_NAME + " WHERE " + SEARCH_BOOK_COLUMN_ID  + " = " + book.getID(), null);
            res.moveToFirst();

            return res.getCount() > 0;

        }finally {
            if (res != null) {
                res.close();
            }
        }
    }

    /**
     * Get all {@link Book book's} from database.<br>
     *
     * @return All {@link Book books's}
     */
    public ArrayList<Book> getAllBooks() {

        ArrayList<User> bookUsers = mSearchBookUserDataSource.getAllUsers();

        Cursor res = null;
        ArrayList<Book> books = new ArrayList<>();
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + SEARCH_BOOK_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    for (User user: bookUsers){
                        if (user.getID() == res.getInt(res.getColumnIndex(SEARCH_BOOK_COLUMN_OWNER_ID))){
                            Book book = new Book(res.getInt(res.getColumnIndex(SEARCH_BOOK_COLUMN_ID)),
                                    res.getString(res.getColumnIndex(SEARCH_BOOK_COLUMN_NAME)),
                                    res.getString(res.getColumnIndex(SEARCH_BOOK_COLUMN_IMAGE_URL)),
                                    res.getString(res.getColumnIndex(SEARCH_BOOK_COLUMN_THUMBNAIL_URL)),
                                    res.getString(res.getColumnIndex(SEARCH_BOOK_COLUMN_AUTHOR)),
                                    Book.State.valueOf(res.getInt(res.getColumnIndex(SEARCH_BOOK_COLUMN_STATE))),
                                    res.getInt(res.getColumnIndex(SEARCH_BOOK_COLUMN_GENRE)),
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
    public boolean deleteAllBooks() {
        boolean result;
        try{
            mSearchBookUserDataSource.deleteAllUsers();
            result = mSqLiteDatabase.delete(SEARCH_BOOK_TABLE_NAME, null, null) > 0;
        }finally {
            Logger.d("Books and book users deleted from database");
        }

        return result;
    }

    //Callable Methods
    public Callable<Boolean> cSaveBook(final Book book){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return saveBook(book);
            }
        };
    }

    public Callable<Boolean> cCheckAndUpdateBook(final Book book){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return checkAndUpdateBook(book);
            }
        };
    }

    public Callable<Boolean> cDeleteAllBooks (){
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return deleteAllBooks();
            }
        };
    }
}
