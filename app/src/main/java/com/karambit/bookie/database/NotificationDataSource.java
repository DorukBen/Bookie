package com.karambit.bookie.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.karambit.bookie.model.Book;
import com.karambit.bookie.model.Notification;
import com.karambit.bookie.model.User;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by doruk on 19.03.2017.
 */

public class NotificationDataSource {

    private static final String TAG = NotificationDataSource.class.getSimpleName();

    private SQLiteDatabase mSqLiteDatabase;
    private NotificationUserDataSource mNotificationUserDataSource;
    private NotificationBookDataSource mNotificationBookDataSource;
    private NotificationBookUserDataSource mNotificationBookUserDataSource;

    private static final String NOTIFICATION_TABLE_NAME = "notification";
    private static final String NOTIFICATION_COLUMN_ID = "notification_id";
    private static final String NOTIFICATION_COLUMN_BOOK_ID = "book_id";
    private static final String NOTIFICATION_COLUMN_USER_ID = "user_id";
    private static final String NOTIFICATION_COLUMN_TYPE = "type";
    private static final String NOTIFICATION_COLUMN_SEEN = "seen";
    private static final String NOTIFICATION_COLUMN_CREATED_AT = "created_at";

    public static final String CREATE_NOTIFICATION_TABLE_TAG = "CREATE TABLE " + NOTIFICATION_TABLE_NAME + " (" +
            NOTIFICATION_COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            NOTIFICATION_COLUMN_BOOK_ID + " INTEGER NOT NULL, " +
            NOTIFICATION_COLUMN_USER_ID + " INTEGER NOT NULL, " +
            NOTIFICATION_COLUMN_TYPE + " INTEGER NOT NULL, " +
            NOTIFICATION_COLUMN_SEEN + " INTEGER NOT NULL, " +
            NOTIFICATION_COLUMN_CREATED_AT + " LONG NOT NULL)";

    public static final String UPGRADE_NOTIFICATION_TABLE_TAG = "DROP TABLE IF EXISTS " + NOTIFICATION_TABLE_NAME;

    public NotificationDataSource(SQLiteDatabase database) {
        mSqLiteDatabase = database;
        mNotificationUserDataSource = new NotificationUserDataSource(mSqLiteDatabase);
        mNotificationBookDataSource = new NotificationBookDataSource(mSqLiteDatabase);
        mNotificationBookUserDataSource = new NotificationBookUserDataSource(mSqLiteDatabase);
    }

    public void saveNotificationToDatabase(final Notification notification){
        if (!mNotificationBookUserDataSource.isUserExists(notification.getBook().getOwner())){
            mNotificationBookUserDataSource.insertUser(notification.getBook().getOwner());
        }

        if (!mNotificationBookDataSource.isBookExists(notification.getBook())){
            mNotificationBookDataSource.insertBook(notification.getBook());
        }

        if (!mNotificationUserDataSource.isUserExists(notification.getOppositeUser())){
            mNotificationUserDataSource.insertUser(notification.getOppositeUser());
        }

        insertNotification(notification);
    }

    /**
     * Insert notification to database.<br>
     *
     * @param notification {@link Notification} which will be inserted
     * @return Returns int boolean value if insertion successful returns true else returns false
     */
    private boolean insertNotification(Notification notification) {
        boolean result = false;
        int messageSeen = 1;
        int messageUnseen = 0;
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(NOTIFICATION_COLUMN_BOOK_ID, notification.getBook().getID());
            contentValues.put(NOTIFICATION_COLUMN_USER_ID, notification.getOppositeUser().getID());
            contentValues.put(NOTIFICATION_COLUMN_TYPE, notification.getType().getTypeCode());
            if (notification.isSeen()){
                contentValues.put(NOTIFICATION_COLUMN_SEEN, messageSeen);
            }else{
                contentValues.put(NOTIFICATION_COLUMN_SEEN, messageUnseen);
            }
            contentValues.put(NOTIFICATION_COLUMN_CREATED_AT, notification.getCreatedAt().getTimeInMillis());

            result = mSqLiteDatabase.insert(NOTIFICATION_TABLE_NAME, null, contentValues) > 0;
        }finally {
            Log.i(TAG, "New Notification insertion successful");
        }
        return result;
    }

    /**
     * get all {@link Notification notification's} from database.<br>
     *
     * @return Returns all notifications from database
     */
    public ArrayList<Notification> getAllNotifications() {

        ArrayList<User> allNotificationUsers = mNotificationUserDataSource.getAllUsers();
        ArrayList<Book> allNotificationBooks= mNotificationBookDataSource.getAllBooks();

        Cursor res = null;
        int seenString = 1;
        ArrayList<Notification> notifications = new ArrayList<>();
        try {
            res = mSqLiteDatabase.rawQuery("SELECT * FROM " + NOTIFICATION_TABLE_NAME, null);
            res.moveToFirst();

            if (res.getCount() > 0) {
                do {
                    User oppositeUser = null;
                    for (User user: allNotificationUsers){
                        if (user.getID() == res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_USER_ID))){
                            oppositeUser = user;
                        }
                    }

                    Book notificationBook = null;
                    for (Book book: allNotificationBooks){
                        if (book.getID() == res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_BOOK_ID))){
                            notificationBook = book;
                        }
                    }

                    Calendar calendar = Calendar.getInstance();
                    long time = res.getLong(res.getColumnIndex(NOTIFICATION_COLUMN_CREATED_AT)); //replace 4 with the column index
                    calendar.setTimeInMillis(time);

                    Notification notification;
                    if (res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_SEEN)) == seenString){
                        notification = new Notification(Notification.Type.valueOf(res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_TYPE))),
                                calendar,
                                notificationBook,
                                oppositeUser,
                                true);
                    }else {
                        notification = new Notification(Notification.Type.valueOf(res.getInt(res.getColumnIndex(NOTIFICATION_COLUMN_TYPE))),
                                calendar,
                                notificationBook,
                                oppositeUser,
                                false);
                    }


                    notifications.add(notification);
                } while (res.moveToNext());
            }
        }finally {
            if (res != null) {
                res.close();
            }
        }
        return notifications;
    }

    /**
     * Counts unseen notifications.<br>
     *
     * @return Total unseen notification count for given user
     */
    public int getUnseenNotificationCount(){
        Cursor res = null;
        String queryVariableString = "totalCount";
        int unseenMessageCount = 0;

        try {
            res = mSqLiteDatabase.rawQuery("SELECT COUNT(*) AS " + queryVariableString + " FROM " + NOTIFICATION_TABLE_NAME +
                    " WHERE " + NOTIFICATION_COLUMN_SEEN + " = " + 0, null);
            res.moveToFirst();

            unseenMessageCount = res.getInt(res.getColumnIndex(queryVariableString));

        } finally {
            if (res != null){
                res.close();
            }
        }

        return unseenMessageCount;
    }


    /**
     * Updates all notifications seen value.<br>
     *
     * Using SQLiteOpenHelper. Can't access database simultaneously.<br>

     */
    public void updateAllNotificationsSeen(){

        try{
            ContentValues cv = new ContentValues();
            cv.put(NOTIFICATION_COLUMN_SEEN, 1);

            mSqLiteDatabase.update(NOTIFICATION_TABLE_NAME, cv,null, null);
        }finally {
            Log.i(TAG, "Notifications seen updated updated");
        }
    }

    /**
     * Deletes all notifications from database.
     */
    public void deleteAllNotifications() {
        try{
            mSqLiteDatabase.delete(NOTIFICATION_TABLE_NAME, null, null);
            mNotificationBookDataSource.deleteAllBooks();
            mNotificationBookUserDataSource.deleteAllUsers();
            mNotificationUserDataSource.deleteAllUsers();

        } finally {
            Log.i(TAG, "All Notification books, notification book users, notification users and notifications deleted from database");
        }
    }
}
